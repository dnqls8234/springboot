package com.homepage.demo.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommonUtils {
	public static String dateToString(Date dt, String format) {
		SimpleDateFormat transFormat = new SimpleDateFormat(format);
		return transFormat.format(dt);
	}
	
	public static Date stringToDate(String datestr, String format) throws ParseException {
		if (datestr != null && !datestr.equals("")) {
			SimpleDateFormat transFormat = new SimpleDateFormat(format);
			return transFormat.parse(datestr);
		} else {
			return null;
		}
	}
	
	public static Date addHour(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);
		cal.add(Calendar.HOUR, amount);
		
		return cal.getTime();
	}
	
	public static Date addDate(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);
		cal.add(Calendar.DATE, amount);
		
		return cal.getTime();
	}
	
	public static Date addMonth(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);
		cal.add(Calendar.MONTH, amount);
		
		return cal.getTime();
	}
	
	public static Date addYear(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);
		cal.add(Calendar.YEAR, amount);
		
		return cal.getTime();
	}
	
	public static String removeComma(String string) {
		return isEmpty(string) ? null : string.replaceAll(",", "");
	}
	
	public static Long stringToLong(String number) {
		return isEmpty(number) ? null : Long.valueOf(removeComma(number));
	}
	
	public static Integer stringToInteger(String number) {
		return isEmpty(number) ? null : Integer.valueOf(removeComma(number));
	}
	
	public static Float stringToFloat(String number) {
		return isEmpty(number) ? null : Float.valueOf(removeComma(number));
	}
	
	public static Double stringToDouble(String number) {
		return isEmpty(number) ? null : Double.valueOf(removeComma(number));
	}

	public static String emptyZero(String var) {
		return empty(var, "0");
	}
	
	public static String empty(String var) {
		return empty(var, "");
	}
	
	public static String empty(String var, String defVar) {
		if (isEmpty(var)) {
			return defVar;
		}
		
		return var;
	}
	
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true; } 
		if ((obj instanceof String) && ((String)obj).equals("")) {
			return true; } 
		if (obj instanceof Map) {
			return ((Map<?, ?>)obj).isEmpty(); } 
		if (obj instanceof List) {
			return ((List<?>)obj).isEmpty(); } 
		if (obj instanceof Object[]) {
			return (((Object[])obj).length == 0); } 
		return false;


	}
	
	public static String emptyReplaceString(Object data) {
		return emptyReplaceString(data, "");
	}
	
	public static String emptyReplaceString(Object data, final String replacement) {
		if(data == null) {
			if(replacement == null) {
				return "";
			}
			
			return replacement;
		}
		
		return data.toString();
	}
	
	public static String objectToJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}
	
	public static String mapToJsonString(Map map) throws IOException {
		return objectToJsonString(map);
	}
	
	public static String listToJsonString(List list) throws IOException {
		return objectToJsonString(list);
	}
	
	public static String arrayToJsonString(Object[] array) throws IOException {
		return objectToJsonString(array);
	}
	
	public static Map<String, Object> jsonStringToMap(String jsonString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
	}
	
	public static List<Map<String, Object>> jsonStringToList(String jsonString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>(){});
	}
	
	public static List<List<String>> scan(String regex, String str) {
		// String 값에 \r or \n 없으면 한줄에서는 1개만 찾게됨.
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		List<List<String>> lilst = new ArrayList<List<String>>();
		
		do {
			if (!m.find()) break;

			for (int i=1; i<=m.groupCount(); i++) {
				if(lilst.size()<i) {
					lilst.add(new ArrayList<String>());
				}
				
				lilst.get(i-1).add(m.group(i));
			}
		
		}while(true); 
		
		return lilst;
	}
	
	public static String getQureyString(Map<String, ?> params) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		
		for (String key : params.keySet()) {
			builder.queryParam(key, params.get(key));
		}
		
		return builder.build().getQuery();
	}
	
	public static String getQureyString(MultiValueMap<String, String> params) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		
		for (String key : params.keySet()) {
			builder.queryParam(key, params.get(key));
		}
		
		return builder.build().getQuery();
	}
	
	public static String getUriString(String url, Map<String, ?> params) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(url)
				.query(getQureyString(params));
		
		return builder.build().toUriString();
	}
	
	public static String getUriString(String url, MultiValueMap<String, String> params) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(url)
				.query(getQureyString(params));
		
		return builder.build().toUriString();
	}
	
	// 리소스를 읽어 String으로 반환
	public static String resourceToString(String resourcePath) {
		String htmlStr = "";

		ClassPathResource cpr = new ClassPathResource(resourcePath);

		try {
			byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());

			htmlStr = new String(bdata, StandardCharsets.UTF_8);
		} catch (Exception e) {}

		return htmlStr;
	}

	// 디바이스명 가져오기
	public static String getDevice(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent").toUpperCase();

		if (userAgent.indexOf("MOBILE") == -1) {
			return "PC";
		} else {
			if (userAgent.indexOf("PHONE") == -1) {
				return "TABLET";
			} else {
				return "MOBILE";
			}
		}
	}
	
	// 브라우저명 가져오기 
	public static String getBrowser(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent").toUpperCase();

		if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("TRIDENT/7.0") > -1) {
			return "MSIE";
		} else if (userAgent.indexOf("CHROME") > -1) {
			return "CHROME";
		} else if (userAgent.indexOf("OPERA") > -1) {
			return "OPERA";
		} else {
			return "FIREFOX";
		}
	}
	
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String XRequestedWith = request.getHeader("X-Requested-With");
		
		if (XRequestedWith == null) return false;
		
		return XRequestedWith.equals("XMLHttpRequest");
	}
	
	public static String generatedUUID20() {
		UUID u = UUID.randomUUID();
		long l = ByteBuffer.wrap(u.toString().getBytes()).getLong();
		String ret = Long.toString(l, Character.MAX_RADIX);
		ret = StringUtils.leftPad(ret, 20, '0');
		return ret;
	}
	
	// 문자열 앞과 뒤 화이트스페이스 문자 제거
	public static String strip(String s) {
		int beginIndex = 0;
		
		while (beginIndex < s.length() && Character.isWhitespace(s.charAt(beginIndex))) {
			beginIndex++;
		}
		
		int endIndex = s.length()-1;
		
		while (endIndex >= 0 && Character.isWhitespace(s.charAt(endIndex))) {
			endIndex--;
		}
		
		return s.substring(beginIndex, endIndex+1);
	}
	
	// htmlString에서 텍스트 외에 모든 태그 제거
	public static String strip_tags(String htmlString) {
		return Jsoup.clean(htmlString, Whitelist.none());
	}
	
	public static String encode(String string, Charset charset) throws CharacterCodingException {
		CharsetEncoder encoder = charset.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
		        .onUnmappableCharacter(CodingErrorAction.REPLACE);
		
		return encoder.encode(CharBuffer.wrap(string)).toString();
	}
	
	public static String encodeUTF8(String string) throws CharacterCodingException {
		return encode(string, StandardCharsets.UTF_8);
	}
	
	public static String encodeEucKr(String string) throws CharacterCodingException {
		return encode(string, Charset.forName("EUC-KR"));
	}
	
	public static Document getXmlDocumentUTF8(String xmlString) throws Exception {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
	}
	
	public static Document getXmlDocumentEucKr(String xmlString) throws Exception {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(xmlString.getBytes("EUC-KR")));
	}
	
	public static NodeList getNodeListForXpath(Object object, String expression) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		try {
			return (NodeList)xpath.evaluate(expression, object, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			return null;
		}
	}
	
	public static String ecapse(String jsString) {
	    jsString = jsString.replace("\\", "\\\\");
	    jsString = jsString.replace("\"", "\\\"");
	    jsString = jsString.replace("\b", "\\b");
	    jsString = jsString.replace("\f", "\\f");
	    jsString = jsString.replace("\n", "\\n");
	    jsString = jsString.replace("\r", "\\r");
	    jsString = jsString.replace("\t", "\\t");
	    //jsString = jsString.replace("/", "\\/");
	    return jsString;
	}
}
