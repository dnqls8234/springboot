package com.homepage.demo.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelUtils {
	public static List<Map<String,String>> excelToList(MultipartFile multiFile,Integer targetSheet, Integer startRow, final String[] indexOfKeys) throws IOException, InvalidFormatException{
		return excelToList(multiFile.getOriginalFilename(),multiFile.getInputStream(), targetSheet, startRow, indexOfKeys);
	}

	public static List<Map<String,String>> excelToList(String filePath,Integer targetSheet, Integer startRow, final String[] indexOfKeys) throws IOException, InvalidFormatException{
		File fd = new File(filePath);
		return excelToList(fd.getName(), new FileInputStream(fd), targetSheet, startRow, indexOfKeys);
	}
	
	public static List<Map<String,String>> excelToList(String filePath,Integer targetSheet, Integer startRow) throws IOException, InvalidFormatException{
		File fd = new File(filePath);
		FileInputStream is = new FileInputStream(fd);
		Workbook workbook = WorkbookFactory.create(is);
		/*
		 * if(fileName.toUpperCase().lastIndexOf(".XLSX") > 0) { workbook = new
		 * XSSFWorkbook(); }else if(fileName.toUpperCase().lastIndexOf(".XLS") > 0) {
		 * workbook = new HSSFWorkbook(is); }
		 */
		Sheet sheet = workbook.getSheetAt(targetSheet);
		int cells;
		Cell cell;
		Row row;
		String value;
		
		row = sheet.getRow(0);
		cells = row.getPhysicalNumberOfCells();
		
		String[] indexOfKeys = new String[cells];
		
		for(int j = 0; j <cells;j++) {
			cell = row.getCell(j);
			switch (cell.getCellType()) {

				case Cell.CELL_TYPE_FORMULA:
					cell.setCellType(Cell.CELL_TYPE_STRING);
					value = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_NUMERIC:
					cell.setCellType(Cell.CELL_TYPE_STRING);
					value = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_STRING:
					value = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_BLANK:
					value = cell.getRichStringCellValue().getString();
					break;
				case Cell.CELL_TYPE_ERROR:
					value = cell.getErrorCellValue()+"";
					break;
				default:
					value = cell.getRichStringCellValue().getString();
					break;
			}
			
			indexOfKeys[j] = value;
		}
		
		is.close();
		
		return excelToList(fd.getName(), new FileInputStream(fd), targetSheet, startRow, indexOfKeys);
	}
	
	public static List<Map<String,String>> excelToList(String fileName,InputStream is,Integer targetSheet, Integer startRow, final String[] indexOfKeys) throws IOException, InvalidFormatException{
		List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
		Map<String,String> temp;
		Workbook workbook = WorkbookFactory.create(is);
		/*
		 * if(fileName.toUpperCase().lastIndexOf(".XLSX") > 0) { workbook = new
		 * XSSFWorkbook(); }else if(fileName.toUpperCase().lastIndexOf(".XLS") > 0) {
		 * workbook = new HSSFWorkbook(is); }
		 */
		Sheet sheet = workbook.getSheetAt(targetSheet);
		int rows = sheet.getPhysicalNumberOfRows();
		int cells;
		Cell cell;
		Row row;
		String value;
		for(int i = startRow;i<rows;i++){
			row = sheet.getRow(i);
			temp = new HashMap<String,String>();
			if(indexOfKeys == null) {
				cells = row.getPhysicalNumberOfCells();
				for(int j = 0; j <cells;j++) {
					cell = row.getCell(j);
					switch (cell.getCellType()) {
						case Cell.CELL_TYPE_FORMULA:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BLANK:
							value = cell.getRichStringCellValue().getString();
							break;
						case Cell.CELL_TYPE_ERROR:
							value = cell.getErrorCellValue()+"";
							break;
						default:
							value = cell.getRichStringCellValue().getString();
							break;
					}
					temp.put("COL_"+j, value);
				}
			}else {
				for(int j = 0; j <indexOfKeys.length;j++) {
					cell = row.getCell(j);
					switch (cell.getCellType()) {
						case Cell.CELL_TYPE_FORMULA:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BLANK:
							value = cell.getRichStringCellValue().getString();
							break;
						case Cell.CELL_TYPE_ERROR:
							value = cell.getErrorCellValue()+"";
							break;
						default:
							value = cell.getRichStringCellValue().getString();
							break;
					}
					temp.put(indexOfKeys[j], value);

				}
			}
			ret.add(temp);
		}
		
		return ret;
	}


	public static File exportExcel(List exportLists, String headers[][] , String keys[]) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		Row row;
		Cell cell;
		int index=0;
		int CellIndex=0;

		if(headers!=null) {
			for(String[] headerRows:headers) {
				row = sheet.createRow(index);
				CellIndex=0;
				for(String headerCell:headerRows) {
					cell = row.createCell(CellIndex);
					cell.setCellValue( headerCell);
					CellIndex++;
				}
				index++;
			}
		}



		if(exportLists!=null && exportLists.size()>0) {
			for(Object obj : exportLists) {
				row = sheet.createRow(index);
				if(obj instanceof Map) {
					if(keys!=null) {
						CellIndex=0;
						for(String key:keys) {
							cell = row.createCell(CellIndex);
							cell.setCellValue( ((Map) obj).get(key) ==null?"":String.valueOf(((Map) obj).get(key)));
							CellIndex++;
						}
					}
				}else {
					Method method;
					String method_name;
					Object val;
					if(keys!=null) {
						CellIndex=0;
						for(String key:keys) {
							cell = row.createCell(CellIndex);
						    Class<?> clazz = obj.getClass();
						    method_name = "get"+key.substring(0,1).toUpperCase()+key.substring(1);
						    method = clazz.getMethod(method_name);
						    method.setAccessible(true);
						    val = method.invoke(obj, null);
							cell.setCellValue( val ==null?"":String.valueOf(val));
							CellIndex++;
						}
					}
				}
				index++;
			}
		}
		
		for(int i=0;i<headers[0].length;i++) {
			sheet.autoSizeColumn(i);
			sheet.setColumnWidth(i, (sheet.getColumnWidth(i))+(short)1024);
		}
		
		
		File tempFile = new File(CommonUtils.generatedUUID20()+"_temp.xlsx");
		FileOutputStream fo = null;
		try {
			fo= new FileOutputStream(tempFile);
			workbook.write(fo);
		}catch (Exception e) {
			// TODO: handle exception
		}finally {
			if(fo!=null) {
				try {
					fo.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("파일 이름 확인용 : "+tempFile.getAbsolutePath());
		return tempFile;
	}
	
	// 한글 파일명 사용 처리
	public static String getDisposition(String filename, HttpServletRequest request) throws Exception {
		String encodedFilename = null;
			
		String browser = CommonUtils.getBrowser(request);

		if (browser.equals("MSIE")) {
			encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
		} else if (browser.equals("FIREFOX")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("OPERA")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("CHROME")) {
			StringBuffer sb = new StringBuffer();
				
			for (int i = 0; i < filename.length(); i++) {
				char c = filename.charAt(i);
					
				if (c > '~') {
					sb.append(URLEncoder.encode("" + c, "UTF-8"));
				} else {
					sb.append(c);
				}
			}
				
			encodedFilename = sb.toString();
		} else {
			throw new RuntimeException("Not supported browser");
		}
			
		return encodedFilename;
	}
		
	public static void downloadFile(HttpServletRequest request, HttpServletResponse response, File file, String fileName) throws Exception {
//		   response.setContentType("application/download; utf-8");
	       response.setContentLength((int)file.length());        
	       response.setHeader("Content-Disposition", "attachment; filename=\"" + getDisposition(fileName, request) + "\";");
	       response.setHeader("Content-Transfer-Encoding", "binary");
	       
	       OutputStream out = response.getOutputStream();
	       FileInputStream fis = null;
	        
	       try {
	    	   fis = new FileInputStream(file);
	            
	           FileCopyUtils.copy(fis, out);
	       } catch (Exception e) {
	           e.printStackTrace();
	       } finally {
	           if (fis != null) {
	               try {
	                   fis.close();
	               } catch(Exception e) {}
	           }
	            
	           if (file != null) {
	        	   file.delete();
	           }
	       }
	        
	       out.flush();
	}
}
