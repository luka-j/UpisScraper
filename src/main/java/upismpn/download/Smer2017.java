package upismpn.download;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import upismpn.obrada2017.LetterUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luka on 2.7.17.
 */
public class Smer2017 extends Smer {
    protected String ime;
    protected String jezik;
    protected String opstina;
    protected String okrug;
    protected String podrucje;
    protected String trajanje;
    protected String kvotaUmanjenje;

    protected String json;



    public String getIme() {
        return ime;
    }

    public String getJezik() {
        return jezik;
    }

    public String getOpstina() {
        return opstina;
    }

    public String getOkrug() {
        return okrug;
    }

    public String getTrajanje() {
        return trajanje;
    }

    //let's pretend this isn't godawful
    //well this ain't supposed to look pretty, it's supposed to work fast
    //will worry about that in Exec/obrada
    public String getPodrucje2017() {
        return podrucje;
    }

    public String getJson() {
        return json;
    }

    public Smer2017(String sifra, String ime, String smer, String jezik, String kvota) {
        super(sifra, smer, kvota);
        this.ime = ime;
        this.jezik = jezik;
    }

    public Smer2017(String compactString) {
        super(compactString);
        String[] tokens = compactString.split("\\\\");
        ime = LetterUtils.toLatin(tokens[3]);
        trajanje = tokens[4];
        kvotaUmanjenje = tokens[5];
        jezik = LetterUtils.toLatin(tokens[5]);
        opstina = LetterUtils.toLatin(tokens[6]);
        okrug = LetterUtils.toLatin(tokens[7]);
        podrucje = LetterUtils.toLatin(tokens[8]);
    }

    public String toCompactString() {
        StringBuilder str = new StringBuilder(super.toCompactString());
        str.deleteCharAt(str.length()-1); //removing newline
        if(ime.endsWith(",")) ime = ime.substring(0, ime.length()-1);
        str.append("\\").append(ime).append("\\").append(trajanje).append("\\")
                .append(kvotaUmanjenje).append("\\").append(jezik.trim()).append("\\")
                .append(opstina).append("\\").append(okrug).append("\\").append(podrucje).append("\n");
        return str.toString();
    }

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    public void setDetails(String json, String opstina, String okrug, String podrucje) {
        this.json = json;
        this.opstina = opstina;
        this.okrug = okrug;
        this.podrucje = podrucje;
        executor.execute(() -> {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Smerovi.SMEROVI_FOLDER, getSifra() + ".json")));
                bw.write(json);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Smerovi2017.getInstance().save(); //not the finest solution
        });
    }

    public void exetendFromJson() {
        try {
            JsonParser parser = new JsonParser();
            trajanje = (parser.parse(Files.readAllLines(new File(Smerovi.SMEROVI_FOLDER, getSifra() + ".json").toPath()).stream().reduce("", (a, b)->a+b)).getAsJsonArray().get(0).getAsJsonObject().get("Trajanje").getAsString()); //I'm not it the mood, mkay?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromJson() {
        try {
            JsonParser parse = new JsonParser();
            JsonObject json = parse.parse(Files.readAllLines(new File(Smerovi.SMEROVI_FOLDER, getSifra() + ".json").toPath()).get(0)).getAsJsonArray().get(0).getAsJsonObject();
            ime = json.get("NazivSkole1").getAsString();
            jezik = SmerMappingTools.getJezik(Integer.parseInt(json.get("IDJezik").getAsString()));
            opstina = SmerMappingTools.getOpstina(Integer.parseInt(json.get("IDOpstina").getAsString()));
            okrug = SmerMappingTools.getOkrug(Integer.parseInt(json.get("IDOkrug").getAsString()));
            kvotaUmanjenje = json.get("KvotaUmanjenje").getAsString();
            trajanje = json.get("Trajanje").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getKvotaUmanjenje() {
        return kvotaUmanjenje;
    }
}
