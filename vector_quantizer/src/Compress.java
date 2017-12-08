import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Compress {
    static int nPicture;
    static int mPicture;
    static int nVector;
    static int mVector;
    static int vectorNum;
    private JPanel Jpanel1;
    private JTextField numberOfVectorsTextField;
    private JTextField numberOfVectorColumnsTextField;
    private JTextField numberOfVectorRowsTextField;
    private JTextField originalPhotoPathTextField;
    private JButton compressButton;
    private JButton decompressButton;

    static class Pair {
        int first;
        int second;
        Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    public Compress() {
        compressButton.addActionListener(e -> {
            vectorNum = Integer.parseInt(numberOfVectorsTextField.getText());
            nVector = Integer.parseInt(numberOfVectorRowsTextField.getText());
            mVector = Integer.parseInt(numberOfVectorColumnsTextField.getText());
            try {
                compress(originalPhotoPathTextField.getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Compressed!");
        });
        decompressButton.addActionListener(e -> {
            try {
                Decompress.decompress("new"+originalPhotoPathTextField.getText());
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Decompressed!");
        });
    }

    public static void main(String[] args) throws IOException {

        JFrame frame = new JFrame("Vector Quantization");
        frame.setContentPane(new Compress().Jpanel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void compress(String originalPath) throws IOException {

        int[][] pixels = new int[nPicture][mPicture];
        ArrayList<ArrayList<ArrayList<Integer>>> vectors = new ArrayList<>();
        pixels = readImage(originalPath);

        for (int i = 0; i < nPicture; i += nVector) {
            for (int j = 0; j < mPicture; j += mVector) {
                ArrayList<ArrayList<Integer>> subVector = new ArrayList<>();
                int cnt = 0;
                for (int k = i; k < i + nVector; k++) {
                    subVector.add(new ArrayList<>());
                    for (int m = j; m < j + mVector; m++) {
                        if (k >= nPicture || m >= mPicture)
                            subVector.get(cnt).add(0);
                        else subVector.get(cnt).add(pixels[k][m]);
                    }
                    ++cnt;
                }
                vectors.add(subVector);
            }
        }

        dequantizedSplit(vectors);
        System.out.println("yay compress end!");
        Decompress.decompress("newLena.jpg");
    }

    public static void dequantizedSplit(ArrayList<ArrayList<ArrayList<Integer>>> vectors) throws IOException {

        ArrayList<ArrayList<ArrayList<Integer>>> splitVector;
        ArrayList<ArrayList<ArrayList<Integer>>> avgVector;
        ArrayList<ArrayList<ArrayList<Integer>>> root = new ArrayList<>();
        ArrayList <Pair> finalAssignments;
        File file = new File("out.txt");
        FileWriter fileWriter = new FileWriter(file);
        int level = 0;

        // making root
        ArrayList <ArrayList <Integer>> avg = new ArrayList<>();
        for (int i = 0; i < nVector; i++){
            avg.add(new ArrayList<>());
            for (int j = 0; j < mVector; j++)
                avg.get(i).add(0);
        }
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < nVector; j++) {
                for (int k = 0; k < mVector; k++) {
                    avg.get(j).set(k, avg.get(j).get(k) + vectors.get(i).get(j).get(k));
                }
            }
        }

        for (int i = 0; i < nVector; i++) {
            for (int j = 0; j < mVector; j++) {
                avg.get(i).set(j, avg.get(i).get(j)/vectors.size());
            }
        }

        root.add(avg);

        // splitting
        while (level < vectorNum) {
            // splitting
            splitVector = new ArrayList<>();
            for (int k = 0; k < root.size(); k++) {
                // first one is the same
                splitVector.add(root.get(k));

                // second one = first + 1
                ArrayList<ArrayList<Integer>> subArray = new ArrayList<>();
                for (int i = 0; i < nVector; i++) {
                    subArray.add(new ArrayList<>());
                    for (int j = 0; j < mVector; j++) {
                        subArray.get(i).add(root.get(k).get(i).get(j) + 1);
                    }
                }
                splitVector.add(subArray);
            }

            // initialize counter
            ArrayList<Integer> counter = new ArrayList<>();
            for (int i = 0; i < splitVector.size(); i++)
                counter.add(0);

            // initialize avg vector
            avgVector = new ArrayList<>();
            for (int i = 0; i < splitVector.size(); i++) {
                avgVector.add(new ArrayList<>());
                for (int j = 0; j < nVector; j++) {
                    avgVector.get(i).add(new ArrayList<>());
                    for (int k = 0; k < mVector; k++)
                        avgVector.get(i).get(j).add(0);
                }
            }

            // calculate avg
            for (int i = 0; i < vectors.size(); i++) {

                // pick nearest value for each vector
                int min = 100000000;
                int minIdx = -1;
                for (int j = 0; j < splitVector.size(); j++) {
                    int tmp = 0;
                    for (int k = 0; k < nVector; k++)
                        for (int m = 0; m < mVector; m++)
                            tmp += Math.abs(vectors.get(i).get(k).get(m) - splitVector.get(j).get(k).get(m));
                    if (tmp < min) {
                        min = tmp;
                        minIdx = j;
                    }
                }
                // add counter
                counter.set(minIdx, counter.get(minIdx) + 1);
                // add the values together
                for (int j = 0; j < nVector; j++)
                    for (int k = 0; k < mVector; k++)
                        avgVector.get(minIdx).get(j).set(k, avgVector.get(minIdx).get(j).get(k) + vectors.get(i).get(j).get(k));
            }

            // get real avg
            for (int i = 0; i < avgVector.size(); i++)
                for (int j = 0; j < nVector; j++)
                    for (int k = 0; k < mVector; k++)
                        if (counter.get(i) > 0)
                        avgVector.get(i).get(j).set(k, avgVector.get(i).get(j).get(k) / (counter.get(i)));
            ++level;
            root = avgVector;
        }

        while (true) {
            finalAssignments = new ArrayList<>();

            // initialize counter
            ArrayList<Integer> counter = new ArrayList<>();
            for (int i = 0; i < root.size(); i++)
                counter.add(0);

            // initialize avg vector
            avgVector = new ArrayList<>();
            for (int i = 0; i < root.size(); i++) {
                avgVector.add(new ArrayList<>());
                for (int j = 0; j < nVector; j++) {
                    avgVector.get(i).add(new ArrayList<>());
                    for (int k = 0; k < mVector; k++)
                        avgVector.get(i).get(j).add(0);
                }
            }

            // calculate avg
            for (int i = 0; i < vectors.size(); i++) {
                // pick nearest value for each vector
                int min = 100000000;
                int minIdx = -1;
                for (int j = 0; j < root.size(); j++) {
                    int tmp = 0;
                    for (int k = 0; k < nVector; k++)
                        for (int m = 0; m < mVector; m++)
                            tmp += Math.abs(vectors.get(i).get(k).get(m) - root.get(j).get(k).get(m));
                    if (tmp < min) {
                        min = tmp;
                        minIdx = j;
                    }
                }

                Pair pair = new Pair(i, minIdx);
                finalAssignments.add(pair);

                // add counter
                counter.set(minIdx, counter.get(minIdx) + 1);
                // add the values together
                for (int j = 0; j < nVector; j++)
                    for (int k = 0; k < mVector; k++)
                        avgVector.get(minIdx).get(j).set(k, avgVector.get(minIdx).get(j).get(k) + vectors.get(i).get(j).get(k));
            }
            // get real avg
            for (int i = 0; i < avgVector.size(); i++)
                for (int j = 0; j < nVector; j++)
                    for (int k = 0; k < mVector; k++)
                        if (counter.get(i) > 0)
                        avgVector.get(i).get(j).set(k, avgVector.get(i).get(j).get(k) / (counter.get(i)));

            boolean okay = true;
            for (int i = 0; i < root.size(); i++){
                for (int j = 0; j < nVector; j++){
                    for (int k = 0; k < mVector; k++){
                        if (!Objects.equals(root.get(i).get(j).get(k), avgVector.get(i).get(j).get(k))) okay = false;
                    }
                }
            }
            if (okay) break;
            root = avgVector;
        }

        fileWriter.append(Integer.toString(nPicture));
        fileWriter.append("\n");
        fileWriter.append(Integer.toString(mPicture));
        fileWriter.append("\n");
        fileWriter.append(Integer.toString(finalAssignments.size()));
        fileWriter.append("\n");
        for (int i = 0; i <  finalAssignments.size(); i++){
            fileWriter.append(Integer.toString(finalAssignments.get(i).second));
            fileWriter.append(" ");
        }
        fileWriter.append("\n");
        fileWriter.append(Integer.toString(root.size()));
        fileWriter.append("\n");
        fileWriter.append(Integer.toString(nVector));
        fileWriter.append("\n");
        fileWriter.append(Integer.toString(mVector));
        fileWriter.append("\n");

        for (int i = 0; i < root.size(); i++){
            for (int j = 0; j < nVector; j++){
                for (int k = 0; k < mVector; k++){
                    fileWriter.append(Integer.toString(root.get(i).get(j).get(k)));
                    fileWriter.append(" ");
                }
                fileWriter.append("\n");
            }
        }

        fileWriter.flush();
        fileWriter.close();
    }

    public static int[][] readImage(String filePath) {
        int width, height;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        mPicture = width;
        nPicture = height;
        int[][] pixels = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;

                pixels[y][x] = r;
            }
        }
        return pixels;
    }
}

/*
1 2 7 9 4 11
3 4 6 6 12 12
4 9 15 14 9 9
10 10 20 18 8 8
4 3 17 16 1 4
4 5 18 18 5 6
 */