package rs.lukaj.upisstats.scraper.download;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import rs.lukaj.upisstats.scraper.Main;
import rs.lukaj.upisstats.scraper.utils.StringTokenizer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by luka on 3.7.17..
 */
public class Ucenik2017 extends Ucenik {

    private static final String UCENICI_URL = "http://upis.mpn.gov.rs/Lat/Ucenici/";

    public Ucenik2017(String id) {
        super(id);
    }

    private Boolean exists = null;
    protected String osId;
    protected String upisana;
    protected String jsonData;
    protected String bodovaAM;
    protected String blizanac, najboljiBlizanacBodovi;
    protected String maternji, prviStrani, drugiStrani;
    protected String origUkupnoBodova, origKrug;
    protected boolean prioritet;

    //naziv testa -> broj bodova
    protected Map<String, String> prijemni = new HashMap<>();

    protected List<Profil> profili = new ArrayList<>();
    protected List<Zelja> listaZelja1 = new ArrayList<>();
    protected List<Zelja> listaZelja2 = new ArrayList<>();

    public String getOsId() {
        return osId;
    }

    public String getJsonData() {
        return jsonData;
    }

    public String getBodovaAM() {
        return bodovaAM;
    }

    public String getBlizanac() {
        return blizanac;
    }

    public String getNajboljiBlizanacBodovi() {
        return najboljiBlizanacBodovi;
    }

    public String getMaternji() {
        return maternji;
    }

    public String getPrviStrani() {
        return prviStrani;
    }

    public String getDrugiStrani() {
        return drugiStrani;
    }

    public Map<String, String> getPrijemni() {
        return prijemni;
    }

    public List<Zelja> getListaZelja1() {
        return listaZelja1;
    }

    public List<Zelja> getListaZelja2() {
        return listaZelja2;
    }

    public String getUpisana() {
        return upisana;
    }

    public boolean isPrioritet() {
        return prioritet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() {
        if(exists == null)
            exists = super.exists() && new File(DownloadController.DATA_FOLDER, id + ".json").exists();
        return exists;
    }

    public Ucenik setDetails(String ukBodova, String krug) {
        origUkupnoBodova = ukBodova;
        prioritet = ukBodova.startsWith("+");
        this.origKrug = krug;
        return this;
    }

    public static class Zelja {
        private final String sifraSmera, uslov, bodovaZaUpis;

        public Zelja(String sifraSmera, String uslov, String bodovaZaUpis) {
            this.sifraSmera = sifraSmera;
            this.uslov = uslov;
            this.bodovaZaUpis = bodovaZaUpis;
        }
        public Zelja(String compactString) {
            StringTokenizer tk = new StringTokenizer(compactString, ',', true);
            sifraSmera = tk.nextToken();
            uslov = tk.nextToken();
            bodovaZaUpis = tk.hasMoreTokens() ? tk.nextToken() : "";
        }
        public String getSifraSmera() {
            return sifraSmera;
        }
        public String getUslov() {
            return uslov;
        }
        public String getBodovaZaUpis() {
            return bodovaZaUpis;
        }

        @Override
        public String toString() {
            return sifraSmera + "," + uslov + "," + bodovaZaUpis;
        }
    }

    public static class Profil {
        private final String naziv, prijemni, takmicenje, ukupno;

        public Profil(String naziv, String prijemni, String takmicenje, String ukupno) {
            this.naziv = naziv;
            this.takmicenje = takmicenje;
            this.prijemni = prijemni;
            this.ukupno = ukupno;
        }
        public Profil(String compactString) {
            StringTokenizer tk = new StringTokenizer(compactString, ',', true);
            naziv = tk.nextToken();
            prijemni = tk.nextToken();
            takmicenje = tk.nextToken();
            ukupno = tk.nextToken();
        }

        @Override
        public String toString() {
            return naziv + "," + prijemni + "," + takmicenje + "," + ukupno;
        }
    }

    @Override
    public Ucenik loadFromNet() throws IOException {
        if (Main.DEBUG) {
            System.out.println("loading ucenik: " + id);
        }
        if(!OVERWRITE_OLD && exists()) return this;
        if(!OVERWRITE_OLD && PRINT_MISSING) {
            System.out.println("Missing! " + id);
            DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.WARNING, "Missing (downloaded): " + id);
        }

        Document doc = Jsoup.connect(UCENICI_URL + id).get();
        Elements scripts = doc.getElementsByTag("script");
        String script = scripts.get(scripts.size()-3).data();
        parseJson(script);

        return this;
    }

