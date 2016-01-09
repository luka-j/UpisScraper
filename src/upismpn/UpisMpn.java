package upismpn;

import upismpn.download.DownloadController;
import upismpn.exec.Zavrsni;
import upismpn.obrada.Exec;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class UpisMpn {

    //(kada je i Exec deo executableClasses), detaljnije objasnjenje nemam; verovatno zato sto prvo radi <clinit> u ovoj
    //klasi (iako ne mora)
    //todo naci resenje koje je elegantno, ali se oslanja na nesto sto je zapravo garantovano da uvek radi

    public static final boolean    DEBUG             = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1)
            System.out.println("invalid args. Valid args: dl & exec [method name]");
        else {
            if(args[0].equalsIgnoreCase("dl"))
                DownloadController.startDownload();
            if(args[0].equalsIgnoreCase("exec")) {
                String[] semPrvog = new String[args.length-1];
                System.arraycopy(args, 1, semPrvog, 0, semPrvog.length);
                Exec.doExec(semPrvog);
            }
        }
    }

}
