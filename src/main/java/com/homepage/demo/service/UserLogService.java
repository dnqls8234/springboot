package com.homepage.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homepage.demo.dao.AdminDao;
 
@Service
public class UserLogService {

	@Autowired
	AdminDao loginDao;
	
	public void userLogInsert(Integer id) {
		
		Map<String, Object> login_user = loginDao.findById(id);
		
		Integer chk = loginDao.userLogInsert(login_user);
		
		
	}
	
}
