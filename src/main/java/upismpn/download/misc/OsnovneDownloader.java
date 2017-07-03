package upismpn.download.misc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import upismpn.download.StudentDownloader;
import upismpn.download.UceniciManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static upismpn.UpisMpn.DEBUG;

/**
 * Created by luka on 2.4.17..
 */
public class OsnovneDownloader {
    private static final String OSNOVNE_URL =
            "http://upis.mpn.gov.rs/skola_homepage.php?prikazi_details=1&ucitaj_podatke=1&skola_id=";
    private static final File SAVEFILE = new File(UceniciManager.DATA_FOLDER, "osnovne");


    public static void downloadOsnovneData() {
        Document doc;
        Deque<Osnovna> skole = new ArrayDeque<>(1250);
        boolean start=false;
        int consecEmpty = 0; //zato sto je 707ica prazna. Ne znam zaista
                             //i to nije sve: od 1700 i nesto do 1800 je prazno. fuck it
        int i=500;
        while(consecEmpty<100) {
            do {
                if(DEBUG && i%50==0)
                    System.out.println("downloading doc for osnovna id " + i + "(started: " + start + ")");
                doc = downloadDoc(i);
            } while(doc == null);
            String ime = doc.select(".osnovna_podaci").get(0).text();
            if(start && ime.isEmpty()) consecEmpty++;
            else consecEmpty=0;
            if(!ime.isEmpty()) start=true;
            if(start && !ime.isEmpty()) skole.push(new Osnovna(i, doc));
            i++;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAVEFILE))) {
            for (Osnovna os : skole) {
                bw.write(os.toCompactString());
                bw.write("\n$\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document downloadDoc(int id) {
        try {
            return Jsoup.connect(OSNOVNE_URL + id).get();
        } catch (SocketTimeoutException ex) {
            System.err.println("Socket timeout @ downloadDoc (OsnovneDownloader)");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex1) {
            Logger.getLogger(StudentDownloader.class.getName()).log(Level.SEVERE, null, ex1);
        }
        return null;
    }
}
