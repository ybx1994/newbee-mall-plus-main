package ltd.newbee.mall.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.time.DateFormatUtils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Jerry.Zhao
 * Excel常用类，用于读取上传的Excel和把数据导出Excel文件
 *
 */
public class ExcelUtil {
	private static Logger logger = Logger.getLogger(ExcelUtil.class);

	//导出
	public static void exportExcel(HttpServletResponse response, ExcelExportSheet sheet) throws Exception {
		exportExcel(response,sheet,sheet.getName());
	}

	//导出
	public static void exportExcel(HttpServletResponse response, ExcelExportSheet sheet, String fileName) throws Exception {
		exportExcel(response, ExcelExport.instances( fileName ).add(  sheet ) );
	}

	//导出
	public static void exportExcel(HttpServletResponse response, ExcelExport excel) throws Exception {

		org.apache.poi.ss.usermodel.Workbook workBook = new HSSFWorkbook();
		for (ExcelExportSheet sheet:excel.getSheets()) {
			createSheet( workBook,sheet  );
		}

		String fileName = excel.getFileName();
		fileName =new String(fileName.getBytes("GBK"), "ISO-8859-1");
		response.setContentType("application/msexcel;charset=GBK");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + "." + "xls");
		BufferedOutputStream b = null;
		try {
			OutputStream out = response.getOutputStream();
			b = new BufferedOutputStream(out);
			workBook.write(b);
			b.flush();
		} finally {
			if( null != b ){
				try {
					b.close();
				} catch (IOException ignored) { }
			}

			try {
				workBook.close();
			} catch (IOException ignored) { }
		}
	}

	public static void exportExcel(HttpServletResponse response, org.apache.poi.ss.usermodel.Workbook workBook, ExcelExport excel) throws Exception {
		if(workBook==null){
			workBook = new HSSFWorkbook();
		}
		for (ExcelExportSheet sheet:excel.getSheets()) {
			createSheet( workBook,sheet  );
		}

		String fileName = excel.getFileName();
		String ext = "xls";
		if(fileName.indexOf(".")>-1){
			ext = fileName.substring(fileName.lastIndexOf(".")+1);
			fileName = fileName.substring(0,fileName.lastIndexOf("."));
		}

		fileName =new String(fileName.getBytes("GBK"), "ISO-8859-1");
		response.setContentType("application/msexcel;charset=GBK");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + "." + ext);
		BufferedOutputStream b = null;
		try {
			OutputStream out = response.getOutputStream();
			b = new BufferedOutputStream(out);
			workBook.write(b);
			b.flush();
		} finally {
			if( null != b ){
				try {
					b.close();
				} catch (IOException ignored) { }
			}

			try {
				workBook.close();
			} catch (IOException ignored) { }
		}
	}


	/**
	 * 导出xlsx格式
	 * @param response
	 * @param workBook
	 * @param excel
	 * @throws Exception
	 */
	public static void exportExcelXlsx(HttpServletResponse response, org.apache.poi.ss.usermodel.Workbook workBook, ExcelExport excel) throws Exception {
		if(workBook==null){
			workBook = new HSSFWorkbook();
		}
		for (ExcelExportSheet sheet:excel.getSheets()) {
			createSheet( workBook,sheet  );
		}

		String fileName = excel.getFileName();
		fileName =new String(fileName.getBytes("GBK"), "ISO-8859-1");
		response.setContentType("application/msexcel;charset=GBK");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + "." + "xlsx");
		BufferedOutputStream b = null;
		try {
			OutputStream out = response.getOutputStream();
			b = new BufferedOutputStream(out);
			workBook.write(b);
			b.flush();
		} finally {
			if( null != b ){
				try {
					b.close();
				} catch (IOException ignored) { }
			}

			try {
				workBook.close();
			} catch (IOException ignored) { }
		}
	}

	private static void createSheet(Workbook workBook, ExcelExportSheet sheetBean) {
		org.apache.poi.ss.usermodel.Sheet sheet = workBook.createSheet(sheetBean.getName());

		//-- 创建表头
		Row head = sheet.createRow( 0 );
		AtomicInteger i = new AtomicInteger();
		sheetBean.getKeys().values().forEach( columnName
				-> head.createCell( i.getAndIncrement() ).setCellValue( columnName )
		);

		if(sheetBean.getHideKeys()!=null && !sheetBean.getHideKeys().isEmpty()){
			int column = 0;
			for(String key:sheetBean.getKeys().keySet()){
				if(sheetBean.getHideKeys().containsKey(key)){
					sheet.setColumnHidden(column,true);
				}
				column++;
			}
		}

		//-- 填充数据
		i.set(1);
		sheetBean.getDatas().forEach( obj -> {
			Row row = sheet.createRow( i.getAndIncrement() );
			setRowData( row , obj ,sheetBean.getKeys().keySet() );
		});

	}

	private static void setRowData(Row row , Object rowData, Set<String> keys) {

		if( null == rowData ){
			return;
		}

		AtomicInteger i = new AtomicInteger();
		keys.forEach( fileName -> {
			//-- 创建一个 单元格
			Cell cell = row.createCell(i.getAndIncrement());
			//-- 获得数据
			Object val =  getVal( rowData, fileName );
			if( null == val ){
				if(rowData instanceof com.alibaba.fastjson.JSONObject){
					val = ((JSONObject) rowData).get(fileName);
				}else if(rowData instanceof Map){
					val = ((Map) rowData).get(fileName);
				}
			}
			if( null == val ){
				cell.setCellValue( "" );
				return;
			}

			if (val instanceof Integer) {
				cell.setCellValue( Integer.valueOf( val.toString() ) );
				return;
			}
			if (val instanceof Double) {
				cell.setCellValue(Double.valueOf(val.toString()));
				return;
			}

			if (val instanceof BigDecimal) {
				BigDecimal _bd = new BigDecimal(val.toString());
				_bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				cell.setCellValue(_bd.doubleValue());
				return;
			}

			if (val instanceof Date) {
				cell.setCellValue( (Date)val );
				return;
			}

			cell.setCellValue(val.toString());
		} );
	}

	private static Object getVal(Object obj	, String fileName) {

		//-- 获得对象方法
		String methodName = "get" + Character.toUpperCase(fileName.charAt(0)) + fileName.substring(1);
		Method method = ReflectionUtils.findMethod(obj.getClass(), methodName);
		if( null == method ){
			return null;
		}
		try {
			Object val = method.invoke(obj);
			//-- 日期类型转换
			if (val instanceof Date) {
				String format = DateUtil.YYYY_MM_DD_HHmmss;
				JsonFormat annotation = AnnotationUtils.findAnnotation(method, JsonFormat.class);
				if( null != annotation ){
					String pattern = annotation.pattern();
					if(StringUtils.hasText( pattern )){
						format = pattern;
					}
				}
				val = DateFormatUtils.format((Date) val ,format);
			}
			return val;
		} catch (Exception e) {
			logger.error("错误信息：",e);
		}
		return null;
	}


	//导出
	public static void exportExcel(HttpServletResponse response, List<List<Object>> dataList, String fileName) {
		Map<String, List<List<Object>>> map = new HashMap<String, List<List<Object>>>();
		map.put(fileName, dataList);
		try {
			exportExcel(response, map, fileName);
		} catch (IOException e) {
			logger.error("错误信息：",e);
		}
	}

	public static void exportExcel(HttpServletResponse response, Map<String, List<List<Object>>> map, String fileName) throws IOException {
		fileName = new String(fileName.getBytes("GBK"), "ISO-8859-1");
		response.setContentType("application/msexcel;charset=GBK");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + "." + "xls");
		OutputStream out = response.getOutputStream();
		BufferedOutputStream b = new BufferedOutputStream(out);
		org.apache.poi.ss.usermodel.Workbook workBook = null;
		workBook = new HSSFWorkbook();
		if (null != map) {
			Set<String> keys = map.keySet();
			int rowSize = 0;// 行
			int cellSize = 0;// 列
			try {
				for (String key : keys) {
					org.apache.poi.ss.usermodel.Sheet sheet = workBook.createSheet(key);
					List<List<Object>> dataList = map.get(key);
					rowSize = dataList.size();
					if (rowSize > 0)
						cellSize = dataList.get(0).size();

					for (int i = 0; i < rowSize; i++) {
						Row row = sheet.createRow(i);
						for (int j = 0; j < cellSize; j++) {
							Object str = dataList.get(i).get(j);
							String s = null;
							try {
								if (str != null) {
									if (str instanceof Integer) {
										row.createCell(j).setCellValue(Integer.parseInt(str.toString()));
										continue;
									} else if (str instanceof Double) {
										row.createCell(j).setCellValue(Double.parseDouble(str.toString()));
										continue;
									} else if (str instanceof BigDecimal) {
										BigDecimal _bd = new BigDecimal(str.toString());
										_bd.setScale(2, BigDecimal.ROUND_HALF_UP);
										row.createCell(j).setCellValue(_bd.doubleValue());
										continue;
									} else if(str instanceof Date) {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										row.createCell(j).setCellValue(sdf.format(str));
										continue;
									}
									s = (String) str;
								} else {
									s = "";
								}
							} catch (Exception e) {
								s = String.valueOf(str);
							}
							row.createCell(j).setCellValue(s);
						}
					}
				}
				workBook.write(b);
				b.flush();
			} catch (Exception e) {
				logger.error("错误信息：",e);
			} finally {
				b.close();
				workBook.close();
			}
		}
	}

	public static List<List<Object>> readExcelPOI(HttpServletRequest request) throws IOException {
		return readExcelPOI(request, 1);
	}

	public static List<List<Object>> readExcelPOI(HttpServletRequest request, int ignoreRows) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, List<MultipartFile>>  map = multipartRequest.getMultiFileMap();
		Set<String> keys = map.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String filedName = it.next();
			List<MultipartFile> list = (List<MultipartFile>) map.get(filedName);
			if(list.size() == 0){
				return new ArrayList<>();
			}
			//只单个文件读取
			MultipartFile file = list.get(0);
			String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
			List<List<Object>> result =  readExcelPOI(fileType,file.getBytes(),ignoreRows);
			return result;
		}
		return new ArrayList<>();
	}

	private static List<List<Object>> readExcelPOI(String fileType,byte[] body,int ignoreRows) throws IOException {
		List<List<Object>> readExcelList = new ArrayList<List<Object>>();
		if (fileType != null) {
			if (fileType.equalsIgnoreCase(".xls")) {
				readExcelList = importExcelXLS(body, ignoreRows);
			} else if (fileType.equalsIgnoreCase(".xlsx")) {
				readExcelList = importExcelXLSX(body, ignoreRows);
			}
		}
		return readExcelList;
	}

	// POI excel导入(xls)
	@SuppressWarnings("deprecation")
	private static List<List<Object>> importExcelXLS(byte[] body, int ignoreRows) throws IOException {
		List<List<Object>> result = new ArrayList<List<Object>>();
		int rowSize = 0;
		InputStream infile = new ByteArrayInputStream(body);
		BufferedInputStream in = new BufferedInputStream(infile);
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFCell cell = null;
		try{
			for (int sheetIndex = 0; sheetIndex < 1; sheetIndex++) {
				// 取得工作簿
				HSSFSheet st = wb.getSheetAt(sheetIndex);
				// 标题行不取
				// 列长按照标题行的最后一行来获取
				HSSFRow lastHeaderRow = null;
				if(ignoreRows==0){
					lastHeaderRow = st.getRow(0);
				}else{
					lastHeaderRow = st.getRow(ignoreRows - 1);
				}
				if (lastHeaderRow == null)
					continue;
				int headerColumnLen = lastHeaderRow.getLastCellNum();
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null) {
						continue;
					}
					int tempRowSize = headerColumnLen + 1;
					if (tempRowSize > rowSize) {
						rowSize = tempRowSize;
					}
					List<Object> values = new ArrayList<Object>();
					boolean hasValue = false;
					for (int columnIndex = 0; columnIndex < headerColumnLen; columnIndex++) {
						Object value = null;
						// 获取单元格
						cell = row.getCell(columnIndex);
						if (cell != null) {
							// cell.setEncoding(HSSFCell.ENCODING_UTF_16);
							switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_STRING:
								value = cell.getStringCellValue();
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:
								if (HSSFDateUtil.isCellDateFormatted(cell)) {
									short format = cell.getCellStyle().getDataFormat();
									SimpleDateFormat sdf = null;
									if(format == 14 || format == 31 || format == 57 || format == 58){
										sdf = new SimpleDateFormat("yyyy-MM-dd");
									}else if(format==188||("dd/mm/yyyy").equals(cell.getCellStyle().getDataFormatString())){
										sdf = new SimpleDateFormat("dd/MM/yyyy");
									}else if (format == 20 || format == 32) {
										sdf = new SimpleDateFormat("HH:mm");
									}
									double data = cell.getNumericCellValue();
									Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(data);
									value = sdf.format(date);
								} else {
									NumberFormat nf = NumberFormat.getInstance();
									nf.setGroupingUsed(false);
									double d = Double.parseDouble(nf.format(cell.getNumericCellValue()));
									if (d % 1.0 != 0) {
										nf.setMinimumFractionDigits(8);
									}
									value = nf.format(cell.getNumericCellValue());
								}
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								// 导入时如果为公式生成的数据则无值
								if (cell.getCachedFormulaResultType() == 0) {
									value = cell.getNumericCellValue();
								} else {
									value = cell.getRichStringCellValue();
									if(value!=null){
										value = value.toString();
									}
								}
								break;
							case HSSFCell.CELL_TYPE_BLANK:
								break;
							case HSSFCell.CELL_TYPE_ERROR:
								value = "";
								break;
							case HSSFCell.CELL_TYPE_BOOLEAN:
								value = (cell.getBooleanCellValue() == true ? "Y" : "N");
								break;
							default:
								value = null;
							}
						}
						if (columnIndex == 0 && value == null) {
							break;
						}
						values.add(value);
						hasValue = true;
					}
					if (hasValue) {
						result.add(values);
					}
				}
			}
		}finally{
			in.close();
			wb.close();
		}
		return result;
	}




	// POI excel导入(xlsx)
	@SuppressWarnings({ "resource", "deprecation" })
	private static List<List<Object>> importExcelXLSX(byte[] body, int ignoreRows) throws IOException {
		List<List<Object>> result = new ArrayList<List<Object>>();
		int rowSize = 0;
		InputStream is = new ByteArrayInputStream(body);
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFCell cell = null;
		for (int sheetIndex = 0; sheetIndex < 1; sheetIndex++) {
			XSSFSheet st = (XSSFSheet) wb.getSheetAt(sheetIndex);
			XSSFRow lastHeaderRow = null;
			if(ignoreRows==0){
				lastHeaderRow = st.getRow(0);
			}else{
				lastHeaderRow = st.getRow(ignoreRows - 1);
			}
			if (lastHeaderRow == null)
				continue;
			int headerColumnLen = lastHeaderRow.getLastCellNum();
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {

				XSSFRow row = st.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				int tempRowSize = headerColumnLen + 1;
				if (tempRowSize > rowSize) {
					rowSize = tempRowSize;
				}
				List<Object> values = new ArrayList<Object>();
				boolean hasValue = false;
				for (int columnIndex = 0; columnIndex < headerColumnLen; columnIndex++) {
					Object value = null;
					cell = row.getCell(columnIndex);
					if (cell != null) {
						// 注意：一定要设成这个，否则可能会出现乱码
						// cell.setEncoding(HSSFCell.ENCODING_UTF_16);
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								Date date = cell.getDateCellValue();
								value = date;
							} else {
								// String cellStr =
								// String.valueOf(cell.getNumericCellValue());
								value = cell.getRawValue();
							}
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							// 导入时如果为公式生成的数据则无值
							if (cell.getCachedFormulaResultType() == 0) {//禅道1433 导入excel小数点问题修复
								BigDecimal b = new BigDecimal(cell.getNumericCellValue());
								value = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
							} else {
								value = cell.getRichStringCellValue();
								if(value!=null){
									value = value.toString();
								}
							}
							break;
						case HSSFCell.CELL_TYPE_BLANK:
							break;
						case HSSFCell.CELL_TYPE_ERROR:
							value = "";
							break;
						case HSSFCell.CELL_TYPE_BOOLEAN:
							value = (cell.getBooleanCellValue() == true ? "Y" : "N");
							break;
						default:
							value = null;
						}
					}
					if (columnIndex == 0 && value == null) {
						break;
					}
					values.add(value);
					hasValue = true;
				}
				if (hasValue) {
					result.add(values);
				}
			}
		}
		is.close();

		return result;
	}

	/**
	 * 报表导出
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	public static byte[] exportReportExcel(ExcelExportSheet sheet) throws Exception {
		return exportReportExcel(ExcelExport.instances( sheet.getName() ).add(  sheet ) );
	}

	/**
	 * 报表导出文件
	 * @param excel
	 * @return
	 * @throws Exception
	 */
	public static byte[] exportReportExcel( ExcelExport excel) throws Exception {
		org.apache.poi.ss.usermodel.Workbook workBook = new HSSFWorkbook();
		for (ExcelExportSheet sheet:excel.getSheets()) {
			createSheet( workBook,sheet  );
		}
		ByteArrayOutputStream  os = new ByteArrayOutputStream();
		byte[]  byteData = null;
		try {
			workBook.write(os);
			byteData = os.toByteArray();
			os.close();
		} finally {
			if( null != os ){
				try {
					os.close();
				} catch (IOException ignored) { }
			}
			try {
				workBook.close();
			} catch (IOException ignored) { }
			return byteData;
		}
	}
	// 1566 s
	//导出
	public static byte[] exportExcel(List<List<Object>> dataList, String fileName) {
		Map<String, List<List<Object>>> map = new HashMap<>();
		map.put(fileName, dataList);
		try {
			return exportExcel(map);
		} catch (IOException e) {
			logger.error("错误信息：",e);
		}
		return new byte[]{};
	}

	public static byte[] exportExcel(Map<String, List<List<Object>>> map) throws IOException {
		ByteArrayOutputStream  os = new ByteArrayOutputStream();
		XSSFWorkbook workBook = new XSSFWorkbook();
		if (null != map) {
			Set<String> keys = map.keySet();
			int rowSize = 0;// 行
			int cellSize = 0;// 列
			try {
				for (String key : keys) {
					org.apache.poi.ss.usermodel.Sheet sheet = workBook.createSheet(key);
					List<List<Object>> dataList = map.get(key);
					rowSize = dataList.size();
					if (rowSize > 0)
						cellSize = dataList.get(0).size();

					for (int i = 0; i < rowSize; i++) {
						Row row = sheet.createRow(i);
						for (int j = 0; j < cellSize; j++) {
							Object str = dataList.get(i).get(j);
							String s = null;
							try {
								if (str != null) {
									if (str instanceof Integer) {
										row.createCell(j).setCellValue(Integer.parseInt(str.toString()));
										continue;
									} else if (str instanceof Double) {
										row.createCell(j).setCellValue(Double.parseDouble(str.toString()));
										continue;
									} else if (str instanceof BigDecimal) {
										BigDecimal _bd = new BigDecimal(str.toString());
										_bd.setScale(2, BigDecimal.ROUND_HALF_UP);
										row.createCell(j).setCellValue(_bd.doubleValue());
										continue;
									} else if(str instanceof Date) {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										row.createCell(j).setCellValue(sdf.format(str));
										continue;
									}
									s = (String) str;
								} else {
									s = "";
								}
							} catch (Exception e) {
								s = String.valueOf(str);
							}
							row.createCell(j).setCellValue(s);
						}
					}
				}
				workBook.write(os);
				return os.toByteArray();
			} catch (Exception e) {
				logger.error("错误信息：",e);
			} finally {
				os.close();
				os.flush();
				workBook.close();
			}
		}
		return new byte[]{};
	}

	public static JSONObject readExcelPOIMulte(HttpServletRequest request, int ignoreRows) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, List<MultipartFile>>  map = multipartRequest.getMultiFileMap();
		Set<String> keys = map.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String filedName = it.next();
			List<MultipartFile> list = (List<MultipartFile>) map.get(filedName);
			if(list.size() == 0){
				return new JSONObject();
			}
			//只单个文件读取
			MultipartFile file = list.get(0);
			String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
			JSONObject result =  readExcelPOIMulte(fileType,file.getBytes(),ignoreRows);
			return result;
		}
		return new JSONObject();
	}

	private static JSONObject readExcelPOIMulte(String fileType, byte[] body, int ignoreRows) throws IOException {
		List<List<Object>> readExcelList = new ArrayList<List<Object>>();
		JSONObject returnJson = new JSONObject();
		if (fileType != null) {
			if (fileType.equalsIgnoreCase(".xls")) {
				returnJson = importExcelXLSMulte(body, ignoreRows);
			} else if (fileType.equalsIgnoreCase(".xlsx")) {
				returnJson = importExcelXLSXMulte(body, ignoreRows);
			}
		}
		return returnJson;
	}

	private static JSONObject importExcelXLSMulte(byte[] body, int ignoreRows) throws IOException {
		JSONObject returnJson = new JSONObject();
		int rowSize = 0;
		InputStream infile = new ByteArrayInputStream(body);
		BufferedInputStream in = new BufferedInputStream(infile);
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFCell cell = null;
		try{
			for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
				// 取得工作簿
				HSSFSheet st = wb.getSheetAt(sheetIndex);
				// 标题行不取
				// 列长按照标题行的最后一行来获取
				HSSFRow lastHeaderRow = null;
				if(ignoreRows==0){
					lastHeaderRow = st.getRow(0);
				}else{
					lastHeaderRow = st.getRow(ignoreRows - 1);
				}
				if (lastHeaderRow == null){continue;}

				String sheetName = st.getSheetName();
				List<List<Object>> result = new ArrayList<List<Object>>();
				int headerColumnLen = lastHeaderRow.getLastCellNum();
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null) {
						continue;
					}
					int tempRowSize = headerColumnLen + 1;
					if (tempRowSize > rowSize) {
						rowSize = tempRowSize;
					}
					List<Object> values = new ArrayList<Object>();
					boolean hasValue = false;
					for (int columnIndex = 0; columnIndex < headerColumnLen; columnIndex++) {
						Object value = null;
						// 获取单元格
						cell = row.getCell(columnIndex);
						if (cell != null) {
							// cell.setEncoding(HSSFCell.ENCODING_UTF_16);
							switch (cell.getCellType()) {
								case HSSFCell.CELL_TYPE_STRING:
									value = cell.getStringCellValue();
									break;
								case HSSFCell.CELL_TYPE_NUMERIC:
									if (HSSFDateUtil.isCellDateFormatted(cell)) {
										short format = cell.getCellStyle().getDataFormat();
										SimpleDateFormat sdf = null;
										if(format == 14 || format == 31 || format == 57 || format == 58){
											sdf = new SimpleDateFormat("yyyy-MM-dd");
										}else if(format==188||("dd/mm/yyyy").equals(cell.getCellStyle().getDataFormatString())){
											sdf = new SimpleDateFormat("dd/MM/yyyy");
										}else if (format == 20 || format == 32) {
											sdf = new SimpleDateFormat("HH:mm");
										}
										double data = cell.getNumericCellValue();
										Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(data);
										value = sdf.format(date);
									} else {
										NumberFormat nf = NumberFormat.getInstance();
										nf.setGroupingUsed(false);
										double d = Double.parseDouble(nf.format(cell.getNumericCellValue()));
										if (d % 1.0 != 0) {
											nf.setMinimumFractionDigits(8);
										}
										value = nf.format(cell.getNumericCellValue());
									}
									break;
								case HSSFCell.CELL_TYPE_FORMULA:
									// 导入时如果为公式生成的数据则无值
									if (cell.getCachedFormulaResultType() == 0) {
										value = cell.getNumericCellValue();
									} else {
										value = cell.getRichStringCellValue();
										if(value!=null){
											value = value.toString();
										}
									}
									break;
								case HSSFCell.CELL_TYPE_BLANK:
									break;
								case HSSFCell.CELL_TYPE_ERROR:
									value = "";
									break;
								case HSSFCell.CELL_TYPE_BOOLEAN:
									value = (cell.getBooleanCellValue() == true ? "Y" : "N");
									break;
								default:
									value = null;
							}
						}
						if (columnIndex == 0 && value == null) {
							break;
						}
						values.add(value);
						hasValue = true;
					}
					if (hasValue) {
						result.add(values);
					}
				}
				returnJson.put(sheetName,result);
			}
		}finally{
			in.close();
			wb.close();
		}
		return returnJson;
	}
	private static JSONObject importExcelXLSXMulte(byte[] body, int ignoreRows) throws IOException {
		JSONObject returnJson = new JSONObject();
		int rowSize = 0;
		InputStream is = new ByteArrayInputStream(body);
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFCell cell = null;
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			XSSFSheet st = (XSSFSheet) wb.getSheetAt(sheetIndex);
			XSSFRow lastHeaderRow = null;
			if(ignoreRows==0){
				lastHeaderRow = st.getRow(0);
			}else{
				lastHeaderRow = st.getRow(ignoreRows - 1);
			}
			if (lastHeaderRow == null){continue;}

			String sheetName = st.getSheetName();
			List<List<Object>> result = new ArrayList<List<Object>>();
			int headerColumnLen = lastHeaderRow.getLastCellNum();
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {

				XSSFRow row = st.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				int tempRowSize = headerColumnLen + 1;
				if (tempRowSize > rowSize) {
					rowSize = tempRowSize;
				}
				List<Object> values = new ArrayList<Object>();
				boolean hasValue = false;
				for (int columnIndex = 0; columnIndex < headerColumnLen; columnIndex++) {
					Object value = null;
					cell = row.getCell(columnIndex);
					if (cell != null) {
						// 注意：一定要设成这个，否则可能会出现乱码
						// cell.setEncoding(HSSFCell.ENCODING_UTF_16);
						switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_STRING:
								value = cell.getStringCellValue();
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:
								if (HSSFDateUtil.isCellDateFormatted(cell)) {
									Date date = cell.getDateCellValue();
									value = date;
								} else {
									// String cellStr =
									// String.valueOf(cell.getNumericCellValue());
									value = cell.getRawValue();
								}
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								// 导入时如果为公式生成的数据则无值
								if (cell.getCachedFormulaResultType() == 0) {//禅道1433 导入excel小数点问题修复
									BigDecimal b = new BigDecimal(cell.getNumericCellValue());
									value = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
								} else {
									value = cell.getRichStringCellValue();
									if(value!=null){
										value = value.toString();
									}
								}
								break;
							case HSSFCell.CELL_TYPE_BLANK:
								break;
							case HSSFCell.CELL_TYPE_ERROR:
								value = "";
								break;
							case HSSFCell.CELL_TYPE_BOOLEAN:
								value = (cell.getBooleanCellValue() == true ? "Y" : "N");
								break;
							default:
								value = null;
						}
					}
					if (columnIndex == 0 && value == null) {
						break;
					}
					values.add(value);
					hasValue = true;
				}
				if (hasValue) {
					result.add(values);
				}
			}
			returnJson.put(sheetName,result);
		}
		is.close();
		return returnJson;
	}



}
