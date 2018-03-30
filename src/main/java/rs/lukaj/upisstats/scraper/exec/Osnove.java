package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroup;
import rs.lukaj.upisstats.scraper.obrada.UcenikWrapper;
import rs.lukaj.upisstats.scraper.obrada2017.UceniciBase;
import rs.lukaj.upisstats.scraper.obrada2017.UcenikW;

import java.util.function.ToDoubleFunction;

public class Osnove {
    public static void brojUcenika() {
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("17");
        System.out.println("2017: " + UceniciBase.svi().count());

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("16");
        System.out.println("2016: " + UceniciGroup.svi().size());

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("15");
        System.out.println("2015: " + UceniciGroup.svi().size());
    }

    public static void prosekOcena() {
        prosekSvi(uc -> uc.prosekUkupno, uc -> uc.prosekUkupno);
    }

    public static void prosekZavrsni() {
        System.out.println("Ukupno: ");
        prosekSvi(uc -> uc.bodoviSaZavrsnog, uc -> uc.bodovaZavrsni);

        System.out.println("Srpski: ");
        prosekSvi(uc -> uc.srpski, uc -> uc.srpski);

        System.out.println("Matematika: ");
        prosekSvi(uc -> uc.matematika, uc -> uc.matematika);

        System.out.println("Kombinovani: ");
        prosekSvi(uc -> uc.kombinovani, uc -> uc.kombinovani);
    }

    private static void prosekSvi(ToDoubleFunction<UcenikWrapper> mapperOld, ToDoubleFunction<UcenikW> mapperNew) {
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("17");
        System.out.println("2017: " + UceniciBase.svi()
                .mapToDouble(mapperNew)
                .average().orElse(0));

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("16");
        System.out.println("2016: " + UceniciGroup.svi().stream()
                .mapToDouble(mapperOld)
                .average().orElse(0));

        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("15");
        System.out.println("2015: " + UceniciGroup.svi().stream()
                .mapToDouble(mapperOld)
                .average().orElse(0));
    }
}
