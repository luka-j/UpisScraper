package upismpn.download;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

import static upismpn.UpisMpn.DEBUG;

/**
 * Predstavlja Ucenika u obliku pogodnom za preuzimanje s neta i cuvanje u fajl (Stringovi)
 */
public class Ucenik {

    protected static final boolean OVERWRITE_OLD = true;
    private static final boolean PRINT_MISSING = false;
    protected boolean exists = false;

    private static final String UCENICI_URL = "http://195.222.98.40/ucenik_info.php?id_ucenika=";

    public final String id; //filename

    protected String osnovnaSkola; //ime_skole
    protected String mestoOS; //ime_mesta\\
    protected String okrugOS; //ime_okruga\n

    protected Map<String, String> sestiRaz; //predmet:ocena\\predmet:ocena... \n
    protected Map<String, String> sedmiRaz; //predmet:ocena\\predmet:ocena... \n
    protected Map<String, String> osmiRaz; //predmet:ocena\\predmet:ocena... \n

    protected Map<String, String> takmicenja = new HashMap<>(); //predmet:bodova\\predmet:bodova... \n

    protected String matematika; //broj_bodova\\
    protected String srpski; //broj_bodova\\
    protected String kombinovani; //broj_bodova\n

    /**
     * !! Zajedno sa prijemnim.
     */
    protected String ukupnoBodova; //broj_bodova\n

    protected List<Skola> listaZelja; //skola_1\\skola_2\\skola_3... \n
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

    public Ucenik(String id) {
        this.id = id;
        exists = new File(DownloadController.DATA_FOLDER, id).exists();
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
        if (DEBUG) {
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
        listaZelja = loadZelje();
        String skola = doc.select(".status_ucenika").get(2).text();
        String skolaPodaci = skola.split("\\. ", 2)[1];
        String[] sifraOstalo = skolaPodaci.split("\\Q - \\E", 2);
        String[] ostalo = sifraOstalo[1].split(",");
        upisanaSkola = new Skola(sifraOstalo[0], ostalo[0], ostalo[1], ostalo[2]);
        upisanaZelja = skola.split("\\. ", 2)[0];
        krug = doc.select(".status_ucenika").get(1).text().contains("PRVOM") ? "1" : "2";
        if (DEBUG) {
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

    private List<Skola> loadZelje() throws IOException {
        List<Skola> l = new ArrayList<>();
        Document doc = Jsoup.connect(UCENICI_URL + id + "&view_details=2").post();
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
        if (DEBUG) {
            System.out.println("saving ucenik: " + id);
        }
        if (exists && !OVERWRITE_OLD) {
            return;
        }
        File f = new File(folder, id);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            fw.write(this.toCompactString());
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (DEBUG) {
            System.out.println("saved ucenik: " + id);
        }
    }

    public void loadFromString(String compactString) {
        String[] chunks = compactString.split("\\n");

        String[] basics = chunks[0].split("\\\\");
        String[] sesti = chunks[1].split("\\\\", 0);
        String[] sedmi = chunks[2].split("\\\\", 0);
        String[] osmi = chunks[3].split("\\\\", 0);
        String[] takm = chunks[4].split("\\\\", 0);
        String[] test = chunks[5].split("\\\\");
        String ukupno = chunks[6].split("\\\\")[0];
        String[] zelje = chunks[7].split("\\\\", 0);
        String[] upisano = chunks[8].split("\\\\");

        osnovnaSkola = basics[0];
        mestoOS = basics[1];
        okrugOS = basics[2];
        sestiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(sesti));
        sedmiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(sedmi));
        osmiRaz = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(osmi));
        takmicenja = UcenikUtils.PredmetiDefault.decompress(UcenikUtils.stringArrayToMap(takm));
        matematika = test[0];
        srpski = test[1];
        kombinovani = test[2];
        ukupnoBodova = ukupno;
        listaZelja = UcenikUtils.stringToList(zelje);
        upisanaSkola = new Skola(upisano[0]);
        upisanaZelja = upisano[1];
        krug = upisano[2];
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

        public final String sifra;
        public final String ime;
        public final String mesto;
        public final String smer;

        Skola(String sifra, String ime, String mesto, String smer) {
            this.sifra = sifra;
            this.ime = ime;
            this.mesto = mesto;
            this.smer = smer;
        }

        Skola(String compactString) {
            String[] tokens = compactString.split(",");
            sifra = tokens[0];
            ime = tokens[1];
            mesto = tokens[2];
            smer = tokens[3];
        }

        @Override
        public String toString() {
            return sifra + "," + ime + "," + mesto + "," + smer;
        }
    }
}
