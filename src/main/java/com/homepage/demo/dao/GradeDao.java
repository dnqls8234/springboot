package com.homepage.demo.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class GradeDao extends DefualtRepository {

	public List<Map<String, Object>> ReadGrade() {

		return getSqlSession().selectList("grade.readGrade");
	}

	public List<Map<String, Object>> ReadGrade2() {

		return getSqlSession().selectList("grade.readGrade2");
	}
	
	public int UpdateGrade(ArrayList<Grade> gradelist) {

		return getSqlSession().update("grade.updateGrade", gradelist);
	}
}
