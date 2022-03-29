package com.homepage.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class MainDao extends DefualtRepository{
	
	public String findByid(Integer id) {
		
		return getSqlSession().selectOne("mainDao.findByid" , id);
	} 

	public void insertExcelTest(List<Map<String, Object>> list) {
		
		for(Map<String, Object> map : list) {

			getSqlSession().insert("mainDao.insert", map);
		}
		
	}

	public List<Map<String, Object>> search(Map<String, Object> params) {
		
		return getSqlSession().selectList("mainDao.search", params);
	}
	
}

