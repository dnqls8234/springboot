package com.homepage.demo.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.homepage.demo.dao.PurchaseDao;
import com.homepage.demo.service.AdminAccountsMngService;
import com.homepage.demo.service.UserLogService;
import com.homepage.demo.service.MainService;
import com.homepage.demo.service.PurchaseService;

@Controller
@RequestMapping("purchase")
public class PurchaseController extends SessionController{
	
	@Autowired
	PurchaseService purchaseServcie;
	
	@Autowired
	AdminAccountsMngService adminAccountsMngService;
	
	@Autowired
	UserLogService userLogService;
	
	@Autowired
	PurchaseDao purchaseDao;
	
	@RequestMapping
	public String index(Model model,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String search_field,
			@RequestParam(required = false) String search_text,
			@RequestParam(required = false) String search_if,
			@RequestParam(required = false) String search_if_text,
			@RequestParam(required = false) String select_date,
			@RequestParam(required = false) Integer page_no,
			@RequestParam(required = false) Integer page_row
			) {
		
		List<Map<String, Object>> list = null; 
		
		
		if(page_row == null) page_row = 30;
		if(page_no == null) page_no = 1; 
		
		model.addAttribute("pageCnt", (page_no-1)*page_row);
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		if(select_date != null) {
			if(select_date.equals("All")) 
				select_date = null;
		}
		
		params.put("search",search);
		params.put("select_date", select_date);
		params.put("search_field", search_field);
		params.put("search_text" , search_text);
		params.put("search_if", search_if);
		params.put("search_if_text" , search_if_text);
		params.put("start"		 , (page_no-1)*page_row);
		params.put("page_row"	 , page_row);
		System.out.println(params);
		
		list = purchaseDao.getPurchases(params);
		
		Integer listCnt = purchaseDao.searchCnt(params);
		
		page_no = (page_no-1)/10;
		System.out.println(list);
		List<Integer> pageList = new ArrayList<Integer>();
		for(int i=0 ; i<=9;i++) {
			pageList.add((page_no*10)+1+i);
		}
		
		model.addAttribute("pageList", pageList);
		model.addAttribute("list", list); 
		model.addAttribute("listCnt", listCnt); 
		
		
		return "main/buyhis";
	}
	
	@PostMapping("downloadExcel")
	@ResponseBody
	public String downloadExcel(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String search_field,
			@RequestParam(required = false) String search_text,
			@RequestParam(required = false) String search_if,
			@RequestParam(required = false) String search_if_text,
			@RequestParam(required = false) String select_date,
			@RequestParam(required = false) Integer page_no,
			@RequestParam(required = false) Integer page_row,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {
		
		if(page_row == null) page_row = 30;
		if(page_no == null) page_no = 1; 
		
		Map<String,Object> params = new HashMap<String, Object>(); 
		String fileName = "구매내역.xlsx";
		
		if(select_date != null) {
			if(select_date.equals("All")) 
				select_date = null;
		}
		
		params.put("search",search);
		params.put("select_date", select_date);
		params.put("search_field", search_field);
		params.put("search_text" , search_text);
		params.put("search_if", search_if);
		params.put("search_if_text" , search_if_text);
		params.put("start"		 , (page_no-1)*page_row);
		params.put("page_row"	 , page_row);
		
		purchaseServcie.download_excel(params, fileName, request, response);
		return "";
	}
	
	@RequestMapping("compExcelUpload")
	@ResponseBody
	public String excelUpload(MultipartHttpServletRequest req){
		
		System.out.println("구매upload확인용");
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		//엑셀 파일이 xls일때와 xlsx일때 서비스 라우팅
		String excelType = req.getParameter("excelType");
		if(excelType.equals("xlsx")){
			list = purchaseServcie.xlsxExcelReader(req);
		}else if(excelType.equals("xls")){
			list = purchaseServcie.xlsExcelReader(req);
		}
		String check = "성공";
		if(list == null ) {
			check = "실패";
		}
		
		return check;
	}
}
