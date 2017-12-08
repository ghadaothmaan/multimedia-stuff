import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class Decompress {

    public static void writeImage(int[][] pixels,String outputFilePath,int height,int width) {

        File fileout = new File(outputFilePath);

        BufferedImage image2 = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB );

        for(int x = 0; x < width ;x++) {
            for(int y = 0; y < height; y++) {
                image2.setRGB(x,y,(pixels[y][x] << 16)|(pixels[y][x] << 8)|(pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image2, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ba read kol el file byb2a feh el sora el size bta3ha el mxn kam y3ni byb2a mawgod feha
    // ba read el 7agat eli got replaced mn zero l7ad n w baqra2 el dequantizednum array da
    // wa3ml ml dequantizednum range tani brdo
    // wmsek kol index 3ndi mn el indices eli 3mltlhom replace fl awl de washof da zero m3na kda
    // eno mawgod f index el dequantizednum kza wa7at el dequantizednum da
    public static void decompress (String newPath) throws FileNotFoundException {

        int n, m;
        ArrayList <Integer> data = new ArrayList<>();
        ArrayList <Integer> dequantizeNum = new ArrayList<>();
        int[][] pixels;
        File file = new File("out.txt");
        Scanner scanner = new Scanner(file);

        n = scanner.nextInt();
        m = scanner.nextInt();

        for (int i = 0; i < n*m; i++)
            data.add(scanner.nextInt());

        while (scanner.hasNextLine())
            dequantizeNum.add(scanner.nextInt());

        pixels = new int[n][m];

        int row = 0;
        int col = 0;
        for (int i = 0; i < data.size(); i++){
            int code = data.get(i);
            pixels [row][col] = dequantizeNum.get(code);
            ++col;
            if (col == m){
                col = 0;
                ++row;
            }
        }
        writeImage(pixels, newPath, n, m);
    }
}
