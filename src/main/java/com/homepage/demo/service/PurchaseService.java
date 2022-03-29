package com.homepage.demo.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.homepage.demo.component.CommonUtils;
import com.homepage.demo.component.ExcelUtils;
import com.homepage.demo.dao.PurchaseDao;

@Service
public class PurchaseService {

	@Autowired
	PurchaseDao pruchaseDao;
	
	public void download_excel(
			Map<String, Object> params,
			String fileName,
			HttpServletRequest request,
			HttpServletResponse response
		) throws Exception {
		List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> sampleMap = new HashMap<String, Object>();
		sampleMap.put("base","ex) 서울");		
		sampleMap.put("base_sub","ex) 서울");		
		sampleMap.put("agency","ex) 서울");		
		sampleMap.put("store","ex) 서울");		
		sampleMap.put("user_grade","ex)  x등급");		
		sampleMap.put("user_id","ex) test001");		
		sampleMap.put("user_name","ex) 홍길동");		
		sampleMap.put("buy_price","ex) 100000");	
		sampleMap.put("buy_at","ex) 20xx-xx-xx xx:xx");	
		results.add(sampleMap);
				
		results.addAll(pruchaseDao.getPurchases(params));
		
		String[][] headers = new String[][] {
			new String[] {
				"본사정보",
				"부본사",
			    "대리점",
			    "매장",
			    "등급",
			    "아이디",
			    "이름",
			    "구매금액",
			    "구매일"
			}
		};
		
		String[] keys = new String[] {
				"base",
				"base_sub",
				"agency",
				"store",
				"user_grade",
				"user_id",
				"user_name",
				"buy_price",
				"buy_at"
				};
		
		File file = ExcelUtils.exportExcel(results, headers, keys);
        
		if (file == null) {
			throw new Exception("파일을 생성 할 수 없습니다.");
		} else {
	        ExcelUtils.downloadFile(request, response, file, fileName);
		}
	}

	@Transactional(rollbackFor= { Exception.class, RuntimeException.class, Error.class })
	public List<Map<String,Object>> xlsExcelReader(MultipartHttpServletRequest req) {
		// 반환할 객체를 생성
		List<Map<String,Object>> list = new ArrayList<>();

		MultipartFile file = req.getFile("excel");
		HSSFWorkbook workbook = null;

		try {
			// HSSFWorkbook은 엑셀파일 전체 내용을 담고 있는 객체
			workbook = new HSSFWorkbook(file.getInputStream());

			// 탐색에 사용할 Sheet, Row, Cell 객체
			HSSFSheet curSheet;
			HSSFRow curRow;
			HSSFCell curCell;
			Map<String,Object> vo;

			// Sheet 탐색 for문
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				// 현재 sheet 반환
				curSheet = workbook.getSheetAt(sheetIndex);
				// row 탐색 for문
				for (int rowIndex = 0; rowIndex < curSheet.getPhysicalNumberOfRows(); rowIndex++) {
					// row 0은 헤더정보이기 때문에 무시
					if (rowIndex != 0 && rowIndex != 1) {
						curRow = curSheet.getRow(rowIndex);
						vo = new HashMap<String,Object>();
						String value;

						// row의 첫번째 cell값이 비어있지 않는 경우만 cell탐색
						if (curRow.getCell(0) != null) {
							if (!"".equals(curRow.getCell(0).getStringCellValue())) {
								// cell 탐색 for문
								for (int cellIndex = 0; cellIndex < curRow.getPhysicalNumberOfCells(); cellIndex++) {
									curCell = curRow.getCell(cellIndex);

									if (true) {
										value = "";
										// cell 스타일이 다르더라도 String으로 반환 받음
										switch (curCell.getCellType()) {
										case HSSFCell.CELL_TYPE_FORMULA:
											value = curCell.getCellFormula();
											break;
										case HSSFCell.CELL_TYPE_NUMERIC:
											
											if(DateUtil.isCellDateFormatted(curCell)) {

												value = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(curCell.getDateCellValue());

												break;
											}
											
											value = curCell.getNumericCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_STRING:
											value = curCell.getStringCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_BLANK:
											value = curCell.getBooleanCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_ERROR:
											value = curCell.getErrorCellValue() + "";
											break;
										default:
											value = new String();
											break;
										} // end switch

										// 현재 colum index에 따라서 vo입력
										switch (cellIndex) {
										case 0: // 본사
											vo.put("base", value);
											break;
										case 1: // 부본사
											vo.put("base_sub", value);
											break;
										case 2: // 대리점
											vo.put("agency", value);
											break;
										case 3: // 매장
											vo.put("store", value);
											break;
										case 4: // 등급
											vo.put("user_grade", value);
											break;
										case 5: // 아이디
											vo.put("user_id", value);
											break;
										case 6: // 이름
											vo.put("user_name", value);
											break;
										case 7: // 구매금액
											vo.put("buy_price", value);
											break;
										case 8: // 구매일
											if(value != null && !value.equals("") && !value.equals("false")) {
												vo.put("buy_at", CommonUtils.stringToDate(value.replaceAll(",", "-"), "yyyy-MM-dd HH:mm"));
												break;
											} else {
												break;
											}
										default:
											break;
										}
									} // end if
								} // end for
								// cell 탐색 이후 vo 추가
								list.add(vo);
							} // end
						} // end if
					}

				}
			}
		} catch (Exception  e) {
			e.printStackTrace();
			return null;
		}

