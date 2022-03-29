package com.homepage.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;


@Repository
public class PurchaseDao extends DefualtRepository{

	public List<Map<String, Object>> getPurchases(Map<String, Object> params){
		
		return getSqlSession().selectList("PuchaseDao.getPurchases", params);
	}
	
	public void insertExcelTest(List<Map<String, Object>> list) {
		
		for(Map<String, Object> map : list) {

			getSqlSession().insert("PuchaseDao.insert", map);
		}
		
	}
	
}
