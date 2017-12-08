/*
 * multimedia assignment standard huffman compression and decompression with gui
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.io.File;

public class Huffman extends Application {
    public static class node {
        String symbol;
        double freq;
        node left, right;
        node (String symbol, double freq) {
            this.symbol = symbol;
            this.freq = freq;
        }
    }

    public static void sort(ArrayList<node> list) {
        // returns 1 if rhs should be after lfs
        // returns -1 if lfs should be before rhs
        // returns 0 otherwise
        Collections.sort(list, (n1, n2) -> (n1.freq < n2.freq ? -1 : n1.freq == n2.freq ? 0 : 1));
    }

    public static String tablePath = "/Users/gee/IdeaProjects/multimedia_std_huffman/src/table.txt";

    public static HashMap<Character, String> table = new HashMap<>(); // symbol - code
    public static HashMap<String, node> adjlist = new HashMap<>(); // symbol - node (parent or child)


    public static void getcode(node node1, String code) { // FIX ME!!
        if (!adjlist.containsKey(node1.symbol)) { // if the list doesnt contain the symbol then its a one character symbol
            table.put(node1.symbol.charAt(0), code); // and it should add its code to the table
            return;
        }

        getcode(node1.left, code + "0");
        getcode(node1.right, code + "1");
    }

    public static File compress(String path) throws IOException {
        File f = new File(tablePath);
        PrintWriter out = new PrintWriter(f);

        File file = new File("/Users/gee/IdeaProjects/multimedia_std_huffman/src/output.txt");
        PrintWriter output = new PrintWriter(file);

        String text = "";
        text = new Scanner(new File(path)).useDelimiter("\\Z").next(); // convert everything in file into one string

        HashMap<Character, Integer> symbolcount = new HashMap<>(); // symbol - count

        for (int i = 0; i < text.length(); i++) { // calculate frequency
            if (symbolcount.containsKey(text.charAt(i))) { // if found increment its number
                int prevcount = symbolcount.get(text.charAt(i));
                symbolcount.put(text.charAt(i), prevcount + 1);
            } else
                symbolcount.put(text.charAt(i), 1); // first occurrence of a character
        }


        ArrayList<node> list = new ArrayList<>();

        for (HashMap.Entry<Character, Integer> element : symbolcount.entrySet()) {
            String symbol = element.getKey().toString();
            Integer freq = element.getValue();
            node node = new node(symbol, freq);
            list.add(node);
            // System.out.println(node.symbol + " " + node.freq); // printing out symbol and its frequency
        }

        while (list.size() > 1) { // stops when there's just 1 node
            sort(list);
            String symbol = list.get(0).symbol + list.get(1).symbol;
            double freq = list.get(0).freq + list.get(1).freq; // yarab aftekr de
            node n = new node(symbol, freq);
            n.left = list.get(0);
            n.right = list.get(1);
            list.remove(0);
            list.remove(0); // removing index 0 twice cuz im adding the first two elements and theres no need for them khalas
            list.add(n);
            adjlist.put(n.symbol, n);
            System.out.println(n.symbol + " " + n.left.symbol + " " + n.right.symbol);
        }

        getcode(adjlist.get(list.get(0).symbol), "");

        String string = "";

        for (HashMap.Entry<Character, String> element : table.entrySet()) { // writing table contents symbol - code on table
            out.println(element.getKey() + " " + element.getValue());
        }

        for (int i = 0; i < text.length(); i++) { // looping over text and figuring out the code for each character
            // System.out.println(text.charAt(i));
            string += table.get(text.charAt(i));
        }
        output.println(string);

        output.close();
        out.close();
        return file;
    }

    public static File decompress(String path) throws IOException {
        Scanner in = new Scanner(new File(tablePath));
        String string = new Scanner(new File(path)).useDelimiter("/Z").next();
        File f = new File("/Users/gee/IdeaProjects/multimedia_std_huffman/src/original.txt");
        PrintWriter out = new PrintWriter(f);

        HashMap<String, String> table = new HashMap<>(); // code - symbol

        try {
            while (in.hasNext()) { // reads input (1010001010101) from file
                String symbol = in.next();
                String code = in.next();
                table.put(code, symbol); // searches by code
            }
        } finally { in.close(); }

        String temp = "", decompressed = "";

        for (int i = 0; i < string.length(); i++){
            temp += string.charAt(i);
            if (table.containsKey(temp)) {
                decompressed += table.get(temp);
                temp = "";
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
        primaryStage.setTitle("Huffman Compressor/Decompressor Tool"); // window title

        Label home = new Label("Choose file to compress/decompress:");

        TextField textField = new TextField(); // text field to be filled with path
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
        layout1.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY))); // gainsboro - wheat

        primaryStage.setScene(scene);
        primaryStage.show(); // most imp line lol

    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}















/*
Collections.sort(list, new Comparator<pair>() {
    @Override
    public int compare(pair p1, pair p2) {
        return (p1.freq > p2.freq ? 1 : 0);
        }
});
*/
