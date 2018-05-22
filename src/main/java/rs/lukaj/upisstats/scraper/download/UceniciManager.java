package rs.lukaj.upisstats.scraper.download;

import rs.lukaj.upisstats.scraper.Main;

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

/**
 *
 * @author Luka
 */
public class UceniciManager {

    private static UceniciManager instance;
    private DownloadConfig config;

    public static UceniciManager getInstance(DownloadConfig config) {
        if(instance == null) instance = new UceniciManager(config);
        return instance;
    }

    protected UceniciManager(DownloadConfig config) {
        this.config = config;
    }

    public static class UcData {

        public final String sifra;
        public final String ukBodova;
        public final String mestoOS;

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

    private static final int SAVE_AT = 400;
    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    protected void add(Deque<UcData> data) {
        if(Main.DEBUG) System.out.println("adding new chunk");
        sifre.addAll(data);
        if (sifre.size() >= SAVE_AT) {
            download();
            if(Main.DEBUG) System.out.println("initializing saver");
            Saver s = new Saver();
            executor.execute(s);
            sifre.clear();
            ucenici.clear();
            if(Main.DEBUG) {
                System.out.println("lists cleared");
            }
        }
    }

    protected final Deque<Ucenik> ucenici = new ArrayDeque<>(SAVE_AT + 161);
    protected final Deque<UcData> sifre = new ArrayDeque<>(SAVE_AT + 161);
    protected final Deque<UcData> failed = new ArrayDeque<>();


    protected void download() {
        if(Main.DEBUG)System.out.println("downloading ucenik info");
        sifre.forEach((UcData datum) -> {
            Ucenik uc = loadUcenik(datum.sifra, datum.ukBodova, datum.mestoOS);
            if (uc != null) {
                ucenici.add(uc);
            } else {
                if(Main.DEBUG) System.out.println("failed downloading ucenik");
                failed.add(new UcData(datum.sifra, datum.ukBodova, datum.mestoOS));
            }
        });
        if(Main.DEBUG)System.out.println("downloaded ucenik info");
    }

    private Ucenik loadUcenik(String sifra, String ukBodova, String mestoOS) {
        Ucenik uc = null;
        try {
            uc = config.generateUcenik(sifra, ukBodova, mestoOS).loadFromNet();
        } catch (SocketTimeoutException | SocketException ex) {
            try {
                Thread.sleep(15000);
                uc = config.generateUcenik(sifra, ukBodova, mestoOS).loadFromNet();
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
     * *2h later*: fk SBB. (1 year later: and buggy wifi drivers)
     */
    private synchronized void attemptToDownloadFailed() {
        failed.forEach((UcData datum) -> {
            Ucenik uc = loadUcenik(datum.sifra, datum.ukBodova, datum.mestoOS);
            if (uc != null) {
                ucenici.add(uc);
                failed.remove(datum);
            }
        });
    }

    public int getFailedCount() {
        return failed.size();
    }

    public void saveFailed() {
        File f = new File(DownloadController.DATA_FOLDER, "failed");
        final StringBuilder sb = new StringBuilder();
        failed.forEach((datum) -> sb.append(datum.toString()));
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(sb.toString());
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(UceniciManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void onExit() {
        DownloadController.mainThread.interrupt();
        new Saver().run();
    }

    class Saver implements Runnable {

        private final Deque<Ucenik> data;
        
        Saver() {
            data = new ArrayDeque<>(ucenici);
        }
        @Override
        public void run() {
            attemptToDownloadFailed();
            data.forEach((Ucenik uc) -> uc.saveToFile(DownloadController.DATA_FOLDER));
            if(Main.DEBUG)System.out.println("Saved ucenici");
            DownloadController.saveProgress();
        }
    }
}
