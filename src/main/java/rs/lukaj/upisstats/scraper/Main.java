package rs.lukaj.upisstats.scraper;

import rs.lukaj.upisstats.scraper.download.DownloadConfig;
import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik2017;
import rs.lukaj.upisstats.scraper.download.misc.OsnovneDownloader;
import rs.lukaj.upisstats.scraper.obrada.Exec;

import java.io.IOException;

/**
 *
 * @author Luka
 */
public class Main {

    public static final boolean DEBUG            = false;
    public static final boolean REDOING_DOWNLOAD = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1)
            System.out.println("invalid args. Valid args: dl & exec [method name]");
        else {
            if(args[0].equalsIgnoreCase("dl") &&
                    (args.length == 1 || args[1].equalsIgnoreCase("uc")))
                DownloadController.startDownload(new DownloadConfig.New());
            if(args.length == 2 &&
                    args[0].equalsIgnoreCase("dl") && args[1].equalsIgnoreCase("os"))
                OsnovneDownloader.downloadOsnovneData();
            if(args[0].equalsIgnoreCase("exec")) {
                String[] semPrvog = new String[args.length-1];
                System.arraycopy(args, 1, semPrvog, 0, semPrvog.length);
                Exec.doExec(semPrvog);
            }
        }
    }

}
