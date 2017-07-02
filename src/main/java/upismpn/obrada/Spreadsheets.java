package upismpn.obrada;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import upismpn.exec.Teritorijalno;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Operacije s tabelama. Koristi Apache POI za kreiranje tabela
 * Created by Luka on 1/4/2016.
 */
public class Spreadsheets {
    /**
     * Rado bih pruzio neku smislenu dokumentaciju, ali ovo sam izvukao iz nekog starog projekta,
     * skoro doslovno kopirano
     * @param outFile izlazni fajl
     * @param out izlazna matrica
     * @throws IOException
     */
    public static void writeXSSF(File outFile, String[][] out) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet    sheet    = workbook.createSheet();
        for (int j = 0; j < out.length; j++) {
            Row row = sheet.createRow(j);
            for (int k = 0; k < out[j].length; k++) {
                Cell cell = row.createCell(k);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(out[j][k]);
            }
        }
        for (int i = 0; i < out[0].length; i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream fos = new FileOutputStream(outFile);
        workbook.write(fos);
        fos.close();
    }

    /**
     * Utility for {@link upismpn.exec.Teritorijalno.StringGroup}
     * @param outFile
     * @param list
     * @throws IOException
     */
    public static void writeStringGroup(File outFile, List<Teritorijalno.StringGroup> list, String val) throws IOException {
        String[][] out = new String[list.size()+1][3];
        out[0][0] = "Mesto"; out[0][1] = val; out[0][2] = "Broj ucenika";
        for(int i=0; i<list.size(); i++) {
            out[i+1][0] = list.get(i).getStr();
            out[i+1][1] = String.valueOf(list.get(i).getVal());
            out[i+1][2] = String.valueOf(list.get(i).getGroup().size());
        }
        writeXSSF(outFile, out);
    }
}
