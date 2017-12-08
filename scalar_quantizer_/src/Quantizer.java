import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/*
 * multimedia assignment scalar quantization compression and decompression with gui
 */

public class Quantizer extends Application {

    static ArrayList<Integer> original = new ArrayList<>(); // 1D array holds the whole pixels in the image
                                                            // (constructed in readImage)
    static int bitLevel = 0, width, height;
    ArrayList<Node> averages = new ArrayList<>();   // array of nodes that will contain the last averages
                                                    // according to the bit level (leaves)
    static ArrayList<q> quantizer = new ArrayList<>();  // the final table to be written after compression

    String QtablePath = "/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/quantizer.txt";
    String DecompressedImgagePath = "/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/lenaAfterCompress.jpg";
    String QimagePath = "/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/Qimage.txt";

    class Node {
        double average;
        Node left;
        Node right;
        int level = 0;
        ArrayList<Integer> relatedNumbers = new ArrayList<>();

        Node(double n) {
            this.average = n;
            this.relatedNumbers = new ArrayList<>();
            this.level = 0;
        }
    }

    class q { // table
        int lowRange;
        int highRange;
        int Q; // 0 1 2 3 kinda like an index
        int Q1; // midpoint of each level

        q(int lowRange, int highRange, int q, int q1) {
            this.lowRange = lowRange;
            this.highRange = highRange;
            this.Q = q;
            this.Q1 = q1;
        }
    }

