package upismpn.download;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by luka on 3.7.17..
 */
public class Osnovna2017 {
    protected int id;
    protected String naziv, sediste, opstina, okrug;
    protected String bodova6, bodova7, bodova8, matematika, srpski, kombinovani;
    protected String brojUcenika, ucenikaZavrsilo, vukovaca, nagradjenih;

    protected static final File OSNOVNE_FOLDER = new File(DownloadController.DATA_FOLDER, "osnovne-json");
    static {
        if(!OSNOVNE_FOLDER.isDirectory()) OSNOVNE_FOLDER.mkdirs();
    }
    protected String json;

    public Osnovna2017(int id) {
        this.id = id;
    }

    public void loadFromNet() {
        try {
            Document doc = Jsoup.connect(generateUrl(id)).get();
            Elements scripts = doc.getElementsByTag("script");
            String script = scripts.get(scripts.size()-3).data();
            json = script.trim().split(";", 2)[0].split(" = ", 2)[1];
            JsonObject data = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
            naziv = data.get("NazivSkole1").getAsString();
            sediste = data.get("SedisteSkole1").getAsString();
            opstina = data.get("Opstina1").getAsString();
            okrug = data.get("NazivOkruga1").getAsString();
            bodova6 = data.get("B6").getAsString();
            bodova7 = data.get("B7").getAsString();
            bodova8 = data.get("B8").getAsString();
            srpski = data.get("BZS").getAsString();
            matematika = data.get("BZM").getAsString();
            kombinovani = data.get("BZK").getAsString();
            brojUcenika = data.get("BrojUcenika").getAsString();
            ucenikaZavrsilo = data.get("BrojUcenikaZavrsili").getAsString();
            vukovaca = data.get("BrojUcenikaVukovaca").getAsString();
            nagradjenih = data.get("BrojUcenikaNagrada").getAsString();
        } catch (SocketTimeoutException e) {
            System.err.println("SocketEx @ Osnovna2017#loadFromNet ("+id+")");
            try {
                Thread.sleep(20000);
                loadFromNet();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Unexpected IOException in Osnovna2017#loadFromNet, id: " + id);
            e.printStackTrace();
        }
    }

    public void saveJson() {
        try (FileWriter fw = new FileWriter(new File(OSNOVNE_FOLDER, String.valueOf(id) + ".json"))) {
            fw.write(json);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("\\").append(naziv).append("\\").append(sediste).append("\\").append(opstina).append("\\").append(okrug).append("\n");
        sb.append(bodova6).append("\\").append(bodova7).append("\\").append(bodova8).append("\\").append(matematika).append("\\").append(srpski).append("\\").append(kombinovani).append("\n");
        sb.append(brojUcenika).append("\\").append(ucenikaZavrsilo).append("\\").append(vukovaca).append("\\").append(nagradjenih).append("\n");
        return sb.toString();
    }
    public void loadFromString() {
        //todo
    }

    private static String generateUrl(int id) {
        return "http://upis.mpn.gov.rs/Lat/Osnovne-skole/" + id;
    }
}
