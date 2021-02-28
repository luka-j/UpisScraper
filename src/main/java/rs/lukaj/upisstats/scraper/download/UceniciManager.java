package rs.lukaj.upisstats.scraper.download;

import rs.lukaj.upisstats.scraper.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
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
    
    public void add(Deque<UcData> data) {
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


    public void download() {
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
                Thread.sleep(10000);
                uc = config.generateUcenik(sifra, ukBodova, mestoOS).loadFromNet();
            } catch (SocketTimeoutException nestedex) {
                System.err.println("Socket timeout @ loadFromNet: " + sifra);
            } catch (IOException | InterruptedException nestedex) {
                System.err.println("Error while loading ucenik " + sifra);
                DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.ERROR, "IO Exception while loading " + sifra);
            }

        } catch (IOException ex) {
            System.err.println("Error while loading ucenik " + sifra);
            DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.ERROR, "IO Exception while loading " + sifra);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            System.err.println("Unknown exception while loading ucenik " + sifra);
            e.printStackTrace();
            DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.ERROR, "Unknown " + e.getClass().getName() + " while loading " + sifra);
        }
        return uc;
    }

    /*
     * ConcurrentModificationException: what are the odds?
     * *2h later*: fk SBB. (1 year later: and buggy wifi drivers)
     */
    private synchronized void attemptToDownloadFailed() {
        for (Iterator<UcData> iterator = failed.iterator(); iterator.hasNext(); ) {
            UcData datum = iterator.next();
            Ucenik uc = loadUcenik(datum.sifra, datum.ukBodova, datum.mestoOS);
            if (uc != null) {
                ucenici.add(uc);
                iterator.remove();
            }
        }
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
        } catch (IOException ex) {
            DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.ERROR, "IO Exception while saving faileds!");
            ex.printStackTrace();
        }
    }
    
    public void onExit() {
        DownloadController.interruptMainThread();
        new Saver().run();
    }

    public class Saver implements Runnable {

        private final Deque<Ucenik> data;
        
        public Saver() {
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
