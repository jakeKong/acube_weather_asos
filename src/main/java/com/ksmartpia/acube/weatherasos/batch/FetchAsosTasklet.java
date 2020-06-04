package com.ksmartpia.acube.weatherasos.batch;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ksmartpia.acube.weatherasos.model.AsosVO;
import com.ksmartpia.acube.weatherasos.model.UltraSrtNcstVO;
import com.ksmartpia.acube.weatherasos.model.UltraSrtVO;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class FetchAsosTasklet {

	static Logger log = Logger.getLogger(FetchAsosTasklet.class.getName());
	
	public String asosServiceKey = "X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D";
	public String UltraSrtKey = "";
	public Calendar nowDate = Calendar.getInstance();
	public SimpleDateFormat date_sdf = new SimpleDateFormat("yyyyMMdd");
	public SimpleDateFormat time_sdf = new SimpleDateFormat("HHmm");
	public SimpleDateFormat datetime_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
	
	@Inject
	public AsosDAO asosDao;

	/**
	 * ASOS 종관 기상 정보
	 * @author taiseo
	 * @throws Exception
	 */
	@Scheduled(cron="0 0/60 * * * *")
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
						System.out.println("초단기 실황 정보 Insert 시작");
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("list", list);
						int insertCnt = this.asosDao.setUltraSrtNcstInfo(map);
						System.out.println("초단기 실황 정보 Insert CNT : ["+insertCnt+"]");
						System.out.println("초단기 실황 정보 Insert 종료");
						//System.out.println("list.size : ["+list.size()+"]");
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
				System.out.println("itemObj : ["+itemObj+"]");
				Gson gson = new Gson();
				AsosVO asos = gson.fromJson(itemObj.toString(), AsosVO.class);
				
				list.add(asos);
			}
		}
		
		return list;
	}
	
	/**
	 * @param jsonBody
	 * @return List<UltraSrtVO>
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
	 * @author taiseo
	 * @param categori
	 * @return api url
	 */
	public String getApiUrl(String categori, int nx, int ny) {

		String url = "";
		
		switch (categori) {
		case "asos":
			nowDate.setTime(new Date());
			nowDate.add(Calendar.DATE, -1);
			url = "http://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList?serviceKey=X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D&pageNo=1&numOfRows=30&dataType=JSON&dataCd=ASOS&dateCd=HR"
					+ "&startDt="+date_sdf.format(nowDate.getTime())+"&startHh=00&endDt="+date_sdf.format(nowDate.getTime())+"&endHh=23&stnIds=143&schListCnt=10";
			break;
		
		case "ultrancst":
			nowDate.setTime(new Date());
			url = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst?serviceKey=tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D"
					+ "&pageNo=1&numOfRows=300&dataType=JSON"
					+ "&base_date="+date_sdf.format(nowDate.getTime())
					+ "&base_time="+time_sdf.format(nowDate.getTime())
					+ "&nx="+nx
					+ "&ny="+ny;
			break;

		default:
			break;
		}
		
		return url;
	}
}
