package rs.lukaj.upisstats.scraper.download;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class DownloadController {
    public static String YEAR = "17";

    public static final File DATA_FOLDER = System.getProperty("os.name").toLowerCase().contains("nix")
            || System.getProperty("os.name").toLowerCase().contains("nux") ?
            new File("/data/Shared/mined/UpisData/" + YEAR)
            : new File("D:\\Shared\\mined\\UpisData\\" + YEAR);
    public static Thread mainThread;
    private static final String SAVE_FILENAME = "save";
    private static StudentDownloader studentDownloader;

    /**
     * Ucitava sve smerova, a zatim ucitava ucenike
     * @see Smerovi#load()
     * @see StudentDownloader#downloadStudentData(DownloadConfig)
     */
    public static void startDownload(DownloadConfig config) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Save(config)));
        mainThread = Thread.currentThread();
        config.loadSmerovi();
        config.saveSmerovi();
        System.out.println("Ucitao sifre smerova");
        config.loadOsnovneIds();
        System.out.println("Ucitao id-jeve osnovnih skola");
        IntLongPair progress = loadProgress();
        studentDownloader = config.getStudentDownloader(progress.a, progress.b);
        studentDownloader.downloadStudentData(config);
        System.out.println("Ucitao ucenike; ucitavam osnovne");
        config.downloadOsnovne();
        System.out.println("Završio download!");
    }

    /**
     * Cuva poslednji ucitan smer i potroseno vreme u fajl odredjen s {@link DownloadController#DATA_FOLDER} i
     * {@link DownloadController#SAVE_FILENAME}
     */
    public static void saveProgress() {
        if(studentDownloader == null) return;
        File saveData = new File(DATA_FOLDER, SAVE_FILENAME);
        try (final FileWriter fw = new FileWriter(saveData)) {
            fw.write(String.valueOf(studentDownloader.getCurrentSmer()) + "\\" + String.valueOf(studentDownloader.getVreme()));
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static class IntLongPair {int a; long b;}
    private static IntLongPair loadProgress() {
        IntLongPair ret = new IntLongPair();
        File saveData = new File(DATA_FOLDER, SAVE_FILENAME);
        char[] buff = new char[16];
        try (final FileReader fr = new FileReader(saveData)) {
            if(saveData.length() == 0) return ret;
            fr.read(buff);
            String[] data = String.valueOf(buff).trim().split("\\\\");
            ret.a = Integer.valueOf(data[0]);
            ret.b = Long.valueOf(data[1]);
            return ret;
        } catch (FileNotFoundException ex) {
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    

    public static void test() {
        try {
            new Ucenik2017("131564").loadFromNet().saveToFile(DATA_FOLDER);
        } catch (IOException ex) {
            Logger.getLogger(DownloadController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Ucenik loaded = new Ucenik("418062");
        //loaded.loadFromFile(DATA_FOLDER);
        System.exit(1);
    }
    
    public static class Save implements Runnable {

        private DownloadConfig config;

        public Save(DownloadConfig config) {
            this.config = config;
        }

        @Override
        public void run() {
            UceniciManager inst = config.getUceniciManager();
            inst.onExit();
            if (inst.getFailedCount() > 0) {
                System.err.println("Failed downloads: " + inst.getFailedCount());
                System.err.println("saving...");
                inst.saveFailed();
            }
        }

    }
}