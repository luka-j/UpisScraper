package rs.lukaj.upisstats.scraper.download;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rs.lukaj.upisstats.scraper.Main;
import rs.lukaj.upisstats.scraper.utils.StringTokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Predstavlja Ucenika u obliku pogodnom za preuzimanje s neta i cuvanje u fajl (Stringovi)
 */
public class Ucenik {

    protected static final boolean OVERWRITE_OLD = false;
    private static final boolean PRINT_MISSING = false;
    private Boolean exists = null;

    private static final String UCENICI_URL = "http://195.222.98.40/ucenik_info.php?id_ucenika=";

    public final String id; //filename

    protected String osnovnaSkola; //ime_skole
    protected String mestoOS; //ime_mesta\\
    protected String okrugOS; //ime_okruga\n

    protected Map<String, String> sestiRaz = new HashMap<>(); //predmet:ocena\\predmet:ocena... \n
    protected Map<String, String> sedmiRaz = new HashMap<>(); //predmet:ocena\\predmet:ocena... \n
    protected Map<String, String> osmiRaz = new HashMap<>(); //predmet:ocena\\predmet:ocena... \n

    protected Map<String, String> takmicenja = new HashMap<>(); //predmet:bodova\\predmet:bodova... \n

    protected String matematika; //broj_bodova\\
    protected String srpski; //broj_bodova\\
    protected String kombinovani; //broj_bodova\n

    /**
     * !! Zajedno sa prijemnim.
     */
    protected String ukupnoBodova; //broj_bodova\n

    protected List<Skola> listaZelja = new ArrayList<>(); //skola_1\\skola_2\\skola_3... \n
    protected Skola upisanaSkola; //ime_skole\\
    protected String upisanaZelja; //redni_broj_u_listi_zelja
    protected String krug;

    public String getOsnovnaSkola() {
        return osnovnaSkola;
    }

    public String getMestoOS() {
        return mestoOS;
    }

    public String getOkrugOS() {
        return okrugOS;
    }

    public Map<String, String> getSestiRaz() {
        return sestiRaz;
    }

    public Map<String, String> getSedmiRaz() {
        return sedmiRaz;
    }

    public Map<String, String> getOsmiRaz() {
        return osmiRaz;
    }

