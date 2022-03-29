package com.homepage.demo.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.homepage.demo.service.GradeService;

@Controller
@RequestMapping("grade")
public class GradeController extends SessionController{

	@Autowired
	GradeService gradeservice;
	
	@RequestMapping
	public String index() {
		
		return "main/grade";
	}
	
	@RequestMapping("memberGrade")
	public String grade(Model model,
			@RequestParam(required = false) String dateFir,
			@RequestParam(required = false) String dateSec,
			@RequestParam(required = false) String dateThi,
			@RequestParam(required = false) String dateFou,
			@RequestParam(required = false) String dateFif,
			@RequestParam(required = false) Integer buyFir,
			@RequestParam(required = false) Integer buySec,
			@RequestParam(required = false) Integer buyThi,
			@RequestParam(required = false) Integer buyFou,
			@RequestParam(required = false) Integer buyFif
			) {
		
		System.out.println(dateFir);
		
		Map<String,Object> gradeList = new HashMap<String, Object>(); 
		
		gradeList.put("dateFir",dateFir);
		gradeList.put("dateSec",dateSec);
		gradeList.put("dateThi",dateThi);
		gradeList.put("dateFou",dateFou);
		gradeList.put("dateFif",dateFif);
		gradeList.put("buyFir",buyFir);
		gradeList.put("buySec",buySec);
		gradeList.put("buyThi",buyThi);
		gradeList.put("buyFou",buyFou);
		gradeList.put("buyFif",buyFif);
		
		gradeservice.updateGrade(gradeList);
		
		return "redirect:/grade";
	}
}
