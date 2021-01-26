package com.ksmartpia.acube.weatherasos.model;

public class VilageNcstVO {
	private int nx;
	private int ny;
	private String baseDate;
	private String baseTime;
	private String fcstDate;
	private String fcstTime;
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
	public String getBaseDate() {
		return baseDate;
	}
	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}
	public String getBaseTime() {
		return baseTime;
	}
	public void setBaseTime(String baseTime) {
		this.baseTime = baseTime;
	}
	public String getFcstDate() {
		return fcstDate;
	}
	public void setFcstDate(String fcstDate) {
		this.fcstDate = fcstDate;
	}
	public String getFcstTime() {
		return fcstTime;
	}
	public void setFcstTime(String fcstTime) {
		this.fcstTime = fcstTime;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public double getFcstValue() {
		return fcstValue;
	}
	public void setFcstValue(double fcstValue) {
		this.fcstValue = fcstValue;
	}
}
