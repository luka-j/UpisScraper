package upismpn.download;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static upismpn.UpisMpn.DEBUG;

/**
 * Created by luka on 3.7.17..
 */
public class Ucenik2017 extends Ucenik {
    public Ucenik2017(String id) {
        super(id);
    }

    protected String osId;
    protected String jsonData;
    protected String bodovaAM;
    protected String blizanac, najboljiBlizanacBodovi;
    protected String maternji, prviStrani, drugiStrani;

    //naziv testa -> broj bodova
    protected Map<String, String> prijemni;
    protected List<Zelja> listaZelja1 = new ArrayList<>();
    protected List<Zelja> listaZelja2 = new ArrayList<>();
    protected String upisana;

    public Ucenik setDetails(String ukBodova, String krug) {
        ukupnoBodova = ukBodova;
        this.krug = krug;
        return this;
    }

    public static class Zelja {
        private String sifraSmera, uslov;

        public Zelja(String sifraSmera, String uslov) {
            this.sifraSmera = sifraSmera;
            this.uslov = uslov;
        }
        public String getSifraSmera() {
            return sifraSmera;
        }
        public String getUslov() {
            return uslov;
        }

        @Override
        public String toString() {
            return sifraSmera + "," + uslov;
        }
    }

    @Override
    public Ucenik loadFromNet() throws IOException {
        if (DEBUG) {
            System.out.println("loading ucenik: " + id);
        }

        Document doc = Jsoup.connect("http://upis.mpn.gov.rs/Lat/Ucenici/" + id).get();
        Elements scripts = doc.getElementsByTag("script");
        String script = scripts.get(scripts.size()-3).data();
        String[] data = script.split("\\n", 10);
        String basic = data[0].split("=")[1].trim();
        String sesti = data[1].split("=")[1].trim();
        String sedmi = data[2].split("=")[1].trim();
        String osmi  = data[3].split("=")[1].trim();
        String nagrade = data[4].split("=")[1].trim();
        String prijemni = data[5].split("=")[1].trim();
        String zelje = data[7].split("=")[1].trim();
        parseBasic(basic);
        OsnovneDownloader2017.getInstance().addOsnovna(Integer.parseInt(osId));
        sestiRaz = parseOcene(sesti);
        sedmiRaz = parseOcene(sedmi);
        osmiRaz = parseOcene(osmi);
        parseNagrade(nagrade);
        parsePrijemni(prijemni);
        parseZelje(zelje, listaZelja1);

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<9; i++) sb.append(data[i]);
        jsonData = sb.toString();
        return this;
    }

    private void parseBasic(String json) {
        JsonObject data = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
        osId = data.get("IDSKola").getAsString();
        upisana = data.get("UpisanNa").getAsString();
        srpski = data.get("BodovaSrp").getAsString();
        matematika = data.get("BodovaMat").getAsString();
        kombinovani = data.get("BodovaKom").getAsString();
        bodovaAM = data.get("BodovaAM").getAsString();
        ukupnoBodova = data.get("BodovaUkupno").getAsString();
        maternji = data.get("Maternji").getAsString();
        prviStrani = data.get("PrviStraniJezik").getAsString();
        drugiStrani = data.get("DrugiStraniJezik").getAsString();
        blizanac = data.get("blizanac").getAsString();
        najboljiBlizanacBodovi = data.get("NajboljiBlizanacBodovi").getAsString(); //I have no idea what this is
    }

    private Map<String, String> parseOcene(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = gson.fromJson(json.substring(1, json.length()-1), type);
        map.remove("IDUcenik");
        map.remove("ZbirOcena");
        map.remove("BrojOcena");
        map.remove("prosek6");
        map.remove("prosek7");
        map.remove("prosek8");
        map.remove("vukovaDiploma");
        return map;
    }

    private void parseNagrade(String json) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            takmicenja.put(obj.get("nagradaPredmet").getAsString(), obj.get("nagradaBodova").getAsString());
        }
    }

    private void parsePrijemni(String json) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            prijemni.put(obj.get("Prijemni").getAsString(), obj.get("Bodova").getAsString());
        }
    }

    private void parseZelje(String json, List<Zelja> zelje) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            zelje.add(new Zelja(obj.get("sifra").getAsString(), obj.get("IspunioUslov").getAsString()));
        }
    }

    @Override
    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        sb.append(osId).append("\\").append(upisana).append("\\").append(krug).append("\\").append(blizanac).append("\\").append(najboljiBlizanacBodovi).append("\n");
        sb.append(srpski).append("\\").append(matematika).append("\\").append(kombinovani).append("\\").append(bodovaAM).append("\\").append(ukupnoBodova).append("\n");
        sb.append(maternji).append("\\").append(prviStrani).append("\\").append(drugiStrani).append("\n");
        sb.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sestiRaz)));
        sb.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sedmiRaz)));
        sb.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(osmiRaz)));
        sb.append(UcenikUtils.mapToStringBuilder(takmicenja));
        sb.append(UcenikUtils.mapToStringBuilder(prijemni));
        sb.append(UcenikUtils.listToStringBuilder(listaZelja1));
        sb.append(UcenikUtils.listToStringBuilder(listaZelja2));
        return sb.toString();
    }

    @Override
    public void loadFromString(String compactString) {
        //todo
    }

    @Override
    public void saveToFile(File folder) {
        super.saveToFile(folder);
        File f = new File(folder, id + ".json");
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            f.delete();
            f.createNewFile();
            fw.write(jsonData);
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
