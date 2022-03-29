package com.homepage.demo.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.homepage.demo.Controller.MainController;
import com.homepage.demo.Controller.SessionController;

@Component
public class DefaultInteceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		System.out.println("인터셉터");
		System.out.println(handler.toString()+"@@@@@@@@@@@@@@@@@@@@@@");
		
		System.out.println("-----------------1");
		if(!(handler instanceof HandlerMethod)) return false;
		System.out.println("-----------------2");
		HandlerMethod method = (HandlerMethod)handler;
		
		HttpSession session = request.getSession();
		
		// SessionController를 상속 받지 않은 Controller는 인터셉트 처리안함.
		if (!(method.getBean() instanceof SessionController)) {
			return true;
		}
		
		String login_user_id = session.getAttribute("user_id") == null ? null : session.getAttribute("user_id").toString();
		
	
			if (login_user_id == null) {
				System.out.println("여기서 에러 ?");
				response.sendRedirect("/");
					return false;
			}
		
		
		SessionController sessionController = (SessionController) method.getBean();
		
		sessionController.setSession(request);
		
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		System.out.println("인터셉터 뷰설정인데 왜 안되냐");
		HandlerMethod method = (HandlerMethod)handler;
		
		if (method.getBean() instanceof SessionController && method.getMethod().getName().equals("index")) {
			SessionController sessionController = (SessionController)method.getBean();
			
			sessionController.setModelAndView(request, modelAndView);
		}	}
}
