import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/*
 * multimedia assignment scalar quantization compression and decompression with gui
 */

public class Quantizer {

    public static class Tree {
        Node root;
        Tree(int a, ArrayList<Integer> originalnums) {
            this.root = new Node(a, originalnums);
        }
    }

    public static class Node {
        int num;
        Node left; Node right;
        ArrayList<Integer> relatedNumbers = new ArrayList<>();

        Node(int n) {
            this.num = n;
        }

        Node (int n, ArrayList<Integer> nums) {
            this.num = n;
            this.relatedNumbers = nums;
        }
    }

    public static int[][] readImage(String filePath) {
        int width = 0;
        int height = 0;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        int[][] pixels = new int[height][width];

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
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

    public static File compress(String path) throws IOException {
        File f = new File(path);
        PrintWriter out = new PrintWriter(f);

        readImage("/Users/gee/IdeaProjects/multimedia_scalar_quantizer/src/originalLena.jpg");

        return f;
    }

    public static Tree quantize(ArrayList<Integer> original, int bits) {
        // calculate average
        int sum = 0;
        for (int i = 0; i < original.size(); i++) {
            sum += original.get(i);
        }
        int average = sum / original.size();
        Tree tree = new Tree(average, original);
        Node currentNode = tree.root;
        tree = constructTree(tree, currentNode, bits, average);
        return tree;
    }

    public static Tree constructTree(Tree t, Node n, int bits, int num) {
        int left  = num - 1; int right = num + 1;
        ArrayList<Integer> arrayleft = new ArrayList<>();
        ArrayList<Integer> arrayright = new ArrayList<>();
        ArrayList<Integer> original = t.root.relatedNumbers;
        ArrayList<Node> temp = new ArrayList<>();
        int sumleft = 0; int sumright = 0;
        for (int i = 0; i < original.size(); i++) {

            if((Math.abs(original.get(i) - left) < Math.abs(original.get(i) - right))) {
                arrayleft.add(original.get(i));
                sumleft += original.get(i);
            }
            else {
                arrayright.add(original.get(i));
                sumright += original.get(i);
            }
        }

        n.left = new Node(left, arrayleft);
        n.right = new Node(right, arrayright);
        bits--;

        if (bits == 0) {
            return t;
        }
        else {
            constructTree(t, n.left, bits, sumleft/arrayleft.size());
            constructTree(t, n.right, bits, sumright/arrayright.size());
        }
        return t;
    }

    public static void printTree(Tree t) {
        Queue<Node> q = new LinkedList<>();
        q.add(t.root);
        Node n;
        while (!q.isEmpty()) {
            n = q.remove();
            if (n.left != null) {
                System.out.println(n.left.num);
                for (int i = 0; i < n.left.relatedNumbers.size(); i++) {
                    System.out.print(n.left.relatedNumbers.get(i) + " ");
                }
                System.out.println();
                q.add(n.left);
            }

            if (n.right != null) {
                System.out.println(n.right.num);
                for (int i = 0; i < n.right.relatedNumbers.size(); i++) {
                    System.out.print(n.right.relatedNumbers.get(i) + " ");
                }
                System.out.println();
                q.add(n.right);
            }
        }
    }

    public static void main(String[] args) {
        int n; // how many numbers
        Scanner cin = new Scanner(System.in);
        n = Integer.parseInt(cin.next());
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            numbers.add(Integer.parseInt(cin.next()));
        }
        System.out.printf("How many bits: ");
        int bits = Integer.parseInt(cin.next());
        Tree t = quantize(numbers, bits);
        printTree(t);
    }
}
// 1 2 5 3 4 6 10 12 9 8
// 6 15 17 60 100 90 66 59 18 3 5 16 14 67 63 2 98 92