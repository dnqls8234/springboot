package com.homepage.demo.Controller;

import java.io.File;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.homepage.demo.dao.AdminDao;
import com.homepage.demo.dao.MainDao;
import com.homepage.demo.service.AdminAccountsMngService;
import com.homepage.demo.service.UserLogService;
import com.homepage.demo.service.MainService;

@Controller
@RequestMapping("member")
public class MainController extends SessionController{
	
	@Autowired
	MainService mainService;
	
	@Autowired
	AdminAccountsMngService adminAccountsMngService;
	
	@Autowired
	UserLogService userLogService;
	
	@Autowired
	MainDao mainDao;
	
	@Autowired
	AdminDao adminDao;
	
	@RequestMapping
	public String index(Model model,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String select_date,
			@RequestParam(required = false) String search_field,
			@RequestParam(required = false) String search_text,
			@RequestParam(required = false) String search_grade,
			@RequestParam(required = false) String search_date,
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
		params.put("search_grade", search_grade);
		params.put("search_date" , search_date);
		params.put("start"		 , (page_no-1)*page_row);
		params.put("page_row"	 , page_row);
		System.out.println(params);
			
		list = mainDao.search(params);
		
		Integer listCnt = mainDao.searchCnt(params);
		
		page_no = (page_no-1)/5;
		
		List<Integer> pageList = new ArrayList<Integer>();
		for(int i=0 ; i<=4;i++) {
			pageList.add((page_no*5)+1+i);
		}
		
		model.addAttribute("pageList", pageList);
		model.addAttribute("list", list); 
		model.addAttribute("listCnt", listCnt);
		
		
		return "main/member";
	}
	
	@PostMapping("downloadExcel")
	@ResponseBody
	public String downloadExcel(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String select_date,
			@RequestParam(required = false) String search_field,
			@RequestParam(required = false) String search_text,
			@RequestParam(required = false) String search_grade,
			@RequestParam(required = false) String search_date,
			@RequestParam(required = false) Integer page_no,
			@RequestParam(required = false) Integer page_row,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {
		
		if(page_row == null) page_row = 30;
		if(page_no == null) page_no = 1; 
		
		Map<String,Object> params = new HashMap<String, Object>(); 
		String fileName = "가입승인회원.xlsx";
		
		if(select_date != null) {
			if(select_date.equals("All")) 
				select_date = null;
		}
		
		params.put("search",search);
		params.put("select_date", select_date);
		params.put("search_field",search_field);
		params.put("search_text",search_text);
		params.put("search_grade",search_grade);
		params.put("search_date",search_date);
		params.put("start"		 , (page_no-1)*page_row);
		params.put("page_row"	 , page_row);
		
		mainService.download_excel(params, fileName, request, response);
		return "";
	}
	
	@RequestMapping("compExcelUpload")
	@ResponseBody
	public String excelUpload(MultipartHttpServletRequest req){
		
		System.out.println("upload확인용");
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		//엑셀 파일이 xls일때와 xlsx일때 서비스 라우팅
		String excelType = req.getParameter("excelType");
		if(excelType.equals("xlsx")){
			list = mainService.xlsxExcelReader(req);
		}else if(excelType.equals("xls")){
			list = mainService.xlsExcelReader(req);
		}
		String check = "성공";
		if(list == null ) {
			check = "실패";
		}
		
		return check;
	}

	@RequestMapping("securityPhone")
	@ResponseBody
	public String securityPhone(Integer id, String passwd, String user_id, Integer user_no) throws Exception {
		String result = "";
		System.out.println(passwd);
		
		Map<String, Object> params = adminDao.findById(ID);
		
		if(passwd.equals(params.get("user_pw"))) {
			
			result = mainDao.findByid(id);
			
			userLogService.userLogInsert(user_no); 
			
		} else {
			
			throw new Exception(); 
			
		}
		
		
		
		
		return result;
	}
	
}
