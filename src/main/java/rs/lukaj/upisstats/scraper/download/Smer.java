package rs.lukaj.upisstats.scraper.download;

import rs.lukaj.upisstats.scraper.obrada2017.LetterUtils;
import rs.lukaj.upisstats.scraper.utils.StringTokenizer;

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
        StringTokenizer tk = new StringTokenizer(compactString, '\\', true);
        sifra = tk.nextToken().toUpperCase();
        podrucje = LetterUtils.toLatin(tk.nextToken().trim()).toLowerCase();
        kvota = tk.nextToken().trim();
    }
    
    public String getSifra() {return sifra;}
    public String getPodrucje() {return podrucje;}
    public String getKvota() {return kvota;}
    public String toCompactString() {return sifra.trim() + "\\" + podrucje + "\\" + kvota + "\n";}
}
