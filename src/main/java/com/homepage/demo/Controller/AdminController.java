package com.homepage.demo.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.homepage.demo.dao.AdminDao;

@Controller
@RequestMapping("adminManager")
public class AdminController extends SessionController{

	@Autowired
	AdminDao adminDao;
	
	@RequestMapping
	public String index(
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		HttpSession session =  request.getSession();
		
		if(session.getAttribute("user_type").equals(100)) {
			System.out.println(session.getAttribute("user_type")+"@");
			return "main/admin";
		} else {
			System.out.println("넌 관리자가 아니야");
			return "redirect:/member";
		}
	}
	
	@RequestMapping("phoneLog")
	public String phoneLog(Model model,
			@RequestParam(required = false) Integer page_no,
			@RequestParam(required = false) Integer page_row) {
		
		if(USER_TYPE == 100) {
			Map<String, Object> params = new HashMap<String, Object>();

			if(page_row == null) page_row = 30;
			if(page_no == null) page_no = 1; 

			model.addAttribute("pageCnt", (page_no-1)*page_row);

			params.put("start"		 , (page_no-1)*page_row);
			params.put("page_row"	 , page_row);

			System.out.println(params);

			List<Map<String, Object>> list = adminDao.findPhoneLog(params);

			Integer listCnt = adminDao.findPhoneLogCnt(params);

			page_no = (page_no-1)/5;

			List<Integer> pageList = new ArrayList<Integer>();
			for(int i=0 ; i<=4;i++) {
				pageList.add((page_no*5)+1+i);
			}

			model.addAttribute("user_id",USER_ID);
			model.addAttribute("user_type",USER_TYPE);
			model.addAttribute("id",ID);
			model.addAttribute("pageList", pageList);
			model.addAttribute("list",list);
			model.addAttribute("listCnt",listCnt);

			return "main/phonelog";
		} else {
			return "redirect:/member";
		}
	}
	
	@RequestMapping("userList")
	public String userList(Model model,
			@RequestParam(required = false) Integer page_no,
			@RequestParam(required = false) Integer page_row) {
		
		if(USER_TYPE == 100) {

			Map<String, Object> params = new HashMap<String, Object>();

			if(page_row == null) page_row = 30;
			if(page_no == null) page_no = 1; 

			model.addAttribute("pageCnt", (page_no-1)*page_row);

			params.put("start"		 , (page_no-1)*page_row);
			params.put("page_row"	 , page_row);

			System.out.println(params);

			List<Map<String, Object>> list = adminDao.findUserList(params);  
			Integer listCnt = adminDao.findUserListCnt(params);

			page_no = (page_no-1)/5;

			List<Integer> pageList = new ArrayList<Integer>();
			for(int i=0 ; i<=4;i++) {
				pageList.add((page_no*5)+1+i);
			}

			model.addAttribute("user_id",USER_ID);
			model.addAttribute("user_type",USER_TYPE);
			model.addAttribute("id",ID);
			model.addAttribute("pageList", pageList);
			model.addAttribute("list", list);
			model.addAttribute("listCnt",listCnt);

			return "main/userlist";
		} else {
			return "redirect:/member";
		}
	}
	
	@RequestMapping("createUser")
	public String createUser(
			@RequestParam(required = false) String user_id,
			@RequestParam(required = false) String user_pw,
			@RequestParam(required = false) String user_name
			) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("user_id", user_id);
		params.put("user_pw", user_pw);
		params.put("user_name", user_name);
		
		adminDao.insertUser(params);
		
		return "redirect:/adminManager";
	}
}
