package com.homepage.demo.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homepage.demo.dao.AdminDao;

@Service
public class LoginService {
	
	@Autowired
	AdminDao adminDao;

	public Map<String, Object> checkLogin(HttpServletRequest request) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		Map<String, Object> login = adminDao.findByUserId(user_id);
		
		if(login == null ) {
			ret.put("login_success", false);
			ret.put("error_msg", "해당아이디가 없습니다.");
		} else if (!login.get("user_pw").toString().equals(user_pw) ) {
			ret.put("login_success", false);
			ret.put("error_msg", "해당아이디의 비밀번호가 다릅니다.");
		} else {
			ret.put("login_success", true);
			HttpSession session = request.getSession();
			
			setSession(session, login);
		}
		return ret;
	}
	
	public void setSession(HttpSession session, Map<String, Object> login) {

		session.setAttribute("id", login.get("id"));
		session.setAttribute("user_type", login.get("user_type"));
		session.setAttribute("user_id", login.get("user_id"));
	}

}
