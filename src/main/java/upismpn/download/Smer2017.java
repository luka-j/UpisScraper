package upismpn.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luka on 2.7.17.
 */
public class Smer2017 extends Smer {
    protected String ime, jezik, opstina, okrug, podrucje;

    protected String json;

    public Smer2017(String sifra, String ime, String smer, String jezik, String kvota) {
        super(sifra, smer, kvota);
        this.ime = ime;
        this.jezik = jezik;
    }

    public Smer2017(String compactString) {
        super(compactString);
        String[] tokens = compactString.split("\\\\");
        ime = tokens[3];
        jezik = tokens[4];
        opstina = tokens[5];
        okrug = tokens[6];
        podrucje = tokens[7];
    }

    public String toCompactString() {
        StringBuilder str = new StringBuilder(super.toCompactString());
        str.deleteCharAt(str.length()-1); //removing newline
        str.append("\\").append(ime).append("\\").append(jezik).append("\\")
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
}