		// 디비에 insert
		pruchaseDao.insertExcelTest(list);
		return list;
	}

	@Transactional(rollbackFor= { Exception.class, RuntimeException.class, Error.class })
	public List<Map<String,Object>> xlsxExcelReader(MultipartHttpServletRequest req) {
		// 반환할 객체를 생성
		List<Map<String,Object>> list = new ArrayList<>();

		MultipartFile file = req.getFile("excel");
		XSSFWorkbook workbook = null;

		try {
			// HSSFWorkbook은 엑셀파일 전체 내용을 담고 있는 객체
			workbook = new XSSFWorkbook(file.getInputStream());

			// 탐색에 사용할 Sheet, Row, Cell 객체
			XSSFSheet curSheet;
			XSSFRow curRow;
			XSSFCell curCell;
			Map<String,Object> vo;

			// Sheet 탐색 for문
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				// 현재 sheet 반환
				curSheet = workbook.getSheetAt(sheetIndex);
				// row 탐색 for문
				for (int rowIndex = 0; rowIndex < curSheet.getPhysicalNumberOfRows(); rowIndex++) {
					// row 0은 헤더정보이기 때문에 무시
					if (rowIndex != 0 && rowIndex != 1) {
						curRow = curSheet.getRow(rowIndex);
						vo = new HashMap<String,Object>();
						String value;

						// row의 첫번째 cell값이 비어있지 않는 경우만 cell탐색
						if (curRow.getCell(0) != null) {
							if ( true ) {
								// cell 탐색 for문

								for (int cellIndex = 0; cellIndex < curSheet.getRow(0).getPhysicalNumberOfCells(); cellIndex++) {
									curCell = curRow.getCell(cellIndex);
									if (true) {
										value = "";
										
										// cell 스타일이 다르더라도 String으로 반환 받음
										if(curCell == null) {
												continue;
										}

										switch (curCell.getCellType()) {
										case HSSFCell.CELL_TYPE_FORMULA:
											value = curCell.getCellFormula();
											break;
										case HSSFCell.CELL_TYPE_NUMERIC:
											
											if(DateUtil.isCellDateFormatted(curCell)) {
												
												value = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(curCell.getDateCellValue());

												break;
											}

											value = curCell.getNumericCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_STRING:
											value = curCell.getStringCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_BLANK:
											value = curCell.getBooleanCellValue() + "";
											break;
										case HSSFCell.CELL_TYPE_ERROR:
											value = curCell.getErrorCellValue() + "";
											break;
										default:
											value = new String();
											break;
										} // end switch

										// 현재 colum index에 따라서 vo입력
										switch (cellIndex) {
										case 0: // 본사
											vo.put("base", value);
											break;
										case 1: // 부본사
											vo.put("base_sub", value);
											break;
										case 2: // 대리점
											vo.put("agency", value);
											break;
										case 3: // 매장
											vo.put("store", value);
											break;
										case 4: // 등급
											vo.put("user_grade", value);
											break;
										case 5: // 아이디
											vo.put("user_id", value);
											break;
										case 6: // 이름
											vo.put("user_name", value);
											break;
										case 7: // 구매금액
											vo.put("buy_price", value);
											break;
										case 8: // 구매일
											if(value != null && !value.equals("") && !value.equals("false")) {
												vo.put("buy_at", CommonUtils.stringToDate(value.replaceAll(",", "-"), "yyyy-MM-dd HH:mm"));
												break;
											} else {
												break;
											}
										default:
											break;
										}
									} // end if
								} // end for
								// cell 탐색 이후 vo 추가
								list.add(vo);
							} // end
						} // end if
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		// 디비에 insert
		pruchaseDao.insertExcelTest(list);
		return list;
	}

	
}
