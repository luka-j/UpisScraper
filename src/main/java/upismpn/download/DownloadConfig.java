package upismpn.download;

/**
 * Created by luka on 2.7.17..
 */
public interface DownloadConfig {
    default void loadSmerovi() {
        getSmerovi().load();
    }
    default void saveSmerovi() {
        getSmerovi().save();
    }
    default void loadOsnovneIds() {
        OsnovneDownloader2017 downloader = getOsnovneDownloader();
        if(downloader != null)
            downloader.loadIdsFromFile();
    }
    default void downloadOsnovne() {
        OsnovneDownloader2017 downloader = getOsnovneDownloader();
        if(downloader != null)
            downloader.download();
    }

    StudentDownloader getStudentDownloader(int startingSmer, long startTime);
    Smerovi getSmerovi();
    UceniciManager getUceniciManager();
    Ucenik generateUcenik(String sifra, String ukBodova, String prop);
    OsnovneDownloader2017 getOsnovneDownloader();
    //original OsnovneDownloader really has nothing to do with anything


    public static class Old implements DownloadConfig {

        @Override
        public StudentDownloader getStudentDownloader(int startingSmer, long startTime) {
            return StudentDownloader.getInstance(startingSmer, startTime);
        }

        @Override
        public Smerovi getSmerovi() {
            return Smerovi.getInstance();
        }

        @Override
        public UceniciManager getUceniciManager() {
            return UceniciManager.getInstance(this);
        }

        @Override
        public Ucenik generateUcenik(String sifra, String ukBodova, String mestoOs) {
            return new Ucenik(sifra).setDetails(ukBodova, mestoOs);
        }

        @Override
        public OsnovneDownloader2017 getOsnovneDownloader() {
            return null; //no way to systematically download osnovne in old version
        }
    }


    public static class New implements DownloadConfig {
        @Override
        public StudentDownloader2017 getStudentDownloader(int startingSmer, long startTime) {
            return StudentDownloader2017.getInstance(startingSmer, startTime);
        }

        @Override
        public Smerovi getSmerovi() {
            return Smerovi2017.getInstance();
        }

        @Override
        public UceniciManager getUceniciManager() {
            return UceniciManager.getInstance(this);
        }

        @Override
        public Ucenik generateUcenik(String sifra, String ukBodova, String krug) {
            return new Ucenik2017(sifra).setDetails(ukBodova, krug);
        }

        @Override
        public OsnovneDownloader2017 getOsnovneDownloader() {
            return OsnovneDownloader2017.getInstance();
        }
    }
}
