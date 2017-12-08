/*
 * multimedia assignment lz77 compression and decompression
 */

import java.util.ArrayList;

public class lz77 {

    public static ArrayList<Tag> Compress(String s) {

        ArrayList<Tag> compressedTag = new ArrayList<Tag>();

        Tag t = new Tag(0,0,' ');

        for (int i = 0; i < s.length(); i++) {

            int j;
            String temp = "";

            for (j = i; j < s.length() - 1; j++) {

                temp += s.charAt(j);
                int pos = s.substring(0,j).lastIndexOf(temp);

                if (pos == -1) {
                    if (temp.length() == 1) {
                        t.offset = 0;
                        t.len = 0;
                        t.next = s.charAt(i);
                    }
                    break;

                } else {
                    t.next = s.charAt(j + 1);
                    t.offset = i - pos; //i = start of the lookahead window - hrg3 ad eh
                    t.len = temp.length();
                }
            }

            i = j; //el j kda eli tl3t ml loop wtla3et akbr match... la2et aba w 7atetha 3yza adawr 3la eli b3deha b2a
            compressedTag.add(t);

            System.out.println(t.offset + " " + t.len + " " + t.next); //printing here
        }

        return compressedTag;
    }

    public static String decompress(ArrayList<Tag> tags) {

        String original = "";

        for (int i = 0; i < tags.size(); i++) {

            int pos = tags.get(i).offset;
            int len = tags.get(i).len;
            char next = tags.get(i).next;

            //first occurrence of a character

            if (pos == 0 && len == 0) {
                original += next;

            } else {
                //go back p steps
                //take l characters and write them in the string
                //original += original.substring(i - pos, i - pos + len); //doesnt work string out of reach bc of overlap

                for (int j = original.length() - pos, k = 0; k < len; j++, k++) {
                    original += original.charAt(j);
                }

                original += next;
            }
        }

        System.out.println(original);
        return original;
    }

    public static void main(String[] args) {

        //abaababaabbbbbbbbbbbba 00a 00b 21a 32b 53b 110a
        //abaabababababababababa 00a 00b 21a 32b 214a

        //compression

        String s = "abaababaabbbbbbbbbbbba";
        ArrayList<Tag> letsee = new ArrayList<Tag>();

        letsee = Compress(s);

        //decompression

        ArrayList<Tag> tags = new ArrayList<Tag>();

        //input
/*
        Tag t = new Tag(0, 0, ' ');
        Scanner in = new Scanner(System.in);

        int x = 0;
        System.out.println("Enter number of tags: ");
        x = in.nextInt();
        for (int i = 0; i < x; i++) {
            System.out.println("Enter offset: ");
            t.offset = in.nextInt();
            in.nextLine();
            System.out.println("Enter length: ");
            t.len = in.nextInt();
            in.nextLine();
            System.out.println("Enter next character: ");
            t.next = in.next().charAt(0);
            tags.add(t);
        }
*/
        Tag t0 = new Tag(0,0,'a');
        Tag t1 = new Tag(0,0,'b');
        Tag t2 = new Tag(2,1,'a');
        Tag t3 = new Tag(3,2,'b');
        Tag t4 = new Tag(5,3,'b');
        Tag t5 = new Tag(1,10,'a');

        tags.add(t0);
        tags.add(t1);
        tags.add(t2);
        tags.add(t3);
        tags.add(t4);
        tags.add(t5);

        String wellsee = decompress(tags);

//        for(int i = 0; i < letsee.size(); i++) {
//            System.out.println(letsee.get(i).offset + " " + letsee.get(i).len + " " + letsee.get(i).next);
//        } //this outputs wrong for some reason hashofha b3den
    }
}
