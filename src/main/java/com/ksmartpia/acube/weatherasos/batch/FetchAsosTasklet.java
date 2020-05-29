package com.ksmartpia.acube.weatherasos.batch;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class FetchAsosTasklet {

	static Logger log = Logger.getLogger(FetchAsosTasklet.class.getName());
	
	public String asosServiceKey = "X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D";
	public Calendar nowDate = Calendar.getInstance();
	public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Inject
	public AsosDAO asosDao;

	@Scheduled(cron="0 0/60 * * * *")
	public void getAsosInfo() throws Exception {
		
		OkHttpClient client = new OkHttpClient();
		List<AsosVO> list = new ArrayList<AsosVO>();
		
		try {
			String url = getApiUrl("asos");

			Request request = new Request.Builder().url(url)
					.get()
					.build();
			
			Response response = client.newCall(request).execute();
			
			if (response.isSuccessful()) {

				String body = response.body().string();
				response.body().close();
				
				list = newAsosRecord(body);
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("list", list);
				this.asosDao.setASOSInfo(map);
				
			} else {
				throw new IOException("Unexpected code " + response);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<AsosVO> newAsosRecord(String jsonBody)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, DOMException,
			ParseException {
		List<AsosVO> list = new ArrayList<AsosVO>();
		Gson jsonParse = new Gson();
		
		JsonObject jsonObj = (JsonObject) jsonParse.fromJson(jsonBody, JsonObject.class);
		JsonObject response = (JsonObject) jsonObj.get("response");
		JsonObject header = (JsonObject) response.get("header");
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
		
		return list;
	}
	
	/**
	 * @author taiseo
	 * @param categori
	 * @return api url
	 */
	public String getApiUrl(String categori) {

		String url = "";
		
		switch (categori) {
		case "asos":
			nowDate.setTime(new Date());
			nowDate.add(Calendar.DATE, -1);
			url = "http://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList?serviceKey=X8cs2LiGddyXvoc895CiVkrx14Doa7kQV6HUNLfZK0LQnDtec1WHC5PLw8OZKXRMUKG9TfOWektVjhD4TVEaXg%3D%3D&pageNo=1&numOfRows=30&dataType=JSON&dataCd=ASOS&dateCd=HR"
					+ "&startDt="+sdf.format(nowDate.getTime())+"&startHh=00&endDt="+sdf.format(nowDate.getTime())+"&endHh=23&stnIds=143&schListCnt=10";
			break;

		default:
			break;
		}
		
		return url;
	}
}
