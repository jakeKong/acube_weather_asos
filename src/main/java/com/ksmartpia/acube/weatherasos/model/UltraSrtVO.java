package com.ksmartpia.acube.weatherasos.model;

public class UltraSrtVO {

	private int nx;
	private int ny;
	private String baseDateTime;
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
	public double getObsrValue() {
		return obsrValue;
	}
	public void setObsrValue(double obsrValue) {
		this.obsrValue = obsrValue;
	}
}
