package com.homepage.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homepage.demo.dao.GradeDao;

@Service
public class GradeService {

	@Autowired
	GradeDao gradedao;
	
	public void updateGrade(Map<String, Object> gradeList) {

		List<Map<String,Object>> gradelist = gradedao.ReadGrade();
		System.out.println(gradelist.get(0).toString());
		System.out.println(gradelist.get(1).toString());
		
		ArrayList<String> idList = new ArrayList<>();
		
		for(int i = 0; i<gradelist.size(); i++) {
			idList.add(gradelist.get(i).get("user_id").toString());
		}
		
		HashMap<String, Integer> duplicate_count = new HashMap<String, Integer>();
        
		for(int i = 0 ; i < idList.size() ; i++){ // ArrayList 만큼 반복
		    if (duplicate_count.containsKey(idList.get(i))) { // HashMap 내부에 이미 key 값이 존재하는지 확인
		        duplicate_count.put(idList.get(i), duplicate_count.get(idList.get(i))  + 1);  // key가 이미 있다면 value에 +1
		    } else { // key값이 존재하지 않으면
		        duplicate_count.put(idList.get(i) , 1); // key 값을 생성후 value를 1로 초기화
		    }
		}
		
		System.out.println(duplicate_count.get("abc11"));
		
		String grade = "";
		
	}
}
