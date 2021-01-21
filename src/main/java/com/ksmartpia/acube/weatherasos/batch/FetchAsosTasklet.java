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
import com.ksmartpia.acube.weatherasos.model.WntyNcstVO;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class FetchAsosTasklet {

	static Logger log = Logger.getLogger(FetchAsosTasklet.class.getName());
	
	
	// 誘쇨낵�옣�떂 �씤利앺궎 - 留뚮즺�맂�벏
//	public String asosServiceKey = "X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D";

	// �닔由� �씤利앺궎 
	public String asosServiceKey = "jGWp9%2FCQcmUmn%2FV6Nxwaa5lFeTGmtYNj2OQ4iHH%2BKD2UmFA9g%2BPhX4EhM6OzMlQXlRjErdtDt%2BEF0UvavkXhwg%3D%3D";
	
	public String ultraSrtKey = "tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D";
	public String wntyNcstKey = "tRZ7yirNM7N%2FD6ee8nHMn2RFzDYPtPvTs9UBMFe2f8Shc1%2Bsa3v5k6ZJ%2FNJnPM%2FdrwIgTVFoSObxehrnqmT%2FDw%3D%3D";
	public Calendar nowDate = Calendar.getInstance();
	public SimpleDateFormat date_sdf = new SimpleDateFormat("yyyyMMdd");
	public SimpleDateFormat time_sdf = new SimpleDateFormat("HHmm");
	public SimpleDateFormat datetime_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
	
	/** 怨듦났�뜲�씠�꽣 �룷�꽭 �꽌鍮꾩뒪紐� */
	public String getWthrDataListServie = "getWthrDataList"; //吏��긽(醫낃�, ASOS) �떆媛꾩옄猷� 議고쉶 �꽌鍮꾩뒪
	public String getUltraSrtNcstServie = "getUltraSrtNcst"; //�룞�꽕�삁蹂� - 珥덈떒湲곗떎�솴 議고쉶 �꽌鍮꾩뒪
	public String getWntyNcstServie = "getWntyNcst"; //吏��긽(醫낃�, ASOS) 湲곗긽愿�痢≪옄猷� 議고쉶 �꽌鍮꾩뒪
	
	@Inject
	public AsosDAO asosDao;

	/**
	 * ASOS 醫낃� 湲곗긽 �젙蹂�
	 * @author taiseo
	 * @throws Exception
	 * 
	 * ��援ш텒 湲곗긽�젙蹂대�� �닔吏묓븯�뒗 API
	 * �떦�씪 �젙蹂대뒗 媛쒖씤�씠 �궗�슜遺덇�, �쟾�궇 �뜲�씠�꽣瑜� 1�떆媛� �떒�쐞濡� ���옣�븿. (00�떆~ 23�떆)
	 * 
	 */
	@Scheduled(cron=" 0 0 3 * * *")
	public void getAsosInfo() throws Exception {
		System.out.println("ASOS 醫낃� 湲곗긽 �젙蹂� �떆�옉");
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
				
				// �넻�떊 �꽦怨듭떆 vo�뿉 �젙蹂� 留ㅽ븨�븿
				list = newAsosRecord(body);
				
				if(list.size() > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("list", list);
					int insertCnt = this.asosDao.setASOSInfo(map);
					System.out.println("ASOS 醫낃� 湲곗긽 �젙蹂� Insert CNT : ["+insertCnt+"]");
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
	 * 珥덈떒湲곗떎�솴 議고쉶
	 * @author taiseo
	 * @throws Exception
	 * 
	 * 援ъ뿭蹂꾨줈 湲곗긽�젙蹂대�� �닔吏묓븯�뒗 API
	 * 湲곗긽泥� X,Y 醫뚰몴蹂꾨줈 援ъ뿭 �굹�닎 - �뻾�젙�룞 湲곗�
	 * �떦�씪 �젙蹂� �궗�슜 媛��뒫.
	 * 
	 */
	@Scheduled(cron="0 30 * * * *")
	public void ultraSrtInfoGet() throws Exception {
		System.out.println("珥덈떒湲� �떎�솴 �젙蹂� �떆�옉");
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
						System.out.println("珥덈떒湲� �떎�솴 �젙蹂� Insert CNT : ["+insertCnt+"]");
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
	
	/** 吏��긽(醫낃�, ASOS) 湲곗긽愿�痢≪옄猷� 議고쉶 �꽌鍮꾩뒪
	 * @author taiseo
	 * @throws Exception
	 */
	//@Scheduled(cron="0 0/1 * * * *")
	public void setWntyNcstInfo() throws Exception {
		System.out.println("吏��긽(醫낃�, ASOS) 湲곗긽愿�痢≪옄猷� �젙蹂� �떆�옉");
		List<Map<String, Object>> gridList = this.asosDao.getGridInfo();
		try {
			OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS)
					.readTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).build();
			for(Map<String, Object> gridMap : gridList) {
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
						int insertCnt = this.asosDao.newWntyNcstRecord(map);
						System.out.println("吏��긽(醫낃�, ASOS) 湲곗긽愿�痢≪옄猷� �젙蹂� Insert CNT : ["+insertCnt+"]");
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
		default:
			break;
		}
		
		return url;
	}
	

	
	/**
	 * @author taiseo
	 * @param jsonBody
	 * @return List<AsosVO> - 吏��긽(醫낃�, ASOS) �떆媛꾩옄猷� 議고쉶 �꽌鍮꾩뒪
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
				 * nullException 蹂댁젙�슜
				 * 
				 * �넻�떊 �쓳�떟寃곌낵 value媛� null�씤 寃쎌슦 留ㅽ븨�떆 �뿉�윭媛� 諛쒖깮�븯�뿬 �뵒�뤃�듃媛� �쟻�슜�븿.
				 * �옄猷뚰삎 臾몄옄�뿴 -> 湲곕낯媛�: 怨듬갚
				 * �옄猷뚰삎 �닽�옄 -> 湲곕낯媛�: 0
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
	 * @return List<UltraSrtVO> - �룞�꽕�삁蹂� - 珥덈떒湲곗떎�솴 議고쉶 �꽌鍮꾩뒪
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
			System.out.println("�젙�긽");
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
	 * @param jsonBody
	 * @return List<WntyNcstVO> - 吏��긽(醫낃�, ASOS) 湲곗긽愿�痢≪옄猷� 議고쉶 �꽌鍮꾩뒪
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
			System.out.println("�젙�긽");
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
