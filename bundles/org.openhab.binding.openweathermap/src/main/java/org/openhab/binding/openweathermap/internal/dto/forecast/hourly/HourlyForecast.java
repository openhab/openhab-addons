package org.openhab.binding.openweathermap.internal.dto.forecast.hourly;

import java.util.List;

import org.openhab.binding.openweathermap.internal.dto.base.Rain;
import org.openhab.binding.openweathermap.internal.dto.base.Snow;
import org.openhab.binding.openweathermap.internal.dto.base.Weather;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for the Hourly Forecast object in the JSON response on the OneCallAPI
 * 
 * * @author Michael Hazelden - Initial contribution
 *
 */
public class HourlyForecast {

    private Integer dt;
    private double temp;
    @SerializedName("feels_like")
    private double feelslike;
    private Integer pressure;
    private Integer humidity;
    @SerializedName("dew_point")
    private double dewpoint;
    private Integer clouds;
    @SerializedName("wind_speed")
    private double windspeed;
    @SerializedName("wind_deg")
    private Integer winddeg;
    private List<Weather> weather;
    private Rain rain;
    private Snow snow;
    private double gust;
    private Integer visibility;
    private double pop;

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsLike() {
        return feelslike;
    }

    public void setFeels_like(double feelslike) {
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

    public Integer getClouds() {
        return clouds;
    }

    public void setClouds(Integer clouds) {
        this.clouds = clouds;
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

    public void setWinddeg(Integer winddeg) {
        this.winddeg = winddeg;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public double getGust() {
        return gust;
    }

    public void setGust(double gust) {
        this.gust = gust;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

}
