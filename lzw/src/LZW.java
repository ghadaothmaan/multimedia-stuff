/*
 * multimedia assignment lzw compression and decompression with gui
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.awt.*;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;



public class LZW extends Application {

    public static File compress(String path) throws FileNotFoundException {

//        String string = "ABAABABBAABAABAAAABABBBBBBBB";
//        string += " *"; // a workaround that failed to succeed... shame!

        String string = new Scanner(new File(path)).useDelimiter("\\Z").next(); // reads from file
        File f = new File("/Users/gee/IdeaProjects/multimedia_lzw/src/out.txt");
        PrintWriter out = new PrintWriter(f);
        // File f = new File();
        String tag = "", next = "";
        int index = 128; // first empty place in dictionary
        Object n;
        Map dictionary = new HashMap();

        dictionary.put("A", 65);
        dictionary.put("B", 66);

        for (int i = 0; i < string.length(); i++) {
            next += string.charAt(i);

            if (tag.length() == 1) // if a new character appears
                dictionary.put(tag,(int) tag.charAt(0)); // convert it to its ascii code

            if (dictionary.containsKey(tag + next)) { // if tag + next exist in dictionary
                tag += next; // update tag
                next = ""; // clear next for next iteration
            }

            else { // else if not exist
                dictionary.put(tag + next, index); // add tag + next to dictionary with its index
//                System.out.println(tag + next + " " + index); // uncomment to printout the dictionary to std output
//                out.println(tag + next + " " + index); // uncomment to printout the dictionary to file
                index++;
                n = dictionary.get(tag);
                out.println(n); // System.out.println(n);
                tag = next;
                next = "";
            }
            if (i == string.length() - 1) // at the last iteration it finds tag bs doesn't print it
                out.println(dictionary.get(tag)); // since the print fn is in the else case of not finding tag

        }
        out.close();
        return f;
//        System.out.println(n);
//        System.out.println(dictionary); // u gotta be kidding me 3omri eli daa3 :))
    }

    public static File decompress(String path) throws FileNotFoundException {
//        read index in dictionary
//        and write it in original
//        read index in dictionary
//        if found --> add to dictionary all symbols in previous step + 1st symbol in current step
//        if !found --> add to dictionary all symbols in previous step + 1st symbol in previous step
        String original = "";
        Scanner in = new Scanner(new File(path));
        File f = new File("/Users/gee/IdeaProjects/multimedia_lzw/src/original.txt");
        PrintWriter out = new PrintWriter(f);

        Map<Integer, String> dictionary = new HashMap<Integer, String>();

        Vector<Integer> v = new Vector<>();

        int index = 128;

        dictionary.put(65, "A"); // key value
        dictionary.put(66, "B");

        try {
            while (in.hasNext()) {
//                reads a line from file then pushes it back in v and loops over it (t7t) starting from index 1
                v.addElement(Integer.parseInt(in.nextLine()));
            }
        } finally {
            in.close();
        }
        original += dictionary.get(v.get(0)); // adds first symbol at index 0

        for (int i = 1; i < v.size(); i++) { // loops over v starting from index 1
            if (dictionary.containsKey(v.get(i))) {
                original += dictionary.get(v.get(i));
//                out.print(dictionary.get(v.get(i)));
                dictionary.put(index, dictionary.get(v.get(i - 1)) + dictionary.get(v.get(i)).charAt(0));
                index++;
            } else {
                dictionary.put(index, dictionary.get(v.get(i - 1)) + dictionary.get(v.get(i - 1)).charAt(0));
                original += dictionary.get(v.get(i));
//                out.print(dictionary.get(v.get(i)));
                index++;
            }
        }
        out.print(original); // System.out.println(original);
        out.close();
        return f;
    }


//  gui starts here

    Button browse, compress, decompress;

    private Desktop desktop = Desktop.getDesktop();

    private void openFile(File file) throws IOException {
//        System.out.println(file.getAbsolutePath());
        ProcessBuilder processbuilder = new ProcessBuilder("open", file.getAbsolutePath());
        Process p = processbuilder.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // main
        primaryStage.setTitle("LZW Compressor/Decompressor Tool"); // window title

        Label home = new Label("Choose file to compress/decompress:");

        TextField textfield = new TextField(); // text field to be filled with path
        textfield.setMaxWidth(200);

        browse = new Button("Browse");
        browse.setMaxSize(100,100);

        FileChooser choose = new FileChooser();

        browse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt"); // only choose text files
                choose.getExtensionFilters().add(extFilter);

                File file = choose.showOpenDialog(primaryStage);
                textfield.setText(file.getAbsolutePath()); // text field now full with path

            }
        });

        VBox layout1  = new VBox(20); //vbox is a layout that puts things vertically
        layout1.setAlignment(Pos.CENTER); // align everything in the center obvs
        // StackPane layout1 = new StackPane();
        compress = new Button("Compress");
        compress.setMaxSize(100,100);
        decompress = new Button("Decompress");
        decompress.setMaxSize(100,100);

        compress.setOnAction(e -> {
            if (textfield.getCharacters().toString().isEmpty()) { // FILE NOT CHOSEN
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please choose a file.");
                alert.show();
            }
            else {
                File o = null; // compress file
                try {
                    o = compress(textfield.getText());
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
            if(textfield.getCharacters().toString().isEmpty()) { // FILE NOT CHOSEN
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please choose a file.");
                alert.show();
            }
            else {
                File d = null; // decompress chosen file //textfield.getText()
                try {
                    d = decompress(textfield.getText());
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                try {
                    openFile(d); // open decompressed file
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        layout1.getChildren().addAll(home, textfield, browse, compress, decompress); // add all the buttons to window
        Scene scene = new Scene(layout1, 300, 350); // window dimensions
        layout1.setBackground(new Background(new BackgroundFill(Color.WHEAT, CornerRadii.EMPTY, Insets.EMPTY))); // gainsboro

        primaryStage.setScene(scene);
        primaryStage.show(); // most imp line lol

    }

    public static void main(String[] args) throws FileNotFoundException {
//        compress("/Users/gee/IdeaProjects/multimedia_lzw/src/in.txt");
//        decompress("/Users/gee/IdeaProjects/multimedia_lzw/src/tags.txt");
        launch(args);
    }
}
