package rs.lukaj.upisstats.scraper.download;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class DownloadController {
    public static final String YEAR = "20";
    public static final int YEAR_INT = 2000 + Integer.parseInt(YEAR);
    private static final boolean DOWNLOAD_OSNOVNE = true;

    public static File DATA_FOLDER = System.getProperty("os.name").toLowerCase().contains("nix")
            || System.getProperty("os.name").toLowerCase().contains("nux") ?
            new File("/tmpdata/UpisData/" + YEAR)
            : new File("E:\\Shared\\mined\\UpisData\\" + YEAR);
    public static Thread mainThread;
    private static final String SAVE_FILENAME = "save";
    private static UceniciDownloader uceniciDownloader;

    public static File generateDataFolder(String year) {
        return System.getProperty("os.name").toLowerCase().contains("nix")
                || System.getProperty("os.name").toLowerCase().contains("nux") ?
                new File("/tmpdata/UpisData/" + year)
                : new File("E:\\Shared\\mined\\UpisData\\" + year);
    }

    /**
     * Ucitava sve sifre smerova, a zatim ucitava ucenike
     * @see Smerovi#load()
     * @see UceniciDownloader#downloadStudentData(DownloadConfig)
     */
    public static void startDownload(DownloadConfig config) {
        DownloadLogger.setLogLevel(DownloadLogger.Level.DEBUG);
        DownloadLogger logger = DownloadLogger.getLogger(DownloadLogger.GENERAL);
        Runtime.getRuntime().addShutdownHook(new Thread(new Save(config)));
        logger.log(DownloadLogger.Level.NORMAL, "Added shutdown hook for saving");
        mainThread = Thread.currentThread();
        config.loadSmerovi();
        logger.log(DownloadLogger.Level.NORMAL, "Loaded smerovi");
        config.saveSmerovi();
        logger.log(DownloadLogger.Level.NORMAL, "Saved smerovi");
        System.out.println("Ucitao sifre smerova");
        config.loadOsnovneIds();
        System.out.println("Ucitao id-jeve osnovnih skola");
        logger.log(DownloadLogger.Level.NORMAL, "Loaded osnovne ids");
        IntLongPair progress = loadProgress();
        uceniciDownloader = config.getStudentDownloader(progress.a, progress.b);
        uceniciDownloader.downloadStudentData(config);
        logger.log(DownloadLogger.Level.NORMAL, "Downloaded all ucenici");
        if(DOWNLOAD_OSNOVNE) {
            System.out.println("Ucitao ucenike; ucitavam osnovne");
            config.downloadOsnovne();
            logger.log(DownloadLogger.Level.NORMAL, "Downloaded osnovne");
        }
        logger.log(DownloadLogger.Level.NORMAL, "Download complete");
        System.out.println("Završio download!");
    }

    /**
     * Cuva poslednji ucitan smer i potroseno vreme u fajl odredjen s {@link DownloadController#DATA_FOLDER} i
     * {@link DownloadController#SAVE_FILENAME}
     */
    public static void saveProgress() {
        if(uceniciDownloader == null) return;
        File saveData = new File(DATA_FOLDER, SAVE_FILENAME);
        try (final FileWriter fw = new FileWriter(saveData)) {
            fw.write(uceniciDownloader.getCurrentSmer() + "\\" + uceniciDownloader.getVreme());
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
