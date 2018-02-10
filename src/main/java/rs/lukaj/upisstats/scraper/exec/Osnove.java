package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroup;
import rs.lukaj.upisstats.scraper.obrada2017.UceniciBase;

public class Osnove {
    public static void brojUcenika() {
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("17");
        System.out.println("2017: " + UceniciBase.svi().count());

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("16");
        System.out.println("2016: " + UceniciGroup.svi().size());

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("15");
        System.out.println("2015: " + UceniciGroup.svi().size());
    }
}