    public static int[][] readImage(String filePath) {
        //int width = 0;
        //int height = 0;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        int[][] pixels = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;

                pixels[y][x] = r;
                original.add(r);
            }
        }
        return pixels;
    }

    public static void writeImage(int[][] pixels, String outputFilePath, int width, int height) {
        File fileout = new File(outputFilePath);
        System.out.println(width + " " + height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // FIX ME!!

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, (pixels[y][x]<<16) | (pixels[y][x]<<8) | (pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/* COMPRESSION */
// compress -> quantize -> recursion(construct) -> association check (check) -> back to compress

/* DECOMPRESSION */
// just decompress lol

    public void compress(String path) throws FileNotFoundException { //
        int[][] p = readImage(path);
        PrintWriter out = new PrintWriter(new File("/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/quantizer.txt"));
        PrintWriter out2 = new PrintWriter(new File("/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/Qimage.txt"));

        // building avgs array
        quantize();
        // now avgs has the required leaves

        // printing the table (to be used in decompression)
        out.println(bitLevel);
        out.println(width + " " + height);
        for (q i : quantizer) {
            out.println(i.Q1);
        }
        out.close();

        // printing the compressed image
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (q x : quantizer) {
                    if (p[i][j] >= x.lowRange && p[i][j] <= x.highRange) {
                        out2.printf(x.Q + " ");
                        break;
                    }
                }
            }
            out2.printf("\n");
        }
        out2.close();
    }

    public void quantize() throws FileNotFoundException {

        // calculating just the first average and calling construct
        double average = 0;
        for (int i = 0; i < original.size(); i++) {
            average += original.get(i);
        }
        average /= original.size();
        Node node = new Node(average);
        averages.add(node);
        constructTree(averages);
    }

    public void constructTree(ArrayList<Node> averages) throws FileNotFoundException {

        // finding the nearest numbers
        for (int i = 0; i < original.size(); i++) {
            double min = 1e9;
            int index = -1;
            for (int j = 0; j < averages.size(); j++) {

                if ((Math.abs(original.get(i) - averages.get(j).average) < min)) { // splitting
                    min = Math.abs(original.get(i) - averages.get(j).average);
                    index = j; // updating index to equal index of min
                }
            }
            averages.get(index).relatedNumbers.add(original.get(i)); // adding
        }

        // break condition -> calling association check
        if (averages.size() == Math.pow(2, bitLevel)) {
            check(averages); // association check
            return;
        }

        // calculating average and splitting
        ArrayList<Node> nodearray = new ArrayList<>();

        for (int i = 0; i < averages.size(); i++) {
            double average = 0.0;
            for (int j = 0; j < averages.get(i).relatedNumbers.size(); j++) {
                average += averages.get(i).relatedNumbers.get(j);
            }

            average /= averages.get(i).relatedNumbers.size();
            averages.get(i).left = new Node(average - 1);
            averages.get(i).right = new Node(average + 1);

            nodearray.add(averages.get(i).left);
            nodearray.add(averages.get(i).right);
        }
        constructTree(nodearray);
    }

    public void check(ArrayList<Node> avgs) throws FileNotFoundException { // association check
        // the main idea is to have two arrays, one holds the old averages and the other the new ones
        // and it keeps comparing these two, if they're equal it means that the association should stop

        // the newAverages array is constructed just like in 'construct' function
        // but this time im repeating the process in the last nodes only (leaves)

        ArrayList<Node> newAverages = new ArrayList<>();
        while (true) {
            // constructing newAverages
            for (Node i : avgs) {
                double avg = 0.0;
                for (int j = 0; j < i.relatedNumbers.size(); j++) {
                    avg += i.relatedNumbers.get(j);
                }
                i.average = avg / i.relatedNumbers.size();
                newAverages.add(new Node(i.average));
            }
            for (int i = 0; i < original.size(); i++) {
                double min = 1e9;
                int index = -1;
                for (int j = 0; j < newAverages.size(); j++) {
                    if (Math.abs(original.get(i) - newAverages.get(j).average) < min) {
                        min = Math.abs(original.get(i) - newAverages.get(j).average);
                        index = j;
                    }
                }
                newAverages.get(index).relatedNumbers.add(original.get(i));
            }

            // checking if newAverages and avgs are equal
            boolean equals = true;
            for (int i = 0; i < avgs.size(); i++) {
                Collections.sort(avgs.get(i).relatedNumbers);
                Collections.sort(newAverages.get(i).relatedNumbers);
                if (!avgs.get(i).relatedNumbers.equals(newAverages.get(i).relatedNumbers)) {
                    equals = false;
                    break;
                }
            }
            if (equals) {
                // building the final table (the range of each of the final averages)
                for (int i = 0; i < avgs.size(); i++) {
                    quantizer.add(new q(Collections.min(avgs.get(i).relatedNumbers), Collections.max(avgs.get(i).relatedNumbers), i, (int) avgs.get(i).average));
                }
                return;
            }
            // making the old one equals the new... repeat!
            avgs.clear();
            avgs.addAll(newAverages);
        }
    }

    public void decompress() throws FileNotFoundException {
        Scanner in1 = new Scanner(new File(QtablePath));
        Scanner in2 = new Scanner(new File(QimagePath));
        int w, h, bit;
        ArrayList<Integer> q = new ArrayList<>();

        // reading information from the table
        bit = in1.nextInt();
        w = in1.nextInt();
        h = in1.nextInt();
        int[][] p = new int[w][h];
        for (int i = 0; i < Math.pow(2, bit); i++) {
            q.add(in1.nextInt());
        }

        // reading every pixel in the compressed image, assigning its average according to the ranges (table)
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int x = in2.nextInt();
                p[i][j] = q.get(x);
            }
        }
        // and voila!
        writeImage(p, DecompressedImgagePath, w, h);
    }

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }


    // gui starts here

    Button browse, compress, decompress;


    private void openFile(File file) throws IOException {
        ProcessBuilder processbuilder = new ProcessBuilder("open", file.getAbsolutePath());
        Process p = processbuilder.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Scalar Quantization Tool"); // window title

        javafx.scene.control.Label home = new javafx.scene.control.Label("Choose file to compress/decompress:");
        javafx.scene.control.Label enterbit = new javafx.scene.control.Label("Enter bit level:");

        javafx.scene.control.TextField textField = new javafx.scene.control.TextField(); // text field to be filled with path
        textField.setMaxWidth(200);
        javafx.scene.control.TextField textField2 = new javafx.scene.control.TextField(); // text field to be filled with path
        textField2.setMaxWidth(200);

        browse = new Button("Browse");
        browse.setMaxSize(100, 100);

        browse.setOnAction(event -> {
            FileChooser choose1 = new FileChooser();
            File file = choose1.showOpenDialog(primaryStage);
            textField.setText(file.getAbsolutePath());
        });

        VBox layout1 = new VBox(20); // vbox is a layout that puts things vertically
        layout1.setAlignment(Pos.CENTER); // align everything in the center

        compress = new Button("Compress");
        compress.setMaxSize(100, 100);
        decompress = new Button("Decompress");
        decompress.setMaxSize(100, 100);

        compress.setOnAction(event -> {
            if (textField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please choose a file.");
                alert.show();
            } else {
                bitLevel = Integer.parseInt(textField2.getText());
                try {
                    compress(textField.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    openFile(new File(QtablePath)); // open compressed file
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        decompress.setOnAction(e -> {
            try {
                decompress();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                openFile(new File(DecompressedImgagePath)); // open decompressed file
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        layout1.getChildren().addAll(home, textField, enterbit, textField2, browse, compress, decompress); // add all the buttons to window
        Scene scene = new Scene(layout1, 300, 350); // window dimensions
        layout1.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHEAT, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY))); // gainsboro - wheat

        primaryStage.setScene(scene);
        primaryStage.show(); // most imp line lol

    }
}

// 1 2 5 3 4 6 10 12 9 8
// 6 15 17 60 100 90 66 59 18 3 5 16 14 67 63 2 98 92