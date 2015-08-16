package upismpn;

import upismpn.download.DownloadController;
import upismpn.obrada.Exec;

/**
 *
 * @author Luka
 */
public class UpisMpn {

    public static final boolean DEBUG = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1)
            System.out.println("invalid args. Valid args: dl & exec [method name]");
        else {
            if(args[0].equalsIgnoreCase("dl"))
                DownloadController.startDownload();
            if(args[0].equalsIgnoreCase("exec"))
                Exec.doExec(args[1]);
        }
    }
    

}
