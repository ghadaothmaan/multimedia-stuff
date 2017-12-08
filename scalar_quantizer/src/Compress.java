import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Compress {

    private static int n = 0;
    private static int m = 0;
    private static ArrayList<Integer> data = new ArrayList<>();
    private static int bitsNumber = 0;
    private static ArrayList<Integer> dequantizedNum = new ArrayList<>();
    String originalPath = "";
    private JButton compressButton;
    private JButton decompressButton;
    private JTextField numberOfBitsTextField;
    private JTextField newPathTextField;
    private JTextField originalPathTextField;
    private JPanel Jpanel1;

    public Compress() {
        compressButton.addActionListener(e -> {
            bitsNumber = Integer.parseInt(numberOfBitsTextField.getText());
            originalPath = originalPathTextField.getText();
            try {
                compress(originalPath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "compressed!");

        });
        decompressButton.addActionListener(e -> {
            String newPath = newPathTextField.getText();
            try {
                Decompress.decompress(newPath);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "decompressed!");
            return;

        });
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Scalar Quantization");
        frame.setContentPane(new Compress().Jpanel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // takes data and puts it in 1D array and sorts it w btkteb el files
    // bakteb el m wl n el size bta3 el sora w 3ndi array ranges 3mlnah mn el dequantizenum
    // tol mna mashya wslt l range akbar mly ana feh fna 3ndi integer code el code da mn zero
    // l7ad el quantizenum fa ha7ot fl file en el raqam da hy get replaced by kza zy el example eli fl mo7adra
    // w b3den bakteb el array de f dequantizednum 3shan asta3mlo fl decompress
    public static void compress (String originalPath) throws IOException {

        ArrayList<Integer> ranges = new ArrayList<>();
        ArrayList<Integer> originalData = new ArrayList<>();
        File file = new File("out.txt");
        FileWriter fileWriter = new FileWriter(file);
        int[][] pixels = new int[n][m];

        pixels = readImage(originalPath);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                data.add(pixels[i][j]);
                originalData.add(pixels[i][j]);
            }

        Collections.sort(data);

        dequantizedSplit(0, 0, n * m);
        dequantize();

        for (int i = 0; i < dequantizedNum.size() - 1; i++)
            ranges.add((dequantizedNum.get(i) + dequantizedNum.get(i + 1)) / 2);

        fileWriter.append(Integer.toString(n));
        fileWriter.append(" ");
        fileWriter.append(Integer.toString(m));
        fileWriter.append("\n");

        for (int i = 0; i < originalData.size(); i++) {
            int code = 0;
            for (int j = 0; j < ranges.size(); j++)
                if (originalData.get(i) > ranges.get(j)) ++code;
            fileWriter.append(Integer.toString(code));
            fileWriter.append(" ");
        }

        fileWriter.append("\n");

        for (int i = 0; i < dequantizedNum.size(); i++) {
            fileWriter.append(Integer.toString(dequantizedNum.get(i)));
            if (i < dequantizedNum.size() - 1) fileWriter.append("\n");
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
        m = width;
        n = height;
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

    // de recursive kol call ha3ady 3aleha hakhod goz2 ml data bta3ty wl data de ha split it nosen ageb el average y3ni wa split
    // el average da b3d kda ha associate wna b3ml kda hakhod brdo el portion el soghyr eli 3mltlo associate da w ha split it nosen
    // ezai bakhod el portion el soghyr da? el data 3ndi sorted w bashof mn fen l7d fen hynfa3 aktar m3 el left split
    // wl ba2y aked hwa da eli hynfa3 m3 el right split w lma anady el call eli b3deh hashof hnady mn fen l fen
    // wa7seb el average bta3o wa3mlo split whakaza
    public static void dequantizedSplit(int level, int stIdx, int edIdx) {
        int avg = 0;
        for (int i = stIdx; i < edIdx; i++)
            avg += data.get(i);

        if (edIdx - stIdx > 0)
            avg /= (edIdx - stIdx);

        if (level == bitsNumber) {
            dequantizedNum.add(avg);
            return;
        }

        int left = avg, right = avg + 1;

        for (int i = 0; i < data.size(); i++)
            if (Math.abs(data.get(i) - left) > Math.abs(data.get(i) - right)) {
                dequantizedSplit(level + 1, stIdx, i);
                dequantizedSplit(level + 1, i, edIdx);
                break;
            }
    }
    // b3d makhalst el dequantizesplit ba2a 3ndi array esmo dequantizednum feh el averages eli fl akher khales
    // el data lsa 3ndi sorted ba loop 3la el array of data 3adi w bafdal wa2fa 3nd awl index fl dequantizednum
    // w bashof el index eli gambo fana bashof el next bta3y hal eli m3aya ynfa3 yt7at m3aya wla yt7at
    // m3 el next bta3y f once eno ba2a a7san m3 el next bta3y f kda khalas kda ba2a m3aya portion of nums brdo
    // mmkn agblhom el avg 3adi f ba7seb el avg tol mana mashya msh zay el tanya el recursive de wb update law me7taga update
    // kol iteration 3ndi bazawed f integer average el data eli 3adet 3alya once en ana wa2aft khalas y3ni once en ana
    // la2et en eli b3dy el raqm eli m3aya da yosta7san yb2a m3 el dequantizednum eli b3di f bawa2af khalas wsafar el avg
    // w law el avg hwa hwa el avg eli fat ha count++ w lma el counter da ywsal ll 3add eli ana 3yzah eli matlob y3ni
    // bawa2af khalas m3na kda en ana 3ndy arqam sabten mtghyarosh ml iteration eli fatt
    public static void dequantize() {
        while (true) {
            int j = 0;
            int avg = 0;
            int prev = 0;
            int cnt = 0;
            for (int i = 0; i < data.size(); i++) {
                // j da el index eli ba loop beh fl dequantizednum
                if (j + 1 < dequantizedNum.size() && Math.abs(data.get(i) - dequantizedNum.get(j)) > Math.abs(data.get(i) - dequantizedNum.get(j + 1))) {
                    avg /= (i - prev);
                    prev = i;
                    if (dequantizedNum.get(j) == avg) ++cnt;
                    else dequantizedNum.set(j, avg);
                    ++j;
                    avg = 0;
                }
                avg += data.get(i);
            }

            avg /= (data.size() - prev);
            if (dequantizedNum.get(dequantizedNum.size() - 1) == avg) ++cnt;
            else dequantizedNum.set(dequantizedNum.size() - 1, avg);

            if (cnt == dequantizedNum.size()) break;
        }
    }
}


