package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroup;
import rs.lukaj.upisstats.scraper.obrada2017.UceniciBase;
import rs.lukaj.upisstats.scraper.utils.Profiler;

public class Osnove {
    public static void brojUcenika() {
        long start = System.nanoTime();
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("15");
        System.out.println("2015: " + UceniciGroup.svi().size());
        long end = System.nanoTime();
        Profiler.addTime("2015 total", end-start);

        start = System.nanoTime();
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("16");
        System.out.println("2016: " + UceniciGroup.svi().size());
        end = System.nanoTime();
        Profiler.addTime("2016 total", end-start);

        start = System.nanoTime();
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("17");
        System.out.println("2017: " + UceniciBase.svi().count());
        end = System.nanoTime();
        Profiler.addTime("2017 total", end-start);

        System.out.println("\n");
        Profiler.print();
    }
}
