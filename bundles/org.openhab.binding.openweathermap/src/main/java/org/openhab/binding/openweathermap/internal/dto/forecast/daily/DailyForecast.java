package org.openhab.binding.openweathermap.internal.dto.forecast.daily;

import java.util.List;

import org.openhab.binding.openweathermap.internal.dto.base.Weather;

import com.google.gson.annotations.SerializedName;

public class DailyForecast {

    private Integer dt;
    private Integer sunrise;
    private Integer sunset;
    private Temp temp;
    @SerializedName("feels_like")
    private FeelsLikeTemp feelslike;
    private Integer pressure;
    private Integer humidity;
    @SerializedName("dew_point")
    private double dewpoint;
    @SerializedName("wind_speed")
    private double windspeed;
    @SerializedName("wind_deg")
    private Integer winddeg;
    private List<Weather> weather;
    private Integer clouds;
    private double rain;
    private double snow;
    private double uvi;

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Integer getSunrise() {
        return sunrise;
    }

    public void setSunrise(Integer sunrise) {
        this.sunrise = sunrise;
    }

    public Integer getSunset() {
        return sunset;
    }

    public void setSunset(Integer sunset) {
        this.sunset = sunset;
    }

    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

    public FeelsLikeTemp getFeelsLike() {
        return feelslike;
    }

    public void setFeelsLike(FeelsLikeTemp feelslike) {
        this.feelslike = feelslike;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public double getDewPoint() {
        return dewpoint;
    }

    public void setDewPoint(double dewpoint) {
        this.dewpoint = dewpoint;
    }

    public double getWindSpeed() {
        return windspeed;
    }

    public void setWindSpeed(double windspeed) {
        this.windspeed = windspeed;
    }

    public Integer getWindDeg() {
        return winddeg;
    }

    public void setWindDeg(Integer winddeg) {
        this.winddeg = winddeg;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Integer getClouds() {
        return clouds;
    }

    public void setClouds(Integer clouds) {
        this.clouds = clouds;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    public double getUvi() {
        return uvi;
    }

    public void setUvi(double uvi) {
        this.uvi = uvi;
    }

    public double getSnow() {
        return snow;
    }

    public void setSnow(double snow) {
        this.snow = snow;
    }

}
