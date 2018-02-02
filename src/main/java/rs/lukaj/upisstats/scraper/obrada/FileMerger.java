package rs.lukaj.upisstats.scraper.obrada;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class FileMerger {

    private static final String DELIMETER = "$";
    public static final String FILENAME = "base";

    public static void mergeToOne(File folder) {
        File[] files = folder.listFiles();
        List<String> lines = new LinkedList<>();
        System.out.println("ucitavam fajlove");
        for (File file : files) {
            if (file.isFile() && file.getName().chars().allMatch(Character::isDigit)) {
                try {
                    lines.add(file.getName());
                    lines.addAll(Files.readAllLines(file.toPath()));
                    lines.add(DELIMETER);
                } catch (IOException ex) {
                    Logger.getLogger(FileMerger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("ucitao sve; ispisujem");
        File dest = new File(folder, FILENAME);
        try (Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), "UTF-8"))) {
            for (String line : lines) {
                bw.append(line).append("\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(FileMerger.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("gotovo.");
    }

    /**
     * Vraća listu učenika u formatu u kom su sačuvani u fajlu
     * @param db
     * @return
     */
    public static List<String> readFromOne(File db) {
        List<String> ret = new LinkedList<>();
        StringBuilder curr = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(db.toPath(), Charset.forName("UTF-8"));
            lines.forEach((line) -> {
                if (!line.equals(DELIMETER)) {
                    curr.append(line).append("\n");
                } else {
                    ret.add(curr.toString());
                    curr.delete(0, curr.length());
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(FileMerger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
