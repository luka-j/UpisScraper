package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.misc.Osnovna;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by luka on 9.2.2018.
 */
public class OsnovneBase {
    private static Map<String, List<Osnovna>> base;

    public static void load() {
        base = new HashMap<>();
        File f = new File(DownloadController.DATA_FOLDER, "osnovne");
        try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            String[] osnovne = text.split("\\n\\$\\n");
            for(String os : osnovne) {
                Osnovna osnovna = new Osnovna(os);
                List<Osnovna> mesta = base.computeIfAbsent(osnovna.getIme() + "+" + osnovna.getOkrug(),
                        k -> new ArrayList<>(6));
                mesta.add(osnovna);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Osnovna> getOsnovne(String ime, String okrug) {
        if(base.isEmpty()) load();
        return base.get(ime + "+" + okrug);
    }
}
