# Upis15Crawler
Crawls upin.mpn.gov.rs for data. `Exec.java` could use some work.

Preuzima:
- Rezultate na završnom (broj bodova na svakom testu)
- Rezultate na prijemnom, ako je učenik upisao neku od škola za koje se polaže poseban ispit
- Ocene od šestog razreda
- Uspeh na državnim i međunarodnim takmičenjima (predmet i mesto)
- Podaci o osnovnoj školi: ime, mesto, okrug
- Podaci o upisanoj školi: ime, mesto, okrug, smer, područje rada, kvota
- Lista želja
- Krug u kojem je učenik upisan

Generiše oko 60k fajlova, za obradu je preporučljivo spojiti ih u jedan koji je velik 66,7MB koristeći metode iz FileMerger klase.
