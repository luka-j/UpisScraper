package upismpn.download;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import static upismpn.UpisMpn.DEBUG;

/**
 *
 * @author Luka
 */
public class UceniciManager {

    static class UcData {

        private final String sifra;
        private final String ukBodova;
        private final String mestoOS;

        UcData(String sifra, String ukBodova, String mestoOS) {
            if(sifra.isEmpty()) throw new NullPointerException();
            this.sifra = sifra;
            this.ukBodova = ukBodova;
            this.mestoOS = mestoOS;
        }

        @Override
        public String toString() {
            return sifra + "\\" + ukBodova + "\\" + mestoOS + "\n";
        }
    }

    public static final File DATA_FOLDER = System.getProperty("os.name").toLowerCase().contains("win") ?
            new File("E:\\Shared\\mined\\UpisMpn\\")
            : new File("/media/luka/Data/Shared/mined/UpisMpn/");
    private static final int SAVE_AT = 400;
    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    static void add(Deque<UcData> data) {
        if(DEBUG) System.out.println("adding new chunk");
        sifre.addAll(data);
        if (sifre.size() >= SAVE_AT) {
            download();
            if(DEBUG) System.out.println("initializing saver");
            Saver s = new Saver();
            executor.execute(s);
            sifre.clear();
            ucenici.clear();
            if(DEBUG) System.out.println("lists cleared");
        }
    }

    private static final Deque<Ucenik> ucenici = new ArrayDeque<>(SAVE_AT + 161);
    private static final Deque<UcData> sifre = new ArrayDeque<>(SAVE_AT + 161);
    private static final Deque<UcData> failed = new ArrayDeque<>();

    static void download() {
        if(DEBUG)System.out.println("downloading ucenik info");
        sifre.forEach((UcData datum) -> {
            Ucenik uc = loadUcenik(datum.sifra, datum.ukBodova, datum.mestoOS);
            if (uc != null) {
                ucenici.add(uc);
            } else {
                if(DEBUG) System.out.println("failed downloading ucenik");
                failed.add(new UcData(datum.sifra, datum.ukBodova, datum.mestoOS));
            }
        });
        if(DEBUG)System.out.println("downloaded ucenik info");
    }

    private static Ucenik loadUcenik(String sifra, String ukBodova, String mestoOS) {
        Ucenik uc = null;
        try {
            uc = new Ucenik(sifra).setDetails(ukBodova, mestoOS).loadFromNet();
        } catch (SocketTimeoutException | SocketException ex) {
            try {
                Thread.sleep(15000);
                uc = new Ucenik(sifra).setDetails(ukBodova, mestoOS).loadFromNet();
            } catch (SocketTimeoutException nestedex) {
                System.err.println("Socket timeout @ loadFromNet: " + sifra);
            } catch (IOException | InterruptedException nestedex) {
                Logger.getLogger(UceniciManager.class.getName()).log(Level.SEVERE, null, nestedex);
            }

        } catch (IOException ex) {
            Logger.getLogger(UceniciManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uc;
    }

    /**
     * ConcurrentModificationException: what are the odds?
     * *2h later*: fk SBB.
     */
    private synchronized static void attemptToDownloadFailed() {
        failed.forEach((UcData datum) -> {
            Ucenik uc = loadUcenik(datum.sifra, datum.ukBodova, datum.mestoOS);
            if (uc != null) {
                ucenici.add(uc);
                failed.remove(datum);
            }
        });
    }

    public static int getFailedCount() {
        return failed.size();
    }

    public static void saveFailed() {
        File f = new File(DATA_FOLDER, "failed");
        final StringBuilder sb = new StringBuilder();
        failed.forEach((datum) -> sb.append(datum.toString()));
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(sb.toString());
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(UceniciManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void onExit() {
        DownloadController.mainThread.interrupt();
        new Saver().run();
    }

    static class Saver implements Runnable {

        private final Deque<Ucenik> data;
        
        Saver() {
            data = new ArrayDeque<>(ucenici);
        }
        @Override
        public void run() {
            attemptToDownloadFailed();
            data.forEach((Ucenik uc) -> {
                uc.saveToFile(DATA_FOLDER);
            });
            if(DEBUG)System.out.println("Saved ucenici");
            DownloadController.saveProgress();
        }
    }
}
