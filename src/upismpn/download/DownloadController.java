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

    public static void startDownload() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Save()));
        mainThread = Thread.currentThread();
        Smerovi.load();
        Smerovi.save();
        System.out.println("Ucitao sifre smerova");
        System.exit(0);
        Sifre.setStart(loadProgress().a);
        Sifre.setVreme(loadProgress().b);
        Sifre.go();
    }

    public static void saveProgress() {
        File saveData = new File(UceniciManager.DATA_FOLDER, SAVE_FILENAME);
        try (final FileWriter fw = new FileWriter(saveData)) {
            saveData.delete();
            saveData.createNewFile();
            fw.write(String.valueOf(Sifre.getStart()) + "\\" + String.valueOf(Sifre.getVreme()));
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
            ret.a = Integer.valueOf(String.valueOf(buff).split("\\\\")[0].trim());
            ret.b = Long.valueOf(String.valueOf(buff).split("\\\\")[1].trim());
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
