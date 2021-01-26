package com.ksmartpia.acube.weatherasos.model;

public class VilageFcstVO {
	private int nx;
	private int ny;
	private String baseDateTime;
	private String fcstDateTime;
	private String category;
	private double fcstValue;
	
	
	public int getNx() {
		return nx;
	}
	public void setNx(int nx) {
		this.nx = nx;
	}
	public int getNy() {
		return ny;
	}
	public void setNy(int ny) {
		this.ny = ny;
	}
	public String getBaseDateTime() {
		return baseDateTime;
	}
	public void setBaseDateTime(String baseDateTime) {
		this.baseDateTime = baseDateTime;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getFcstDateTime() {
		return fcstDateTime;
	}
	public void setFcstDateTime(String fcstDateTime) {
		this.fcstDateTime = fcstDateTime;
	}
	public double getFcstValue() {
		return fcstValue;
	}
	public void setFcstValue(double fcstValue) {
		this.fcstValue = fcstValue;
	}
}
