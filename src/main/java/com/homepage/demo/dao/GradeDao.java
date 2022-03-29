package com.homepage.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class GradeDao extends DefualtRepository{

public List<Map<String, Object>> ReadGrade() {
		
		return getSqlSession().selectList("grade.readGrade");
	}
}
