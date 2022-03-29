package com.homepage.demo.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homepage.demo.dao.Grade;
import com.homepage.demo.dao.GradeDao;

@Service
public class GradeService {

	@Autowired
	GradeDao gradedao;

	int grade1;
	int grade2;
	String grade = "";
	Date registerDate;
	Date dateFir;
	Date dateSec;
	Date dateThi;
	Date dateFou;
	Date dateFif;
	int count;
	int count1;
	int count2;
	int count3;
	int count4;
	int count5;

	public void updateGrade(Map<String, Object> gradeList) {
		try {
			List<Map<String, Object>> gradelist1 = gradedao.ReadGrade();
			List<Map<String, Object>> gradelist2 = gradedao.ReadGrade2();

			ArrayList<String> idList = new ArrayList<>();

			for (int i = 0; i < gradelist1.size(); i++) {
				idList.add(gradelist1.get(i).get("user_id").toString());
			}

			HashMap<String, Integer> duplicate_count = new HashMap<String, Integer>();

			for (int i = 0; i < idList.size(); i++) { // ArrayList 만큼 반복
				if (duplicate_count.containsKey(idList.get(i))) { // HashMap 내부에 이미 key 값이 존재하는지 확인
					duplicate_count.put(idList.get(i), duplicate_count.get(idList.get(i)) + 1); // key가 이미 있다면 value에 +1
				} else { // key값이 존재하지 않으면
					duplicate_count.put(idList.get(i), 1); // key 값을 생성후 value를 1로 초기화
				}
			}
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");// dd/MM/yyyy
			dateFir = sdfDate.parse(gradeList.get("dateFir").toString());
			dateSec = sdfDate.parse(gradeList.get("dateSec").toString());
			dateThi = sdfDate.parse(gradeList.get("dateThi").toString());
			dateFou = sdfDate.parse(gradeList.get("dateFou").toString());
			dateFif = sdfDate.parse(gradeList.get("dateFif").toString());
			count1 = Integer.parseInt(gradeList.get("buyFir").toString());
			count2 = Integer.parseInt(gradeList.get("buySec").toString());
			count3 = Integer.parseInt(gradeList.get("buyThi").toString());
			count4 = Integer.parseInt(gradeList.get("buyFif").toString());
			count5 = Integer.parseInt(gradeList.get("buyFou").toString());

			ArrayList<Grade> gradelist = new ArrayList<>();

			for (int i = 0; i < gradelist2.size(); i++) {

				registerDate = sdfDate.parse(gradelist2.get(i).get("register_at").toString());
				count = duplicate_count.get(gradelist2.get(i).get("user_id").toString());

				if (registerDate.before(dateFir) && count >= count1) {
					grade1 = 1;
				} else if (registerDate.after(dateFir) && registerDate.before(dateSec)) {
					grade1 = 2;
				} else if (registerDate.after(dateSec) && registerDate.before(dateThi)) {
					grade1 = 3;
				} else if (registerDate.after(dateThi) && registerDate.before(dateFou)) {
					grade1 = 4;
				} else if (registerDate.after(dateFou) && registerDate.before(dateFif)) {
					grade1 = 5;
				} else {
					grade1 = 9;
				}

				if (count >= count1) {
					grade2 = 1;
				} else if (count < count1 && count >= count2) {
					grade2 = 2;
				} else if (count < count2 && count >= count3) {
					grade2 = 3;
				} else if (count < count3 && count >= count4) {
					grade2 = 4;
				} else if (count < count4 && count >= count5) {
					grade2 = 5;
				} else {
					grade2 = 9;
				}
				
				if(grade1 < grade2) {
					grade = grade2 + "등급";
				}else if(grade2 < grade1) {
					grade = grade1 + "등급";
				}else if(grade2 == grade1) {
					grade = grade1 + "등급";
				}
				
				
				Grade gradeInstance = new Grade();
				gradeInstance.setId(gradelist2.get(i).get("user_id").toString());
				gradeInstance.setGrade(grade);
				gradelist.add(gradeInstance);
			}

			int update = gradedao.UpdateGrade(gradelist);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		;
	}
}