    public Ucenik2017 loadFromJson() throws IOException {
        if(!OVERWRITE_OLD) System.err.println("warning: OVERWRITE_OLD is set to false; changes won't persist");
        File f = new File(DownloadController.DATA_FOLDER, id + ".json");
        parseJson("\r\n" + new String(Files.readAllBytes(f.toPath())).replace('\r', '\n'));
        return this;
    }

    private void parseJson(String script) throws IOException {
        String[] data = script.split("\\n", 12);
        //data[0] is only \r
        String basic = data[1].split(" = ")[1].trim().replace("];", "]");
        String sesti = data[2].split(" = ")[1].trim().replace("];", "]");
        String sedmi = data[3].split(" = ")[1].trim().replace("];", "]");
        String osmi  = data[4].split(" = ")[1].trim().replace("];", "]");
        String nagrade = data[5].split(" = ")[1].trim().replace("];", "]");
        String prijemni = data[6].split(" = ")[1].trim().replace("];", "]");
        String profili = data[7].split(" = ")[1].trim().replace("];", "]");
        String zelje = data[8].split(" = ")[1].trim().replace("];", "]");
        String zelje2 = data[9].split(" = ")[1].trim().replace("];", "]");
        parseBasic(basic);
        OsnovneDownloader2017.getInstance().addOsnovna(Integer.parseInt(osId));
        sestiRaz = parseOcene(sesti);
        sedmiRaz = parseOcene(sedmi);
        osmiRaz = parseOcene(osmi);
        parseNagrade(nagrade);
        parsePrijemni(prijemni);
        parseProfili(profili);
        parseZelje(zelje, listaZelja1);
        parseZelje(zelje2, listaZelja2);

        StringBuilder sb = new StringBuilder();
        for(int i=1; i<10; i++) sb.append(data[i]).append("\n");
        jsonData = sb.toString();
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

        String krugText = data.get("UpisanNaOpis").getAsString();
        if(krugText.startsWith("Raspoređen u prvom") ||
                krugText.startsWith("Распоређен у првом")) krug = "1";
        else if(krugText.startsWith("Raspoređen u drugom") ||
                krugText.startsWith("Распоређен у другом")) krug = "2";
        else if(krugText.startsWith("Upisan po odluci OUK") ||
                krugText.startsWith("Уписан по одлуци ОУК")) krug = "*";
        else throw new IllegalArgumentException("Invalid krug text: " + krugText + " @ " + id);
    }

    private Map<String, String> parseOcene(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = gson.fromJson(json.substring(1, json.length()-1), type);
        map.remove("IDUcenik");
        return map;
    }

