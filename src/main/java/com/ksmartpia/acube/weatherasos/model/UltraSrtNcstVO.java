package com.ksmartpia.acube.weatherasos.model;

public class UltraSrtNcstVO {

	private int nx;
	private int ny;
	private String baseDate;
	private String baseTime;
	private String category;
	private double obsrValue;
	
	
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
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public double getObsrValue() {
		return obsrValue;
	}
	public void setObsrValue(double obsrValue) {
		this.obsrValue = obsrValue;
	}
}
