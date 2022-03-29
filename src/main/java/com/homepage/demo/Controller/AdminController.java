package com.homepage.demo.Controller;

import java.io.IOException;
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
//			response.sendRedirect("/member");
			return "redirect:/member";
		}
	}
	
	@RequestMapping("phoneLog")
	public String phoneLog(Model model) {
		List<Map<String, Object>> list = adminDao.findPhoneLog();
		
		model.addAttribute("user_id",USER_ID);
		model.addAttribute("user_type",USER_TYPE);
		model.addAttribute("id",ID);
		model.addAttribute("list",list);

		return "main/phonelog";
	}
	
	@RequestMapping("userList")
	public String userList(Model model) {
		List<Map<String, Object>> list = adminDao.findUserList();  
		
		model.addAttribute("user_id",USER_ID);
		model.addAttribute("user_type",USER_TYPE);
		model.addAttribute("id",ID);
		model.addAttribute("list", list);

		return "main/userlist";
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
