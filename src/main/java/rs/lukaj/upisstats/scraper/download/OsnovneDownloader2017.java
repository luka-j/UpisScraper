package rs.lukaj.upisstats.scraper.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by luka on 3.7.17..
 */
public class OsnovneDownloader2017 {
    public static final String FILENAME = "osnovne";
    private static final File SAVEFILE = new File(DownloadController.DATA_FOLDER, "osnovneIds");

    private static OsnovneDownloader2017 instance;

    protected OsnovneDownloader2017() {}

    public static OsnovneDownloader2017 getInstance() {
        if(instance == null) instance = new OsnovneDownloader2017();
        return instance;
    }

    private final Set<Integer> base = new HashSet<>();

    public static File getDatafile() {
        return new File(DownloadController.DATA_FOLDER, FILENAME);
    }

    public void loadIdsFromFile() {
        if(!SAVEFILE.exists() || SAVEFILE.length()==0) return;
        try {
            List<String> ids = Files.readAllLines(SAVEFILE.toPath());
            for(String id :ids)
                base.add(Integer.parseInt(id));
            DownloadLogger.getLogger(DownloadLogger.OSNOVNE).log(DownloadLogger.Level.NORMAL, "Loaded " + ids.size() + "osnovnih (ids from file)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addOsnovna(int id) throws IOException {
        if(base.contains(id)) return;
        base.add(id);
        try(FileWriter fw = new FileWriter(SAVEFILE, true)) { //not the fastest one out there
            fw.append(String.valueOf(id)).append('\n');
        }
    }

    public void download() {
        List<Osnovna2017> osnovne = base.stream().map(id -> {
            Osnovna2017 osnovna = new Osnovna2017(id);
            osnovna.loadFromNet();
            osnovna.saveJson();
            return osnovna;
        }).collect(Collectors.toList());
        DownloadLogger.getLogger(DownloadLogger.OSNOVNE).log(DownloadLogger.Level.NORMAL, "Downloaded " + osnovne.size() + " osnovnih");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getDatafile()))) {
            getDatafile().createNewFile();
            osnovne.forEach(o -> {
                try {
                    bw.write(o.toCompactString() + "\n$\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            DownloadLogger.getLogger(DownloadLogger.OSNOVNE).log(DownloadLogger.Level.NORMAL, "Saved " + osnovne.size() + " osnovnih");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
