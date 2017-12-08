/*
 * a class just to return the desired tag form
 */

public class Tag {

    int offset;
    int len;
    char next;

    Tag(int o, int l, char n) {
        this.offset = o;
        this.len = l;
        this.next = n;
    }
}