    public Map<String, String> getTakmicenja() {
        return takmicenja;
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

    public String getUkupnoBodova() {
        return ukupnoBodova;
    }

    public List<Skola> getListaZelja() {
        return listaZelja;
    }

    public Skola getUpisanaSkola() {
        return upisanaSkola;
    }

    public String getUpisanaZelja() {
        return upisanaZelja;
    }

    public String getKrug() {
        return krug;
    }

    /**
     * Returns whether this Ucenik exists locally (on disk).
     * This is an expensive operation!
     * @return
     */
    public boolean exists() {
        if(exists == null) exists = new File(DownloadController.DATA_FOLDER, id).exists();
        return exists;
    }

    public Ucenik(String id) {
        this.id = id;
    }

    public Ucenik setDetails(String ukBodova, String mestoOS) {
        this.ukupnoBodova = ukBodova;
        this.mestoOS = mestoOS;
        return this;
    }

    /**
     * Ucitava podatke o uceniku s neta
     * @return
     * @throws IOException
     */
    public Ucenik loadFromNet() throws IOException {
        if (Main.DEBUG) {
            System.out.println("loading ucenik: " + id);
        }
        if (exists && !OVERWRITE_OLD) {
            return this;
        }
        if(PRINT_MISSING && Smerovi.getInstance().getCurrentIndex()<2355)
            System.out.println("missing");
        Document doc = Jsoup.connect(UCENICI_URL + id).post();
        osnovnaSkola = doc.select(".ospod_malo_naznaceno .home_link").text().trim();
        okrugOS = doc.select(".ospod_malo_naznaceno").stream()
                .filter((Element e) -> UcenikUtils.isAllCaps(e.text()))
                .collect(Collectors.toList())
                .get(0)
                .text()
                .trim();
        sestiRaz = loadOcene("6");
        sedmiRaz = loadOcene("7");
        osmiRaz = loadOcene("8");
        takmicenja = loadTakmicenja();
        Elements test = doc.select(".osnovna_kvalifikacioni_naznaceno2");
        for (Element test1 : test) {
            if (!test1.text().equals("\u00a0")) {
                if (matematika == null) {
                    matematika = test1.text();
                } else if (srpski == null) {
                    srpski = test1.text();
                } else if (kombinovani == null) {
                    kombinovani = test1.text();
                }
            }
        }
        listaZelja = loadZelje(1);
        String skola = doc.select(".status_ucenika").get(2).text();
        String skolaPodaci = skola.split("\\. ", 2)[1];
        String[] sifraOstalo = skolaPodaci.split("\\Q - \\E", 2);
        String[] ostalo = sifraOstalo[1].split(",");
        upisanaSkola = new Skola(sifraOstalo[0], ostalo[0], ostalo[1], ostalo[2]);
        upisanaZelja = skola.split("\\. ", 2)[0];
        /*String krugText = doc.select(".status_ucenika").get(1).text();
        if(krugText.contains("PRVOM")) krug="1";
        else if(krugText.contains("DRUGOM")) krug="2";
        else if(krugText.contains("OUK")) krug="0";*/ //ovo bi bilo idealno, ali ne živimo u idealnom svetu
        krug = doc.select(".status_ucenika").get(1).text().contains("PRVOM") ? "1" : "2";
        if (Main.DEBUG) {
            System.out.println("loaded ucenik: " + id);
        }
        return this;
    }

    private Map<String, String> loadOcene(String razred) throws IOException {
        Map<String, String> m = new HashMap<>();
        Document doc = Jsoup.connect(UCENICI_URL + id + "&razred_id=" + razred).post();
        Elements ocene = doc.select(".ocene_razred[width=200] tr");
        ocene.remove(doc.select(".ocene_razred_header").first().parent());
        ocene.forEach((Element e) -> {
            m.put(e.select(".ocene_razred_normal").text(), e.select(".ocene_razred_naglaseno").text());
        });
        return m;
    }

    private Map<String, String> loadTakmicenja() throws IOException {
        Map<String, String> m = new HashMap<>();
        Document doc = Jsoup.connect(UCENICI_URL + id + "&view_details=1").post();
        Elements takm = doc.select(".nagrade tr");
        takm.remove(doc.select(".nagrade_header").first().parent());
        if (!takm.text().equals("Nema podataka o osvojenim nagradama")) {
            takm.stream().map((takm1) -> takm1.select(".nagrade_normal")).forEach((nagrada) -> {
                m.put(nagrada.get(0).text(), nagrada.get(3).text());
            });
        }
        return m;
    }

    private List<Skola> loadZelje(int krug) throws IOException {
        List<Skola> l = new ArrayList<>();
        Document doc = Jsoup.connect(UCENICI_URL + id + "&view_details=" + (krug+1)).post();
        Elements zelje = doc.select(".zelje_normal");
        zelje.forEach((zelja) -> {
            try {
                String[] sifraOstalo = zelja.text().split("\\. ", 2)[1].split("\\Q - \\E", 2);
                String[] ostalo = sifraOstalo[1].split(",");
                l.add(new Skola(sifraOstalo[0], ostalo[0], ostalo[1], ostalo[2]));
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println("Invalid zelja: " + zelja.text());
                System.exit(2);
            }
        });
        return l;
    }

    /**
     * Ucitava podatke o Uceniku iz fajla
     * @param folder folder u kome se nalaze podaci o ucenicima
     * @return
     */
    public Ucenik loadFromFile(File folder) {
        File f = new File(folder, id);
        try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            loadFromString(text);
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    /**
     * Cuva ovog ucenika u fajl
     * @param folder folder u kom treba sacuvati ucenika
     */
    public void saveToFile(File folder) {
        if (Main.DEBUG) {
            System.out.println("saving ucenik: " + id);
        }
        if (exists && !OVERWRITE_OLD) {
            return;
        }
        if(Main.REDOING_DOWNLOAD)
            System.out.println("Found missing! " + id);
        File f = new File(folder, id);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            fw.write(this.toCompactString());
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (Main.DEBUG) {
            System.out.println("saved ucenik: " + id);
        }
    }

    public void loadFromString(String compactString) {
        StringTokenizer master = new StringTokenizer(compactString, '\n', true);
        //String[] chunks = compactString.split("\\n");

        StringTokenizer inner = new StringTokenizer(master.nextToken(), '\\', true);
        osnovnaSkola = inner.nextToken();
        mestoOS = inner.nextToken();
        okrugOS = inner.nextToken();
        //String[] basics = chunks[0].split("\\\\");

        loadPredmetiMap(sestiRaz, master.nextToken());
        loadPredmetiMap(sedmiRaz, master.nextToken());
        loadPredmetiMap(osmiRaz, master.nextToken());
        //String[] sesti = chunks[1].split("\\\\", 0);
        //String[] sedmi = chunks[2].split("\\\\", 0);
        //String[] osmi = chunks[3].split("\\\\", 0);

        loadPredmetiMap(takmicenja, master.nextToken());
        //String[] takm = chunks[4].split("\\\\", 0);

        inner = new StringTokenizer(master.nextToken(), '\\', true);
        matematika = inner.nextToken();
        srpski = inner.nextToken();
        kombinovani = inner.nextToken();
        //String[] test = chunks[5].split("\\\\");

        ukupnoBodova = master.nextToken();
        //String ukupno = chunks[6].split("\\\\")[0];

        inner = new StringTokenizer(master.nextToken(), '\\', false);
        while(inner.hasMoreTokens())
            listaZelja.add(Skola.makeSkola(inner.nextToken()));
        //String[] zelje = chunks[7].split("\\\\", 0);

        inner = new StringTokenizer(master.nextToken(), '\\', true);
        upisanaSkola = Skola.makeSkola(inner.nextToken());
        upisanaZelja = inner.nextToken();
        krug = inner.nextToken();
        //String[] upisano = chunks[8].split("\\\\");

        //Profiler.addTime("UcenikLoadFromStringOld", end-start);

        //osnovnaSkola = basics[0];
        //mestoOS = basics[1];
        //okrugOS = basics[2];
        //sestiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(sesti));
        //sedmiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(sedmi));
        //osmiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(osmi));
        //takmicenja = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(takm));
        /*matematika = test[0];
        srpski = test[1];
        kombinovani = test[2];*/
        //ukupnoBodova = ukupno;
        //listaZelja = UcenikUtils.stringToList(zelje);
        /*upisanaSkola = new Skola(upisano[0]);
        upisanaZelja = upisano[1];
        krug = upisano[2];*/
    }

    private void loadPredmetiMap(Map<String, String> to, String from) {
        if(UcenikUtils.PredmetiDefault.inverse == null) UcenikUtils.PredmetiDefault.initInverse();

        StringTokenizer predmeti = new StringTokenizer(from, '\\', false);
        while(predmeti.hasMoreTokens()) {
            String pr = predmeti.nextToken();
            String ocena = String.valueOf(pr.charAt(pr.length()-1));
            String predmet = pr.substring(0, pr.indexOf(':'));
            predmet = UcenikUtils.PredmetiDefault.inverse.getOrDefault(predmet, predmet);
            to.put(predmet, ocena);
        }
    }

    public String toCompactString() {
        StringBuilder compactString = new StringBuilder();
        compactString.append(osnovnaSkola).append("\\").append(mestoOS).append("\\").append(okrugOS).append("\n");
        compactString.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sestiRaz)));
        compactString.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(sedmiRaz)));
        compactString.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(osmiRaz)));
        compactString.append(UcenikUtils.mapToStringBuilder(UcenikUtils.PredmetiDefault.compress(takmicenja)));
        compactString.append(matematika).append("\\").append(srpski).append("\\").append(kombinovani).append("\n");
        compactString.append(ukupnoBodova).append("\n");
        compactString.append(UcenikUtils.listToStringBuilder(listaZelja));
        compactString.append(upisanaSkola).append("\\").append(upisanaZelja).append("\\").append(krug);
        return compactString.toString();
    }

    public static class Skola {

        private static Map<String, Skola> cache = new HashMap<>();

        public static void clearCache() {
            cache.clear();
        }
        public final String sifra;
        public final String ime;
        public String mesto;
        public String smer;

        Skola(String sifra, String ime, String mesto, String smer) {
            this.sifra = sifra;
            this.ime = ime;
            this.mesto = mesto;
            this.smer = smer;
        }

        static Skola makeSkola(String compactString) {
            StringTokenizer tk = new StringTokenizer(compactString, ',', true);
            String sifra = tk.nextToken().toUpperCase();
            if(cache.containsKey(sifra)) return cache.get(sifra);
            else {
                Skola sk = new Skola(sifra, tk);
                cache.put(sifra, sk);
                return sk;
            }
        }
        Skola(String sifra, StringTokenizer tk) {
            this.sifra = sifra;
            ime = tk.nextToken();
            mesto = tk.nextToken();
            if(mesto.charAt(0) == ' ') mesto = mesto.substring(1);
            smer = tk.nextToken();
            if(smer.charAt(0) == ' ') smer = smer.substring(1);
        }

        @Override
        public String toString() {
            return sifra + "," + ime + "," + mesto + "," + smer;
        }
    }
}
