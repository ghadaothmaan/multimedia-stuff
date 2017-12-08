import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Decompress {

    public static void decompress (String newPath) throws FileNotFoundException {

        File file = new File("/Users/gee/IdeaProjects/mm_vector_quantizer/out.txt");
        Scanner scanner = new Scanner(file);

        int nPicture = scanner.nextInt();
        int mPicture = scanner.nextInt();
        ArrayList <Integer> compressedData = new ArrayList<>();
        int compressedDataSize = scanner.nextInt();

        for (int i = 0; i < compressedDataSize; i++)
            compressedData.add(scanner.nextInt());
        int codeBookSize = scanner.nextInt();
        int nVector = scanner.nextInt();
        int mVector = scanner.nextInt();
        int row = (int) Math.ceil((nPicture*1.0)/(nVector*1.0));
        int col = (int) Math.ceil((mPicture*1.0)/(mVector*1.0));

        ArrayList <ArrayList<ArrayList<Integer>>> vectors = new ArrayList<>();
        int[][] pixels = new int[nPicture][mPicture];

        for (int i = 0; i < codeBookSize; i++){
            ArrayList <ArrayList<Integer>> subVector = new ArrayList<>();
            for (int j = 0; j < nVector; j++){
                subVector.add(new ArrayList<>());
                for (int k = 0; k < mVector; k++){
                    subVector.get(j).add(scanner.nextInt());
                }
            }
            vectors.add(subVector);
        }

        int cnt = 0;
        int pixelRow = 0;
        for (int n = 0; n < row; n++){
            int pixelCol = 0;
            for (int m = 0; m < col; m++){
                ArrayList <ArrayList<Integer>> subVector = vectors.get(compressedData.get(cnt));
                ++cnt;
                int nRow = 0, nCol;
                for (int i = pixelRow; i < pixelRow+nVector; i++){
                    nCol = 0;
                    for (int j = pixelCol; j < pixelCol+mVector; j++){
                        if (i < nPicture && j < mPicture)
                            pixels[i][j] = subVector.get(nRow).get(nCol);
                        ++nCol;
                    }
                    ++nRow;
                }
                pixelCol += mVector;
            }
            pixelRow += nVector;
        }
        writeImage(pixels, newPath, nPicture, mPicture);
    }

    public static void writeImage(int[][] pixels,String outputFilePath,int height,int width) {

        File fileout = new File(outputFilePath);
        BufferedImage image2 = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB );

        for(int x = 0; x < width ;x++) {
            for(int y = 0; y < height;y++) {
                image2.setRGB(x,y,(pixels[y][x]<<16)|(pixels[y][x]<<8)|(pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image2, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
