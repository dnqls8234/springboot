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

	public List<Map<String, Object>> findUserList(Map<String, Object> params) {

		return getSqlSession().selectList("AdminDao.findUserList", params);
	}
	
	public List<Map<String, Object>> findPhoneLog(Map<String, Object> params) {

		return getSqlSession().selectList("AdminDao.findPhoneLog", params);
	}

	public Integer findPhoneLogCnt(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return getSqlSession().selectOne("AdminDao.findPhoneLogCnt", params);
	}

	public Integer findUserListCnt(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return getSqlSession().selectOne("AdminDao.findUserListCnt", params);
	}

}