    private void parseNagrade(String json) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            takmicenja.put(obj.get("nagradaPredmet").getAsString() + "~" + obj.get("nagradaNivo").getAsString()
                    + "~" + obj.get("nagradaMesto").getAsString(),
                    obj.get("nagradaBodova").getAsString());
        }
    }

    private void parsePrijemni(String json) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            prijemni.put(obj.get("Prijemni").getAsString().trim(), obj.get("Bodova").getAsString());
        }
    }

    private void parseProfili(String json) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            profili.add(new Profil(obj.get("Profil").getAsString(), obj.get("Bodova").getAsString(),
                    obj.get("BodovaNagrade").getAsString(), obj.get("Ukupno").getAsString()));
        }
    }

    private void parseZelje(String json, List<Zelja> zelje) {
        JsonArray data = new JsonParser().parse(json).getAsJsonArray();
        for(JsonElement el : data) {
            JsonObject obj = el.getAsJsonObject();
            zelje.add(new Zelja(obj.get("sifra").getAsString(), obj.get("IspunioUslov").getAsString(),
                    obj.get("sBodova").getAsString()));
        }
    }

    @Override
    public String toCompactString() {
        return osId + "\\" + upisana + "\\" + origKrug + "\\" + krug + "\\" + blizanac + "\\" + najboljiBlizanacBodovi + "\\" + prioritet + "\n" +
                srpski + "\\" + matematika + "\\" + kombinovani + "\\" + bodovaAM + "\\" + origUkupnoBodova + "\\" + ukupnoBodova + "\n" +
                maternji + "\\" + prviStrani + "\\" + drugiStrani + "\n" +
                UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sestiRaz)) +
                UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sedmiRaz)) +
                UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(osmiRaz)) +
                UcenikUtils.mapToStringBuilder(takmicenja) +
                UcenikUtils.mapToStringBuilder(prijemni) +
                UcenikUtils.listToStringBuilder(profili) +
                UcenikUtils.listToStringBuilder(listaZelja1) +
                UcenikUtils.listToStringBuilder(listaZelja2);
    }

    @Override
    public void loadFromString(String compactString) {
        StringTokenizer master = new StringTokenizer(compactString, '\n', true);

        StringTokenizer inner = new StringTokenizer(master.nextToken(), '\\', true);
        osId = inner.nextToken();
        upisana = inner.nextToken();
        origKrug = inner.nextToken();
        krug = inner.nextToken();
        blizanac = inner.nextToken();
        najboljiBlizanacBodovi = inner.nextToken();
        prioritet = Boolean.parseBoolean(inner.nextToken());

        inner = new StringTokenizer(master.nextToken(), '\\', true);
        srpski = inner.nextToken();
        matematika = inner.nextToken();
        kombinovani = inner.nextToken();
        bodovaAM = inner.nextToken();
        origUkupnoBodova = inner.nextToken();
        ukupnoBodova = inner.nextToken();

        inner = new StringTokenizer(master.nextToken(), '\\', true);
        maternji = inner.nextToken();
        prviStrani = inner.nextToken();
        drugiStrani = inner.nextToken();


        loadOcene(sestiRaz, master.nextToken());
        loadOcene(sedmiRaz, master.nextToken());
        loadOcene(osmiRaz, master.nextToken());
        loadMap(takmicenja, master.nextToken());
        loadMap(prijemni, master.nextToken());

        inner = new StringTokenizer(master.nextToken(), '\\', false);
        while(inner.hasMoreTokens())
            this.profili.add(new Profil(inner.nextToken()));
        inner = new StringTokenizer(master.nextToken(), '\\', false);
        while(inner.hasMoreTokens())
            this.listaZelja1.add(new Zelja(inner.nextToken()));
        inner = new StringTokenizer(master.nextToken(), '\\', false);
        while(inner.hasMoreTokens())
            this.listaZelja2.add(new Zelja(inner.nextToken()));
        long end = System.nanoTime();
    }

    private void loadOcene(Map<String, String> to, String from) {
        if(UcenikUtils.PredmetiDefault.inverse == null) UcenikUtils.PredmetiDefault.initInverse();

        StringTokenizer predmeti = new StringTokenizer(from, '\\', false);
        while(predmeti.hasMoreTokens()) {
            String pr = predmeti.nextToken();
            String predmet = UcenikUtils.PredmetiDefault.inverse.get(String.valueOf(pr.charAt(0)));
            to.put(predmet, pr.substring(2));
        }
    }

    private void loadMap(Map<String, String> to, String from) {
        StringTokenizer items = new StringTokenizer(from, '\\', false);
        while(items.hasMoreTokens()) {
            String item = items.nextToken();
            int delim = item.indexOf(':');
            to.put(item.substring(0, delim), item.substring(delim+1));
        }
    }

    @Override
    public void saveToFile(File folder) {
        super.saveToFile(folder);
        if(exists() && !OVERWRITE_OLD) return;
        File f = new File(folder, id + ".json");
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            fw.write(jsonData);
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveToFile(File folder, boolean skipJson) {
        if(skipJson) super.saveToFile(folder);
        else this.saveToFile(folder);
    }
}
