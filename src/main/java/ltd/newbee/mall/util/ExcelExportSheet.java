package ltd.newbee.mall.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * xls 导出 单个 Sheet
 * */
public class ExcelExportSheet {

    public static ExcelExportSheet instances(String  name){
        ExcelExportSheet sheet = new ExcelExportSheet();
        return sheet.setName( name );
    }

    /**
     * 表格名称
     * */
    private String name;

    /**
     * 表头: <p>
     * key : 对应的字段名称<br>
     * val : 对应的 表头名称
     * */
    private Map<String , String> keys = new LinkedHashMap<>();
    /**
     * 隐藏的字段，对应表头名称
     */
    private Map<String , String> hideKeys = new LinkedHashMap<>();

    /**
     * 数据
     * */
    private List<? >  datas =  new ArrayList<>();

    public ExcelExportSheet addKey(String filename, String columnName ){
        keys.put( filename, columnName);
        return this;
    }

    public String getName() {
        return name;
    }

    public ExcelExportSheet setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, String> getKeys() {
        return keys;
    }

    public ExcelExportSheet setKeys(Map<String, String> keys) {
        this.keys = keys;
        return this;
    }

    public List<?> getDatas() {
        return datas;
    }

    public ExcelExportSheet setDatas(List<?> datas) {
        this.datas = datas;
        return this;
    }

    public ExcelExportSheet addHideKey(String filename, String columnName ){
        hideKeys.put( filename, columnName);
        return this;
    }

    public Map<String, String> getHideKeys() {
        return hideKeys;
    }

    public void setHideKeys(Map<String, String> hideKeys) {
        this.hideKeys = hideKeys;
    }
}
