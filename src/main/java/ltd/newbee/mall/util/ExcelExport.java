package ltd.newbee.mall.util;

import java.util.ArrayList;
import java.util.List;

public class ExcelExport {

    public static ExcelExport instances(String  fileName){
        ExcelExport excel = new ExcelExport();
        return excel.setFileName( fileName );
    }


    private String fileName;
    private List<ExcelExportSheet> sheets = new ArrayList<>();

    public ExcelExport add(ExcelExportSheet sheet){
        sheets.add( sheet );
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ExcelExport setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public List<ExcelExportSheet> getSheets() {
        return sheets;
    }

    public ExcelExport setSheets(List<ExcelExportSheet> sheets) {
        this.sheets = sheets;
        return this;
    }
}
