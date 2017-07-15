package upismpn.download;

/**
 * Predstavlja podatke o jednom smeru; sifru, podrucje i kvotu
 * @author Luka
 */
public class Smer {
    private final String sifra;
    private final String podrucje;
    private final String kvota;
    
    public Smer(String sifra, String podrucje, String kvota) {
        this.sifra = sifra.trim();
        this.podrucje = podrucje;
        this.kvota = kvota;
    }
    
    public Smer(String compactString) {
        String[] tokens = compactString.split("\\\\");
        sifra = tokens[0];
        podrucje = tokens[1];
        kvota = tokens[2];
    }
    
    public String getSifra() {return sifra;}
    public String getPodrucje() {return podrucje;}
    public String getKvota() {return kvota;}
    public String toCompactString() {return sifra.trim() + "\\" + podrucje + "\\" + kvota + "\n";}
}
