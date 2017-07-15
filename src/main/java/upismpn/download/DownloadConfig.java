package upismpn.download;

/**
 * Created by luka on 2.7.17..
 */
public abstract class DownloadConfig {
    public void loadSmerovi() {
        getSmerovi().load();
    }
    public void saveSmerovi() {
        getSmerovi().save();
    }
    public void loadOsnovneIds() {
        OsnovneDownloader2017 downloader = getOsnovneDownloader();
        if(downloader != null)
            downloader.loadIdsFromFile();
    }
    public void downloadOsnovne() {
        OsnovneDownloader2017 downloader = getOsnovneDownloader();
        if(downloader != null)
            downloader.download();
    }

    public abstract StudentDownloader getStudentDownloader(int startingSmer, long startTime);
    public abstract Smerovi getSmerovi();
    public abstract UceniciManager getUceniciManager();
    public abstract Ucenik generateUcenik(String sifra, String ukBodova, String prop);
    public abstract OsnovneDownloader2017 getOsnovneDownloader();
    //original OsnovneDownloader really has nothing to do with anything


    public static class Old extends DownloadConfig {

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


    public static class New extends DownloadConfig {
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
