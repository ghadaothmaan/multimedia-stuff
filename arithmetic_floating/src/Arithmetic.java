/*
 * multimedia assignment floating arithmetic compression and decompression with gui
 */

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.Label;
import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

public class Arithmetic extends Application {

    public static String inputFilePath = "/Users/gee/IdeaProjects/mm_arithmetic_floating/src/in.txt";
    public static String outputFilePath = "/Users/gee/IdeaProjects/mm_arithmetic_floating/src/out.txt";
    public static String probsFilePath = "/Users/gee/IdeaProjects/mm_arithmetic_floating/src/probabilities.txt";
    public static String originalFilePath = "/Users/gee/IdeaProjects/mm_arithmetic_floating/src/original.txt";

    public static class Probability {
        double low, high;

        Probability(double low, double high) {
            this.low = low;
            this.high = high;
        }
    }

    public static File compress(String path) throws IOException {
        File f = new File(probsFilePath);
        PrintWriter out = new PrintWriter(f);
        File file = new File(outputFilePath);
        PrintWriter output = new PrintWriter(file);

        String text = new Scanner(new File(path)).useDelimiter("\\Z").next(); // convert everything in file into one string

        HashMap<Character, Integer> symbolcount = new HashMap<>(); // symbol - count

        for (int i = 0; i < text.length(); i++) { // calculate frequency
            if (symbolcount.containsKey(text.charAt(i))) { // if found increment its number
                int prevcount = symbolcount.get(text.charAt(i));
                symbolcount.put(text.charAt(i), prevcount + 1);
            } else
                symbolcount.put(text.charAt(i), 1); // first occurrence of a character
        }

        HashMap<Character, Probability> whatever = new HashMap<>(); // symbol - low , high

        double low = 0, high;

        out.println(text.length()); // saving length of text for decompression

        for (HashMap.Entry<Character, Integer> element : symbolcount.entrySet()) {
            Character symbol = element.getKey();
            Integer freq = element.getValue();
            double prob = 1.0 * freq / text.length();
            high = low + prob;
            Probability probability = new Probability(low, high);
            whatever.put(symbol, probability);
            low = high;
            out.println(symbol + " " + probability.low + " " + probability.high); // printing out probabilities to probFilePath
        }

        double lower = 0, upper = 1, range = 1;

        for (int i = 0; i < text.length(); i++) {
            if (i == 0) {
                lower = whatever.get(text.charAt(i)).low;
                upper = whatever.get(text.charAt(i)).high;
                range = upper - lower; // for next symbol
            } else {
                upper = lower + range * whatever.get(text.charAt(i)).high;
                lower = lower + range * whatever.get(text.charAt(i)).low;
                range = upper - lower; // for next symbol
            }
        }

        double code = (lower + upper) / 2;
        output.println(code);

        output.close();
        out.close();
        return file;
    }


    public static File decompress(String path) throws IOException {
        String string = new Scanner(new File(path)).useDelimiter("/Z").next();

        File f = new File(originalFilePath);
        PrintWriter out = new PrintWriter(f);

        HashMap<Character, Probability> probabilities = new HashMap<>(); // symbol - low , high
        int textLength = 0;

        try (Scanner in = new Scanner(new File(probsFilePath))) {
            textLength = Integer.parseInt(in.next());
            while (in.hasNext()) { // reads input from probabilities file
                Character symbol = in.next().charAt(0);
                double low = Double.parseDouble(in.next());
                double high = Double.parseDouble(in.next());
                Probability probability = new Probability(low, high);
                probabilities.put(symbol, probability); // adds symbol - low , high
            }
        }

        String decompressed = "";

        double code = Double.parseDouble(string);
        double lower, upper, range;

        for (int i = 0; i < textLength; i++){  // looping over length
            for (HashMap.Entry<Character, Probability> element : probabilities.entrySet()) {
                lower = element.getValue().low;
                upper = element.getValue().high;
                range = upper - lower;
                if (lower < code && code < upper) { // if code lies b/w low and high
                    decompressed += element.getKey(); // add its symbol to decompressed
                    code = (code - lower) / range;
                    break;
                }
            }
        }

        out.println(decompressed);
        out.close();
        return f;
    }

    //    gui starts here

    Button browse, compress, decompress;

    private Desktop desktop = Desktop.getDesktop();

    private void openFile(File file) throws IOException {
        ProcessBuilder processbuilder = new ProcessBuilder("open", file.getAbsolutePath());
        Process p = processbuilder.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Arithmetic Compressor/Decompressor Tool"); // window title

        javafx.scene.control.Label home = new javafx.scene.control.Label("Choose file to compress/decompress:");

        javafx.scene.control.TextField textField = new javafx.scene.control.TextField(); // text field to be filled with path
        textField.setMaxWidth(200);

        browse = new Button("Browse");
        browse.setMaxSize(100,100);

        FileChooser choose = new FileChooser();
        browse.setOnAction(event -> {
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt"); // only choose text files
            choose.getExtensionFilters().add(extFilter);


            File file = choose.showOpenDialog(primaryStage);
            textField.setText(file.getAbsolutePath()); // text field now full with path
        });

        VBox layout1  = new VBox(20); // vbox is a layout that puts things vertically
        layout1.setAlignment(Pos.CENTER); // align everything in the center

        compress = new Button("Compress");
        compress.setMaxSize(100,100);
        decompress = new Button("Decompress");
        decompress.setMaxSize(100,100);

        compress.setOnAction(e -> {
            if (textField.getCharacters().toString().isEmpty()) { // FILE NOT CHOSEN
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please choose a file.");
                alert.show();
            }
            else {
                File o = null; // compress file
                try {
                    o = compress(textField.getText());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    openFile(o); // open compressed file
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        decompress.setOnAction(e -> {
            if(textField.getCharacters().toString().isEmpty()) { // FILE NOT CHOSEN
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please choose a file.");
                alert.show();
            }
            else {
                File d = null; // decompress chosen file
                try {
                    d = decompress(textField.getText());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    openFile(d); // open decompressed file
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        layout1.getChildren().addAll(home, textField, browse, compress, decompress); // add all the buttons to window
        Scene scene = new Scene(layout1, 300, 350); // window dimensions
        layout1.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.BEIGE, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY))); // gainsboro - wheat

        primaryStage.setScene(scene);
        primaryStage.show(); // most imp line lol

    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
