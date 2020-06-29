package com.ksmartpia.acube.weatherasos.model;

public class WntyNcstVO {

	private String stnId;
	private String tm;
	private String weatherStatusNumber;
	private String visibility;
	private String cloudAmount;
	private String cloudAmountMid;
	private String temperature;
	private String dewPointTemperature;
	private String rainfallDay;
	private String newSnowDay;
	private String humidity;
	private String windDirection;
	private String windSpeed;
	private String sealevelPressure;
	private String tamax;
	private String tamin;
	
	
	public String getStnId() {
		return stnId;
	}
	public void setStnId(String stnId) {
		this.stnId = stnId;
	}
	public String getTm() {
		return tm;
	}
	public void setTm(String tm) {
		this.tm = tm;
	}
	public String getWeatherStatusNumber() {
		return weatherStatusNumber;
	}
	public void setWeatherStatusNumber(String weatherStatusNumber) {
		this.weatherStatusNumber = weatherStatusNumber;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public String getCloudAmount() {
		return cloudAmount;
	}
	public void setCloudAmount(String cloudAmount) {
		this.cloudAmount = cloudAmount;
	}
	public String getCloudAmountMid() {
		return cloudAmountMid;
	}
	public void setCloudAmountMid(String cloudAmountMid) {
		this.cloudAmountMid = cloudAmountMid;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getDewPointTemperature() {
		return dewPointTemperature;
	}
	public void setDewPointTemperature(String dewPointTemperature) {
		this.dewPointTemperature = dewPointTemperature;
	}
	public String getRainfallDay() {
		return rainfallDay;
	}
	public void setRainfallDay(String rainfallDay) {
		this.rainfallDay = rainfallDay;
	}
	public String getNewSnowDay() {
		return newSnowDay;
	}
	public void setNewSnowDay(String newSnowDay) {
		this.newSnowDay = newSnowDay;
	}
	public String getHumidity() {
		return humidity;
	}
	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
	public String getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(String windDirection) {
		this.windDirection = convertToDirection(windDirection);
	}
	public String getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}
	public String getSealevelPressure() {
		return sealevelPressure;
	}
	public void setSealevelPressure(String sealevelPressure) {
		this.sealevelPressure = sealevelPressure;
	}
	public String getTamax() {
		return tamax;
	}
	public void setTamax(String tamax) {
		this.tamax = tamax;
	}
	public String getTamin() {
		return tamin;
	}
	public void setTamin(String tamin) {
		this.tamin = tamin;
	}
	
	public String convertToDirection(String windion) {
		switch (windion) {
		case "0":
			windion = "N";
			break;
		case "1":
			windion = "NNE";
			break;
		case "2":
			windion = "NE";
			break;
		case "3":
			windion = "ENE";
			break;
		case "4":
			windion = "E";
			break;
		case "5":
			windion = "ESE";
			break;
		case "6":
			windion = "SE";
			break;
		case "7":
			windion = "SSE";
			break;
		case "8":
			windion = "S";
			break;
		case "9":
			windion = "SSW";
			break;
		case "10":
			windion = "SW";
			break;
		case "11":
			windion = "WSW";
			break;
		case "12":
			windion = "W";
			break;
		case "13":
			windion = "WNW";
			break;
		case "14":
			windion = "NW";
			break;
		case "15":
			windion = "NNW";
			break;
		case "16":
			windion = "N";
			break;
		default:
			break;
		}
		
		return windion;
	}
}
