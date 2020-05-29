package com.ksmartpia.acube.weatherasos.batch;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ksmartpia.acube.weatherasos.model.AsosVO;

@Component
public class AsosDAO {
	
	static Logger log = Logger.getLogger(AsosDAO.class.getName());
	@Inject
	@Qualifier("sqlSession")
	private SqlSession sqlSession;
	private String mapper = "com.ksmartpia.acube.weatherasos.model.mapper.AsosVO";
	
	public void setASOSInfo(Map<String, Object> map) {
		// TODO Auto-generated method stub
		sqlSession.insert(mapper+".asosInsert", map);
	}
}
