package com.homepage.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class AdminDao extends DefualtRepository{
	
	public Map<String, Object> findByUserId(String user_id) {
		
		return getSqlSession().selectOne("AdminDao.findByUserId", user_id);
	}
	
	public Map<String, Object> findById(Integer id) {
		
		return getSqlSession().selectOne("AdminDao.findById", id);
	}

	public Integer userLogInsert(Map<String, Object> login_user) {
		
		return getSqlSession().selectOne("AdminDao.userLogInsert", login_user);
		
	}

	public Integer insertUser(Map<String, Object> params) {
		
		return getSqlSession().selectOne("AdminDao.insertUser", params);
	}

	public List<Map<String, Object>> findUserList() {

		return getSqlSession().selectList("AdminDao.findUserList");
	}
	
	public List<Map<String, Object>> findPhoneLog() {

		return getSqlSession().selectList("AdminDao.findPhoneLog");
	}

}
