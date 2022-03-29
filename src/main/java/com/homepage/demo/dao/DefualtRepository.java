package com.homepage.demo.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

public class DefualtRepository {

	@Autowired
	private SqlSession sqlSession;
	
	public SqlSession getSqlSession () {
		return sqlSession;
	}
	
	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}
	
}
