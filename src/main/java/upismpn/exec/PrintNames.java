package upismpn.exec;


import upismpn.obrada.UceniciGroup;
import upismpn.obrada.UceniciGroupBuilder;
import upismpn.obrada.UcenikWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by luka on 2.2.17..
 */
public class PrintNames {

    public static void strings() {
        Set<String> sifreSmerova = new HashSet<>(), naziviOS = new HashSet<>(), naziviSS = new HashSet<>(),
                mestoOS = new HashSet<>(), mestoSS = new HashSet<>(), okruzi = new HashSet<>(),
                podrucjaRada = new HashSet<>(), naziviSmerova = new HashSet<>();
        UceniciGroup all = new UceniciGroupBuilder(null).getGroup();
        all.forEach(uc -> {
            UcenikWrapper.SrednjaSkola smer = uc.upisanaSkola;
            UcenikWrapper.OsnovnaSkola os = uc.osInfo;
            sifreSmerova.add(smer.sifra);
            naziviOS.add(os.ime);
            naziviSS.add(smer.ime);
            mestoOS.add(os.mesto);
            mestoSS.add(smer.mesto);
            okruzi.add(os.okrug);
            podrucjaRada.add(smer.podrucje);
            naziviSmerova.add(cleanSmer(smer.smer));
        });
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("/data/Shared/Projects/UpisDesktop/res/suggestions"));
            for(String s : sifreSmerova) out.write(s.toUpperCase() + "\n");
            out.write("-----\n");
            for(String s : naziviOS) out.write(capitalize(s) + "\n");
            out.write("-----\n");
            for(String s : naziviSS) out.write(capitalize(s) + "\n");
            out.write("-----\n");
            for(String s : mestoOS) out.write(capitalize(s) + "\n");
            out.write("-----\n");
            for(String s : mestoSS) out.write(capitalize(s) + "\n");
            out.write("-----\n");
            for(String s : okruzi) out.write(capitalize(s) + "\n");
            out.write("-----\n");
            for(String s : podrucjaRada) out.write(s.substring(0,1).toUpperCase() + s.substring(1) + "\n");
            out.write("-----\n");
            for(String s : naziviSmerova) out.write(s.substring(0,1).toUpperCase() + s.substring(1) + "\n");
            out.write("-----\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeStrings() {
        File sugg15 = new File("/data/Shared/Projects/UpisDesktop/res/suggestions15");
        File sugg16 = new File("/data/Shared/Projects/UpisDesktop/res/suggestions");
        try {
            List<String> lines15 = Files.readAllLines(sugg15.toPath());
            List<String> lines16 = Files.readAllLines(sugg16.toPath());
            List<Set<String>> merged = new ArrayList<>();
            HashSet<String> current = new HashSet<>();
            for (String line : lines15) {
                if (line.startsWith("--")) {
                    merged.add(current);
                    current = new HashSet<>();
                } else {
                    current.add(line);
                }
            }
            int i=0;
            for (String line : lines16) {
                if (line.startsWith("--")) {
                    i++;
                } else {
                    merged.get(i).add(line);
                }
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter("/data/Shared/Projects/UpisDesktop/res/suggestions.all"));
            for(Set<String> category : merged) {
                for(String item : category) {
                    bw.write(item);
                    bw.write("\n");
                }
                bw.write("---\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String capitalize(String str) {
        char[] chars = str.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\"') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    private static String cleanSmer(String smer) {
        if(smer.endsWith(" odluka okružne upisne komisije doneta na osnovu: - zakona o osnovama sistema obrazovanja i vaspitanja (\"službeni glasnik rs\"")) {
            return smer.substring(0, smer.length()-" odluka okružne upisne komisije doneta na osnovu: - zakona o osnovama sistema obrazovanja i vaspitanja (\"službeni glasnik rs\"".length());
        } else {
            return smer;
        }
    }
}
