package com.homepage.demo.dao;

public class Grade {

	private String id;
	private String grade;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	@Override
	public String toString() {
		return "Grade [id=" + id + ", grade=" + grade + "]";
	}
	
}
