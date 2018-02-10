package rs.lukaj.upisstats.scraper.download;

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

    public int getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getSediste() {
        return sediste;
    }

    public String getOpstina() {
        return opstina;
    }

    public String getOkrug() {
        return okrug;
    }

    public String getBodova6() {
        return bodova6;
    }

    public String getBodova7() {
        return bodova7;
    }

    public String getBodova8() {
        return bodova8;
    }

    public String getMatematika() {
        return matematika;
    }

    public String getSrpski() {
        return srpski;
    }

    public String getKombinovani() {
        return kombinovani;
    }

    public String getBrojUcenika() {
        return brojUcenika;
    }

    public String getUcenikaZavrsilo() {
        return ucenikaZavrsilo;
    }

    public String getVukovaca() {
        return vukovaca;
    }

    public String getNagradjenih() {
        return nagradjenih;
    }

    protected static final File OSNOVNE_FOLDER = new File(DownloadController.DATA_FOLDER, "osnovne-json");
    static {
        if(!OSNOVNE_FOLDER.isDirectory()) OSNOVNE_FOLDER.mkdirs();
    }
    protected String json;

    public Osnovna2017(int id) {
        this.id = id;
    }
    public Osnovna2017(String str) {
        String[] lines = str.split("\n");
        String[] basic = lines[0].split("\\\\");
        id = Integer.parseInt(basic[0]);
        naziv = basic[1];
        sediste = basic[2];
        opstina = basic[3];
        okrug = basic[4];
        String[] ocene = lines[1].split("\\\\");
        bodova6 = ocene[0];
        bodova7 = ocene[1];
        bodova8 = ocene[2];
        matematika = ocene[3];
        srpski = ocene[4];
        kombinovani = ocene[5];
        String[] ucenici = lines[2].split("\\\\");
        brojUcenika = ucenici[0];
        ucenikaZavrsilo = ucenici[1];
        vukovaca = ucenici[2];
        nagradjenih = ucenici[3];
        //Profiler.addTime("new Osnovna2017", end-start);
    }

    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("\\").append(naziv).append("\\").append(sediste).append("\\").append(opstina).append("\\").append(okrug).append("\n");
        sb.append(bodova6).append("\\").append(bodova7).append("\\").append(bodova8).append("\\").append(matematika).append("\\").append(srpski).append("\\").append(kombinovani).append("\n");
        sb.append(brojUcenika).append("\\").append(ucenikaZavrsilo).append("\\").append(vukovaca).append("\\").append(nagradjenih).append("\n");
        return sb.toString();
    }

    public void loadFromNet() {
        try {
            Document doc = Jsoup.connect(generateUrl(id)).get();
            Elements scripts = doc.getElementsByTag("script");
            String script = scripts.get(scripts.size()-3).data();
            json = script.trim().split("}];", 2)[0].split(" = ", 2)[1] + "}]";
            JsonObject data = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
            naziv = data.get("NazivSkole2").getAsString(); //2 je za latinicu. Na cirilicnom sajtu je NazivSkole1
            sediste = data.get("SedisteSkole2").getAsString(); //ponekad se zaista pitam kakvi ljudi rade ovakve stvari
            opstina = data.get("Opstina2").getAsString();
            okrug = data.get("NazivOkruga2").getAsString();
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
        File save = new File(OSNOVNE_FOLDER, String.valueOf(id) + ".json");
        try (FileWriter fw = new FileWriter(save)) {
            save.createNewFile();
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

    private static String generateUrl(int id) {
        return "http://upis.mpn.gov.rs/Lat/Osnovne-skole/" + id;
    }
}
