package rs.lukaj.upisstats.scraper.utils;

/**
 * Because java.util.StringTokenizer apparently isn't supported anymore and has bugs such as
 * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4418160 which won't fix.
 *
 * A small, simple tokenizer which accepts only one character as a delimiter. The most efficient you can get.
 */
public class StringTokenizer {
    private char delimiter;
    private int pos = -1;
    private String str;
    private boolean retLastEmpty;

    public StringTokenizer(String str, char delim) {
        this(str, delim, false);
    }
    public StringTokenizer(String str, char delim, boolean retLastEmpty) {
        this.str = str;
        this.delimiter = delim;
        this.retLastEmpty = retLastEmpty;
    }

    public boolean hasMoreTokens() {
        if(str.isEmpty() && pos < 0) return retLastEmpty;

        if(retLastEmpty)
            return pos < str.length();
        else {
            if(pos < str.length() - 1) return true;
            else return str.charAt(pos) != delimiter;
        }
    }

    public void changeDelimiter(char newDelimiter) {
        this.delimiter = newDelimiter;
    }

    public String nextToken() {
        pos++;
        int start = pos;
        if(start > str.length())
            throw new IndexOutOfBoundsException("No more elements! String: " + str + ", delimiter " + String.valueOf(delimiter));
        if(start == str.length()) {
            if(retLastEmpty) return "";
            else throw new IndexOutOfBoundsException("No more elements! String: " + str + ", delimiter " + delimiter);
        }

        while(pos < str.length() && str.charAt(pos) != delimiter) pos++;

        if(start == pos) return "";

        return str.substring(start, pos);
    }
}
