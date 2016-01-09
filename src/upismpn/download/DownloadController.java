package upismpn.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class DownloadController {
    public static Thread mainThread;
    private static final String SAVE_FILENAME = "save";

    /**
     * Ucitava sve smerova, a zatim ucitava ucenike
     * @see Smerovi#load()
     * @see StudentDownloader#downloadStudentData()
     */
    public static void startDownload() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Save()));
        mainThread = Thread.currentThread();
        Smerovi.load();
        Smerovi.save();
        System.out.println("Ucitao sifre smerova");
        IntLongPair progress = loadProgress();
        StudentDownloader.setStart(progress.a);
        StudentDownloader.setVreme(progress.b);
        StudentDownloader.downloadStudentData();
    }

    /**
     * Cuva poslednji ucitan smer i potroseno vreme u fajl odredjen s {@link UceniciManager#DATA_FOLDER} i
     * {@link DownloadController#SAVE_FILENAME}
     */
    public static void saveProgress() {
        File saveData = new File(UceniciManager.DATA_FOLDER, SAVE_FILENAME);
        try (final FileWriter fw = new FileWriter(saveData)) {
            saveData.delete();
            saveData.createNewFile();
            fw.write(String.valueOf(StudentDownloader.getCurrentSmer()) + "\\" + String.valueOf(StudentDownloader.getVreme()));
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static class IntLongPair {int a; long b;}
    private static IntLongPair loadProgress() {
        IntLongPair ret = new IntLongPair(); ret.a = 0; ret.b = 0;
        File saveData = new File(UceniciManager.DATA_FOLDER, SAVE_FILENAME);
        char[] buff = new char[16];
        try (final FileReader fr = new FileReader(saveData)) {
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
    
    

    public static void test() throws InterruptedException {
        try {
            new Ucenik("418062").loadFromNet().saveToFile(UceniciManager.DATA_FOLDER);
        } catch (IOException ex) {
            Logger.getLogger(DownloadController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Ucenik loaded = new Ucenik("418062");
        loaded.loadFromFile(UceniciManager.DATA_FOLDER);
        System.exit(1);
    }
    
    public static class Save implements Runnable {

        @Override
        public void run() {
            UceniciManager.onExit();
            if (UceniciManager.getFailedCount() > 0) {
                System.err.println("Failed downloads: " + UceniciManager.getFailedCount());
                System.err.println("saving...");
                UceniciManager.saveFailed();
            }
        }

    }
}
