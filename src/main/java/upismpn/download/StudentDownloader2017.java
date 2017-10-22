package upismpn.download;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import upismpn.UpisMpn;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static upismpn.UpisMpn.DEBUG;

/**
 * Created by luka on 3.7.17..
 */
public class StudentDownloader2017 extends StudentDownloader {
    private StudentDownloader2017(int startingIndex, long time) {
        super(startingIndex, time);
    }

    private static StudentDownloader2017 instance;

    public static StudentDownloader2017 getInstance(int startingIndex, long time) {
        if(instance == null) instance = new StudentDownloader2017(startingIndex, time);
        return instance;
    }

    @Override
    public Deque<UceniciManager.UcData> getSifreUcenika(String sifraProfila) {
        Deque<UceniciManager.UcData> sifre = new ArrayDeque<>();
        boolean end = false;
        try {
            Document whole = null;
            while(whole==null) whole = downloadDoc(generateSmerUrl(sifraProfila), "", false);
            downloadSmerDetails(whole);

            Document doc;
            UceniciManager.UcData data;
            int i = 1;
            while (!end) {
                do{
                    if(DEBUG)System.out.println("downloading doc " + i + " za " + sifraProfila);
                    doc = downloadDoc(generateSmerUrl(sifraProfila), generatePageParams(i), true);
                } while(doc == null);
                if(DEBUG)System.out.println("starting download of ucenici");
                Elements elSifre = doc.select(".tbody .kolona1");
                Elements bodovi = doc.select(".tbody .kolona3");
                Elements krugovi = doc.select(".tbody .kolona4");
                if(elSifre.isEmpty()) end=true;
                else {
                    for(int j=0; j<elSifre.size(); j++) {
                        data = new UceniciManager.UcData(elSifre.get(j).text(), bodovi.get(j).text(), krugovi.get(j).text());
                        sifre.add(data);
                        if(DEBUG)System.out.print("added new ucenik: " + data);
                    }
                }
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(UpisMpn.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sifre;
    }

    private void downloadSmerDetails(Document doc) {
        Elements scripts = doc.getElementsByTag("script");
        String script = scripts.get(scripts.size()-3).data();
        String json = script.trim().split(";", 2)[0].split(" = ", 2)[1];
        JsonObject data = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
        String sifra = data.get("sifra").getAsString();
        String opstina = SmerMappingTools.getOpstina(Integer.parseInt(data.get("IDOpstina").getAsString()));
        String okrug = SmerMappingTools.getOkrug(Integer.parseInt(data.get("IDOkrug").getAsString()));
        String podrucje = SmerMappingTools.getPodrucje(Integer.parseInt(data.get("IDPodrucjeRada").getAsString()));
        Smerovi2017.getInstance().get(sifra).setDetails(json, opstina, okrug, podrucje);
    }

    private static String generateSmerUrl(String sifra) {
        return "http://upis.mpn.gov.rs/Cir/Srednje-skole/" + sifra.replace(' ', '-');
    }
    private static String generatePageParams(int page) {
        return "id_grid=wuc_Grid1&grid_refresh=1&filter=&sort=&page=" + page + "&page_size=-1&IDPocetniFilter=0&IDStalniFilter=0&multiselect=0&Pretraga=&executeUCMethod=wuc_Grid%3FDBID%3D9%26ID%3Dwuc_Grid1%26PageSize%3D-1%26ClientMode%3D1&methodName=InitGrid";
    }
}
