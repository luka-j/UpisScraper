package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.DownloadConfig;
import rs.lukaj.upisstats.scraper.download.Smerovi2017;
import rs.lukaj.upisstats.scraper.download.UceniciDownloader2017;
import rs.lukaj.upisstats.scraper.download.UceniciManager;

import java.util.Deque;

public class TestDL {
    public static void testDL() {
        Smerovi2017.getInstance().load();
        Deque<UceniciManager.UcData> data = UceniciDownloader2017.getInstance(0, 0).getSifreUcenika("BGSG GA 4R04S");
        UceniciManager dler = new DownloadConfig.New().getUceniciManager();
        dler.add(data);
        dler.download();
        dler.new Saver().run();
    }
}
