package rs.lukaj.upisstats.scraper.obrada2017;

import java.util.HashMap;
import java.util.Map;

public class LetterUtils {
    private static final Map<Character, String> toLatin = new HashMap<>();

    static {
        toLatin.put('љ', "lj");
        toLatin.put('њ', "nj");
        toLatin.put('е', "e");
        toLatin.put('р', "r");
        toLatin.put('т', "t");
        toLatin.put('з', "z");
        toLatin.put('у', "u");
        toLatin.put('и', "i");
        toLatin.put('о', "o");
        toLatin.put('п', "p");
        toLatin.put('ш', "š");
        toLatin.put('ђ', "đ");
        toLatin.put('а', "a");
        toLatin.put('с', "s");
        toLatin.put('д', "d");
        toLatin.put('ф', "f");
        toLatin.put('г', "g");
        toLatin.put('х', "h");
        toLatin.put('ј', "j");
        toLatin.put('к', "k");
        toLatin.put('л', "l");
        toLatin.put('ч', "č");
        toLatin.put('ћ', "ć");
        toLatin.put('ж', "ž");
        toLatin.put('џ', "dž");
        toLatin.put('ц', "c");
        toLatin.put('в', "v");
        toLatin.put('б', "b");
        toLatin.put('н', "n");
        toLatin.put('м', "m");
    }

    public static String toLatin(String word) {
        word = word.toLowerCase();
        StringBuilder latin = new StringBuilder();
        for(int i=0; i<word.length(); i++) {
            char c = word.charAt(i);
            if(toLatin.containsKey(c))
                latin.append(toLatin.get(c));
            else
                latin.append(c);
        }
        return latin.toString();
    }
}
