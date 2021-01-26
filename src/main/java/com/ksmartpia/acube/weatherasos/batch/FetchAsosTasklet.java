package com.ksmartpia.acube.weatherasos.batch;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ksmartpia.acube.weatherasos.model.AsosVO;
import com.ksmartpia.acube.weatherasos.model.UltraSrtNcstVO;
import com.ksmartpia.acube.weatherasos.model.UltraSrtVO;
import com.ksmartpia.acube.weatherasos.model.VilageFcstVO;
import com.ksmartpia.acube.weatherasos.model.VilageNcstVO;
import com.ksmartpia.acube.weatherasos.model.WntyNcstVO;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class FetchAsosTasklet {

	static Logger log = Logger.getLogger(FetchAsosTasklet.class.getName());
	
	
	// 민과장님 인증키 - 만료된듯
//	public String asosServiceKey = "X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D";

	// 수림 인증키 
	public String asosServiceKey = "jGWp9%2FCQcmUmn%2FV6Nxwaa5lFeTGmtYNj2OQ4iHH%2BKD2UmFA9g%2BPhX4EhM6OzMlQXlRjErdtDt%2BEF0UvavkXhwg%3D%3D";
	
	public String ultraSrtKey = "tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D";
	public String wntyNcstKey = "tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D";
	public String vilageFcstKey = "tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D";
	public Calendar nowDate = Calendar.getInstance();
	public SimpleDateFormat date_sdf = new SimpleDateFormat("yyyyMMdd");
	public SimpleDateFormat time_sdf = new SimpleDateFormat("HHmm");
	public SimpleDateFormat datetime_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
	
	/** 공공데이터 포털 서비스명 */
	public String getWthrDataListServie = "getWthrDataList"; //지상(종관, ASOS) 시간자료 조회 서비스
	public String getUltraSrtNcstServie = "getUltraSrtNcst"; //동네예보 - 초단기실황 조회 서비스
	public String getWntyNcstServie = "getWntyNcst"; //지상(종관, ASOS) 기상관측자료 조회 서비스
	public String getVilageFcstServie = "getVilageFcst"; //지상(종관, ASOS) 기상관측자료 조회 서비스
	@Inject
	public AsosDAO asosDao;

	/**
	 * ASOS 종관 기상 정보
	 * @author taiseo
	 * @throws Exception
	 * 
	 * 대구권 기상정보를 수집하는 API
	 * 당일 정보는 개인이 사용불가, 전날 데이터를 1시간 단위로 저장함. (00시~ 23시)
	 * 
	 */  
	@Scheduled(cron="0 0 3 * * *")
	public void getAsosInfo() throws Exception {
		System.out.println("ASOS 종관 기상 정보 시작");
		List<AsosVO> list = new ArrayList<AsosVO>();
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			String url = getApiUrl("asos", 0, 0);

			Request request = new Request.Builder().url(url)
					.get()
					.build();
			
			Response response = client.newCall(request).execute();
			
			if (response.isSuccessful()) {
				String body = response.body().string();
				response.body().close();
				
				// 통신 성공시 vo에 정보 매핑함
				list = newAsosRecord(body);
				
				if(list.size() > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("list", list);
					int insertCnt = this.asosDao.setASOSInfo(map);
					System.out.println("ASOS 종관 기상 정보 Insert CNT : ["+insertCnt+"]");
				}
				
			} else {
				throw new IOException("Unexpected code " + response);
			}
			
		} catch (SocketTimeoutException ste) {
			System.out.println("ASOS SocketTimeoutException : ["+ste.getMessage()+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 초단기실황 조회
	 * @author taiseo
	 * @throws Exception
	 * 
	 * 구역별로 기상정보를 수집하는 API
	 * 기상청 X,Y 좌표별로 구역 나눔 - 행정동 기준
	 * 당일 정보 사용 가능.
	 * 
	 */
	@Scheduled(cron="0 30 * * * *")
	public void ultraSrtInfoGet() throws Exception {
		System.out.println("초단기 실황 정보 시작");
		List<Map<String, Object>> gridList = this.asosDao.getGridInfo();
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			for(Map<String, Object> gridMap : gridList) {
				List<UltraSrtVO> list = new ArrayList<UltraSrtVO>();
				int nx = (int) gridMap.get("nx");
				int ny = (int) gridMap.get("ny");
				
				String url = getApiUrl("ultrancst", nx, ny);
	
				Request request = new Request.Builder().url(url)
						.get()
						.build();
				
				Response response = client.newCall(request).execute();
				
				if (response.isSuccessful()) {
	
					String body = response.body().string();
					response.body().close();
					
					System.out.println("body : ["+body+"]");
					list = newUltraSrtRecord(body);
					System.out.println("list.size() : ["+list.size()+"]");
					
					if(list.size() > 0) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("list", list);
						int insertCnt = this.asosDao.setUltraSrtNcstInfo(map);
						System.out.println("초단기 실황 정보 Insert CNT : ["+insertCnt+"]");
					}
				} else {
					throw new IOException("Unexpected code " + response);
				}
			}
		} catch (SocketTimeoutException ste) {
			System.out.println("UltraSrcNcst SocketTimeoutException : ["+ste.getMessage()+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 동네예보 조회
	 * @author seongheum
	 * @throws Exception
	 * 
	 * 구역별로 기상정보를 수집하는 API
	 * 기상청 X,Y 좌표별로 구역 나눔 - 행정동 기준
	 * 당일 정보 사용 가능.
	 * 
	 */         //   초 분 시 일 요일 연도 
	@Scheduled(cron="0 10 2 * * *")
	public void vilageFcstInfoGet() throws Exception {
		System.out.println("동네예보 정보 시작");
		List<Map<String, Object>> gridList = this.asosDao.getGridInfo();
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			for(Map<String, Object> gridMap : gridList) {
				List<VilageFcstVO> list = new ArrayList<VilageFcstVO>();
				int nx = (int) gridMap.get("nx");
				int ny = (int) gridMap.get("ny");
				
				String url = getApiUrl("vilagefcst", nx, ny);
	
				Request request = new Request.Builder().url(url)
						.get()
						.build();
				
				Response response = client.newCall(request).execute();
				
				if (response.isSuccessful()) {
	
					String body = response.body().string();
					response.body().close();
					
					System.out.println("body : ["+body+"]");
					list = newVilageFcstRecord(body);
					System.out.println("list.size() : ["+list.size()+"]");
					
					if(list.size() > 0) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("list", list);
						int insertCnt = this.asosDao.setVilageFcstInfo(map);
						System.out.println("초단기 실황 정보 Insert CNT : ["+insertCnt+"]");
					}
				} else {
					throw new IOException("Unexpected code " + response);
				}
			}
		} catch (SocketTimeoutException ste) {
			System.out.println("UltraSrcNcst SocketTimeoutException : ["+ste.getMessage()+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 지상(종관, ASOS) 기상관측자료 조회 서비스 - 전국 실황조회 10/01일 서비스 종료
	 * @author taiseo
	 * @throws Exception
	 */
	@Scheduled(cron="0 0/10 * * * *")
	public void setWntyNcstInfo() throws Exception {
		System.out.println("지상(종관, ASOS) 기상관측자료 정보 시작");
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			List<WntyNcstVO> list = new ArrayList<WntyNcstVO>();
			
			String url = getApiUrl("wntyNcst", 0, 0);

			Request request = new Request.Builder().url(url)
					.get()
					.build();
			
			Response response = client.newCall(request).execute();
			
			if (response.isSuccessful()) {

				String body = response.body().string();
				response.body().close();
				
				System.out.println("body : ["+body+"]");
				list = newWntyNcstRecord(body);
				System.out.println("list.size() : ["+list.size()+"]");
				
				if(list.size() > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("list", list);
					for(int el = 0; el < list.size(); el++){
						WntyNcstVO wnVo = list.get(el);
						if(wnVo.getWeatherStatusNumber().equals("")){
							wnVo.setWeatherStatusNumber("0");
						}else{}
						if(wnVo.getRainfallDay().equals("")){
							wnVo.setRainfallDay("0");
						}else{}
						if(wnVo.getNewSnowDay().equals("")){
							wnVo.setNewSnowDay("0.00");
						}else{}
						if(wnVo.getWindDirection().equals("")){
							wnVo.setWindDirection("0");
						}else{}
						list.remove(el);
						list.add(wnVo);
					}
					int insertCnt = this.asosDao.newWntyNcstRecord(map);
					System.out.println("지상(종관, ASOS) 기상관측자료 정보 Insert CNT : ["+insertCnt+"]");
				}
			} else {
				throw new IOException("Unexpected code " + response);
			}
		} catch (SocketTimeoutException ste) {
			System.out.println("UltraSrcNcst SocketTimeoutException : ["+ste.getMessage()+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 지상(종관, ASOS) 기상관측자료 조회 서비스 - 지점별정시관측조회  10/01일 서비스 종료
	 * @author taiseo
	 * @throws Exception
	 */
	//@Scheduled(cron="0 0/10 * * * *")
	public void setStnbyRtmObsInfo() throws Exception {
		System.out.println("지상(종관, ASOS) 기상관측자료 정보 시작");
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			List<WntyNcstVO> list = new ArrayList<WntyNcstVO>();
			
			String url = getApiUrl("wntyNcst", 0, 0);

			Request request = new Request.Builder().url(url)
					.get()
					.build();
			
			Response response = client.newCall(request).execute();
			
			if (response.isSuccessful()) {

				String body = response.body().string();
				response.body().close();
				
				System.out.println("body : ["+body+"]");
				list = newWntyNcstRecord(body);
				System.out.println("list.size() : ["+list.size()+"]");
				
				if(list.size() > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("list", list);
					for(int el = 0; el < list.size(); el++){
						WntyNcstVO wnVo = list.get(el);
						if(wnVo.getWeatherStatusNumber().equals("")){
							wnVo.setWeatherStatusNumber("0");
						}else{}
						if(wnVo.getRainfallDay().equals("")){
							wnVo.setRainfallDay("0");
						}else{}
						if(wnVo.getNewSnowDay().equals("")){
							wnVo.setNewSnowDay("0.00");
						}else{}
						if(wnVo.getWindDirection().equals("")){
							wnVo.setWindDirection("0");
						}else{}
						list.remove(el);
						list.add(wnVo);
					}
					int insertCnt = this.asosDao.newWntyNcstRecord(map);
					System.out.println("지상(종관, ASOS) 기상관측자료 정보 Insert CNT : ["+insertCnt+"]");
				}
			} else {
				throw new IOException("Unexpected code " + response);
			}
		} catch (SocketTimeoutException ste) {
			System.out.println("UltraSrcNcst SocketTimeoutException : ["+ste.getMessage()+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @author taiseo
	 * @param categori
	 * @return api url
	 */
	public String getApiUrl(String categori, int nx, int ny) {

		String url = "";
		HttpUrl urls = null;
		
		switch (categori) {
		case "asos":
			nowDate.setTime(new Date());
			nowDate.add(Calendar.DATE, -1);
			urls = new HttpUrl.Builder().scheme("http")
					.host("apis.data.go.kr").addEncodedPathSegment("1360000")
					.addEncodedPathSegment("AsosHourlyInfoService")
					.addEncodedPathSegment(getWthrDataListServie)
					.addEncodedQueryParameter("serviceKey", asosServiceKey)
					.addEncodedQueryParameter("pageNo", "1")
					.addEncodedQueryParameter("numOfRows", "30")
					.addEncodedQueryParameter("dataType", "JSON")
					.addEncodedQueryParameter("dataCd", "ASOS")
					.addEncodedQueryParameter("dateCd", "HR")
					.addEncodedQueryParameter("startDt", date_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("startHh", "00")
					.addEncodedQueryParameter("endDt", date_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("endHh", "23")
					.addEncodedQueryParameter("stnIds", "143")
					.addEncodedQueryParameter("schListCnt", "10")
					.build();
			url = urls.toString();
			break;
		
		case "ultrancst":
			nowDate.setTime(new Date());
			urls = new HttpUrl.Builder().scheme("http")
					.host("apis.data.go.kr").addEncodedPathSegment("1360000")
					.addEncodedPathSegment("VilageFcstInfoService")
					.addEncodedPathSegment(getUltraSrtNcstServie)
					.addEncodedQueryParameter("serviceKey", ultraSrtKey)
					.addEncodedQueryParameter("pageNo", "1")
					.addEncodedQueryParameter("numOfRows", "300")
					.addEncodedQueryParameter("dataType", "JSON")
					.addEncodedQueryParameter("base_date", date_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("base_time", time_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("nx", Integer.toString(nx))
					.addEncodedQueryParameter("ny", Integer.toString(ny))
					.build();
			url = urls.toString();
			break;
		case "wntyNcst":
			urls = new HttpUrl.Builder().scheme("http")
					.host("apis.data.go.kr").addEncodedPathSegment("1360000")
					.addEncodedPathSegment("SfcInfoService")
					.addEncodedPathSegment(getWntyNcstServie)
					.addEncodedQueryParameter("serviceKey", wntyNcstKey)
					.addEncodedQueryParameter("numOfRows", "10")
					.addEncodedQueryParameter("pageNo", "1")
					.addEncodedQueryParameter("dataType", "JSON")
					.addEncodedQueryParameter("stnId", "143")
					.build();
			url = urls.toString();
			//vilagefcst
		case "vilagefcst":
			nowDate.setTime(new Date());
			urls = new HttpUrl.Builder().scheme("http")
					.host("apis.data.go.kr").addEncodedPathSegment("1360000")
					.addEncodedPathSegment("VilageFcstInfoService")
					.addEncodedPathSegment(getVilageFcstServie)
					.addEncodedQueryParameter("serviceKey", vilageFcstKey)
					.addEncodedQueryParameter("pageNo", "1")
					.addEncodedQueryParameter("numOfRows", "300")
					.addEncodedQueryParameter("dataType", "JSON")
					.addEncodedQueryParameter("base_date", date_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("base_time", time_sdf.format(nowDate.getTime()))
					.addEncodedQueryParameter("nx", Integer.toString(nx))
					.addEncodedQueryParameter("ny", Integer.toString(ny))
					.build();
			url = urls.toString();
			break;
		default:
			break;
		}
		
		return url;
	}
	

	
	/**
	 * @author taiseo
	 * @param jsonBody
	 * @return List<AsosVO> - 지상(종관, ASOS) 시간자료 조회 서비스
	 * @throws Exception
	 */
	private List<AsosVO> newAsosRecord(String jsonBody) throws Exception {
		List<AsosVO> list = new ArrayList<AsosVO>();
		Gson jsonParse = new Gson();
		
		JsonObject jsonObj = (JsonObject) jsonParse.fromJson(jsonBody, JsonObject.class);
		JsonObject response = (JsonObject) jsonObj.get("response");
		JsonObject header = (JsonObject) response.get("header");
		String resultCode= header.get("resultCode").getAsString();
		
		if(resultCode.equals("00")) {
			JsonObject body = (JsonObject) response.get("body");
			JsonObject items = (JsonObject) body.get("items");
			JsonArray item = (JsonArray) items.get("item");
			
			for(int i=0; i<item.size();  i++) {
				
				JsonObject itemObj = (JsonObject)item.get(i);
				
				Map<String, Object> map = new ObjectMapper().readValue(itemObj.toString(), Map.class);
				Iterator<String> keys = map.keySet().iterator();
				
				
				/*
				 * nullException 보정용
				 * 
				 * 통신 응답결과 value가 null인 경우 매핑시 에러가 발생하여 디폴트값 적용함.
				 * 자료형 문자열 -> 기본값: 공백
				 * 자료형 숫자 -> 기본값: 0
				 * 
				 */
				
				JSONObject jo = new JSONObject();
				while(keys.hasNext()) {
					
					String key = keys.next();
					String value = map.get(key).toString();
					
					if(value.length() == 0) {
						if(key == "clfmAbbrCd") {
							value = " ";
						} else {
							value = "0";
						}
					} 
					
					jo.put(key, value);
					
				};
				
//				System.out.println("itemObj : ["+itemObj+"]");
				
				Gson gson = new Gson();
				AsosVO asos = gson.fromJson(jo.toString(), AsosVO.class);
				
				list.add(asos);
				
			}
		}
		
		return list;
	}
	
	/**
	 * @author taiseo
	 * @param jsonBody
	 * @return List<UltraSrtVO> - 동네예보 - 초단기실황 조회 서비스
	 * @throws Exception
	 */
	private List<UltraSrtVO> newUltraSrtRecord(String jsonBody) throws Exception {
		List<UltraSrtVO> list = new ArrayList<UltraSrtVO>();
		Gson jsonParse = new Gson();
		
		JsonObject jsonObj = (JsonObject) jsonParse.fromJson(jsonBody, JsonObject.class);
		JsonObject response = (JsonObject) jsonObj.get("response");
		JsonObject header = (JsonObject) response.get("header");
		String resultCode= header.get("resultCode").getAsString();
		
		if(resultCode.equals("00")) {
			System.out.println("정상");
			JsonObject body = (JsonObject) response.get("body");
			JsonObject items = (JsonObject) body.get("items");
			JsonArray item = (JsonArray) items.get("item");
			for(int i=0; i<item.size();  i++) {
				JsonObject itemObj = (JsonObject)item.get(i);
				System.out.println("itemObj : ["+itemObj+"]");
				Gson gson = new Gson();
				UltraSrtNcstVO asos = gson.fromJson(itemObj.toString(), UltraSrtNcstVO.class);
				UltraSrtVO usVo = new UltraSrtVO();
				usVo.setNx(asos.getNx());
				usVo.setNy(asos.getNy());
				usVo.setBaseDateTime(asos.getBaseDate()+asos.getBaseTime()+"00");
				usVo.setCategory(asos.getCategory());
				usVo.setObsrValue(asos.getObsrValue());
				list.add(usVo);
			}
		}
		
		return list;
	}
	
	/**
	 * @author seongheum
	 * @param jsonBody
	 * @return List<UltraSrtVO> - 동네예보
	 * @throws Exception
	 */
	private List<VilageFcstVO> newVilageFcstRecord(String jsonBody) throws Exception {
		List<VilageFcstVO> list = new ArrayList<VilageFcstVO>();
		Gson jsonParse = new Gson();
		
		JsonObject jsonObj = (JsonObject) jsonParse.fromJson(jsonBody, JsonObject.class);
		JsonObject response = (JsonObject) jsonObj.get("response");
		JsonObject header = (JsonObject) response.get("header");
		String resultCode= header.get("resultCode").getAsString();
		
		if(resultCode.equals("00")) {
			System.out.println("정상");
			JsonObject body = (JsonObject) response.get("body");
			JsonObject items = (JsonObject) body.get("items");
			JsonArray item = (JsonArray) items.get("item");
			for(int i=0; i<item.size();  i++) {
				JsonObject itemObj = (JsonObject)item.get(i);
				System.out.println("itemObj : ["+itemObj+"]");
				Gson gson = new Gson();
				VilageNcstVO asos = gson.fromJson(itemObj.toString(), VilageNcstVO.class);
				VilageFcstVO usVo = new VilageFcstVO();
				usVo.setNx(asos.getNx());
				usVo.setNy(asos.getNy());
				usVo.setBaseDateTime(asos.getBaseDate()+asos.getBaseTime()+"00");
				usVo.setFcstDateTime(asos.getFcstDate()+asos.getFcstTime()+"00");
				usVo.setCategory(asos.getCategory());
				usVo.setFcstValue(asos.getFcstValue());
				list.add(usVo);
			}
		}
		
		return list;
	}
	
	/**
	 * @author taiseo
	 * @param jsonBody
	 * @return List<WntyNcstVO> - 지상(종관, ASOS) 기상관측자료 조회 서비스
	 * @throws Exception
	 */
	private List<WntyNcstVO> newWntyNcstRecord(String jsonBody) throws Exception {
		List<WntyNcstVO> list = new ArrayList<WntyNcstVO>();
		Gson jsonParse = new Gson();
		
		JsonObject jsonObj = (JsonObject) jsonParse.fromJson(jsonBody, JsonObject.class);
		JsonObject response = (JsonObject) jsonObj.get("response");
		JsonObject header = (JsonObject) response.get("header");
		String resultCode= header.get("resultCode").getAsString();
		
		if(resultCode.equals("00")) {
			System.out.println("정상");
			JsonObject body = (JsonObject) response.get("body");
			JsonObject items = (JsonObject) body.get("items");
			JsonArray item = (JsonArray) items.get("item");
			for(int i=0; i<item.size();  i++) {
				JsonObject itemObj = (JsonObject)item.get(i);
				System.out.println("itemObj : ["+itemObj+"]");
				Gson gson = new Gson();
				WntyNcstVO wntyNcstVo = gson.fromJson(itemObj.toString(), WntyNcstVO.class);
				list.add(wntyNcstVo);
			}
		}
		
		return list;
	}
}
