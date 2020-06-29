package com.ksmartpia.acube.weatherasos.batch;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AsosDAO {
	
	static Logger log = Logger.getLogger(AsosDAO.class.getName());
	@Inject
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	private String mapper = "com.ksmartpia.acube.weatherasos.model.mapper.AsosVO";
	
	public int setASOSInfo(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return this.sqlSession.insert(this.mapper+".asosInsert", map);
	}

	public List<Map<String, Object>> getGridInfo() {
		// TODO Auto-generated method stub
		return this.sqlSession.selectList(this.mapper+".gridInfo");
	}
	
	public int setUltraSrtNcstInfo(Map<String, Object> map) {
		return this.sqlSession.insert(this.mapper+".setUltraSrtNcstInfo", map);
	}

	public int newWntyNcstRecord(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return this.sqlSession.insert(this.mapper+".setWntyNcstInfo", map);
	}
}
