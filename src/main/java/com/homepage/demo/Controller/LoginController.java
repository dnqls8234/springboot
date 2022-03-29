package com.homepage.demo.Controller;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.homepage.demo.dao.AdminDao;
import com.homepage.demo.service.LoginService;

@Controller
@RequestMapping("/")
public class LoginController {
	
	@Autowired
	AdminDao loginDao;
	
	@Autowired
	LoginService loginService;
	
	@GetMapping
	public String index(Locale locale, Model model) {
		return "login";
	}
	
	@PostMapping("login/login")
	@ResponseBody
	public Map<String,Object> checkId(HttpServletRequest request) {
		
		return loginService.checkLogin(request);
	}
	
	@RequestMapping("logout")
	@ResponseBody
	public void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
		System.out.println("로그아웃");
		HttpSession session = req.getSession();
		
		session.invalidate();
		
		res.sendRedirect("/");
//		return "redirect://";
	}

}
