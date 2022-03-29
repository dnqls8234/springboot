package com.homepage.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class MainSubDao extends DefualtRepository{
	
	public String findByid(Integer id) {
		
		return getSqlSession().selectOne("mainDao.findByid" , id);
	} 

	public void insertExcelTest(List<Map<String, Object>> list) {
		
		for(Map<String, Object> map : list) {

			getSqlSession().insert("mainDaoSub.insert", map);
		}
		
	}

	public List<Map<String, Object>> search(Map<String, Object> params) {
		
		return getSqlSession().selectList("mainDaoSub.search", params);
	}

	public Integer searchCnt(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return getSqlSession().selectOne("mainDaoSub.searchCnt",params);
	}
	
}
