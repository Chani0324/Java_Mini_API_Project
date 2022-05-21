package open.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

// 네이버 기계번역 (Papago SMT) API 예제
public class ApiExamTranslateNmt {
	// step 01
	// String문자열을 JSON 포멧으로 변경해 보기
	/*
	 * 문자열에서 번역된 데이터 반환해서 활용
	 * 검색 키워드 : string json java
	 * 
	 * json simple library 사용해서 필요 data만 추출(python dictionary구조와 유사)
	 * 	- key로 value
	 */
	public static void stringToJson(String v) {
		// JSON 포멧과 관련된 작업 - 문법 검증 및 변환
		JSONParser jsonParser = new JSONParser();

		try {
			// 문자열 타입을 JSON 구조의 객체로 변환
			// key로 value를 구분해서 활용 가능하게 하는 API
			JSONObject jsonObj1 = (JSONObject) jsonParser.parse(v);
//			System.out.println(jsonObj1);
//			System.out.println(jsonObj1.get("message"));
//
			// value구조가 중첩된 json포멧인 경우 새롭게 JSONObject 생성
			JSONObject jsonObj2 = (JSONObject) jsonObj1.get("message");
//			System.out.println(jsonObj2.get("result"));
			JSONObject jsonObj3 = (JSONObject) jsonObj2.get("result");
			System.out.println(jsonObj3.get("translatedText")); // The weather is nice

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// step02 리펙토링
	/*
	 * 형변환 문법으로 인해서 코드 line을 줄이려 했더니 에러
	 * 즉 간소화보단 가독성이 너무 저해되는 이슈로 인해 각각의 변수 선언 및 활용 유지
	 */
	public static void stringToJson2(String v) {
		JSONParser jsonParser = new JSONParser();
		
		try {
			JSONObject jsonObj1 = (JSONObject) jsonParser.parse(v);
			JSONObject jsonObj2 = (JSONObject) jsonObj1.get("message");
			JSONObject jsonObj3 = (JSONObject) jsonObj2.get("result");
			
			System.out.println(jsonObj3.get("translatedText")); 
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public static String fileReadingTest(int i) {
		BufferedReader in = null;
		String readData = null;
//		ArrayList<String> nm = new ArrayList<String>();
		String result = "";
		try {
			
			in = new BufferedReader(new FileReader("20220520미션.txt"));

			// read한 데이터를 대입받는 변수, 데이터가 있으면 String객체, 없을 경우 null
			while ((readData = in.readLine()) != null) {
				result = result + readData;
//				System.out.println(readData);
//				if (readData.length() > 0) {
//				nm.add(readData);
//				}
			}
			

		} catch (FileNotFoundException e) { // file이 없을 경우 실행
			e.printStackTrace();
		} catch (IOException e) { // 데이터 read시에 예외 발생시
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;

	}
	
	public static void main(String[] args) {
		String clientId = "Ma5Uq_VDdJBBOCsGzHoz";// 애플리케이션 클라이언트 아이디값";
		String clientSecret = "fx0w0djxYr";// 애플리케이션 클라이언트 시크릿값";

		String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
		String text = null;
		try {
			text = URLEncoder.encode(fileReadingTest(1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("인코딩 실패", e);
		}

		Map<String, String> requestHeaders = new HashMap<>();
		requestHeaders.put("X-Naver-Client-Id", clientId);
		requestHeaders.put("X-Naver-Client-Secret", clientSecret);

		// 한글 데이터 제공 후 번역 요청 후에 응답받은 결과
		String responseBody = post(apiURL, requestHeaders, text);

//		System.out.println(responseBody);
		// 번역된 부분만 출력하게(json안씀)
		char[] nn = new char[(responseBody.indexOf("engineType")) - responseBody.indexOf("translatedText")];
//        System.out.println(responseBody.indexOf("translatedText"));
//        System.out.println(responseBody.indexOf("engineType"));
		responseBody = responseBody.replace("\"", "");
		responseBody = responseBody.replace(",", "");
		responseBody.getChars(responseBody.indexOf("translatedText"), responseBody.indexOf("engineType"), nn, 0);
		System.out.println(nn);

//		stringToJson(responseBody);
	}

	private static String post(String apiUrl, Map<String, String> requestHeaders, String text) {
		HttpURLConnection con = connect(apiUrl);
		String postParams = "source=ko&target=en&text=" + text; // 원본언어: 한국어 (ko) -> 목적언어: 영어 (en)
		try {
			con.setRequestMethod("POST");
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}

			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				wr.write(postParams.getBytes());
				wr.flush();
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
				return readBody(con.getInputStream());
			} else { // 에러 응답
				return readBody(con.getErrorStream());
			}
		} catch (IOException e) {
			throw new RuntimeException("API 요청과 응답 실패", e);
		} finally {
			con.disconnect();
		}
	}

	private static HttpURLConnection connect(String apiUrl) {
		try {
			URL url = new URL(apiUrl);
			return (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
		} catch (IOException e) {
			throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
		}
	}

	private static String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body);

		try (BufferedReader lineReader = new BufferedReader(streamReader)) {
			StringBuilder responseBody = new StringBuilder();

			String line;
			while ((line = lineReader.readLine()) != null) {
				responseBody.append(line);
			}

			return responseBody.toString();
		} catch (IOException e) {
			throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
		}
	}
}