package com.homepage.demo.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;

public class SessionController {

	protected String USER_ID;
	protected Integer USER_TYPE;
	protected Integer ID;
	
	public void setSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		
		ID = Integer.parseInt(session.getAttribute("id").toString());
		USER_TYPE = Integer.parseInt(session.getAttribute("user_type").toString());
		USER_ID = session.getAttribute("user_id").toString();
	}
	
	public void setModelAndView(HttpServletRequest request, ModelAndView modelAndView) {
		
		modelAndView.addObject("user_id", USER_ID);
		modelAndView.addObject("user_type", USER_TYPE);
		modelAndView.addObject("id", ID );
		
	}
}
