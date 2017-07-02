package upismpn.download;

/**
 * Created by luka on 2.7.17.
 */
public class Smer2017 extends Smer {
    private String ime, jezik;

    public Smer2017(String sifra, String ime, String podrucje, String jezik, String kvota) {
        super(sifra, podrucje, kvota);
        this.ime = ime;
        this.jezik = jezik;
    }

    public Smer2017(String compactString) {
        super(compactString);
        String[] tokens = compactString.split("\\\\");
        ime = tokens[3];
        jezik = tokens[4];
    }

    public String toCompactString() {
        StringBuilder str = new StringBuilder(super.toCompactString());
        str.deleteCharAt(str.length()-1); //removing newline
        str.append("\\").append(ime).append("\\").append(jezik).append("\n");
        return str.toString();
    }
}
