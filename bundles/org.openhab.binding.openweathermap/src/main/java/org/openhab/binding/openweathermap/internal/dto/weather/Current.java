package org.openhab.binding.openweathermap.internal.dto.weather;

import java.util.List;

import org.openhab.binding.openweathermap.internal.dto.base.Rain;
import org.openhab.binding.openweathermap.internal.dto.base.Snow;
import org.openhab.binding.openweathermap.internal.dto.base.Weather;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for the current weather block in the JSON response on the OneCallAPI
 *
 *
 * @author Michael Hazelden - Initial contribution
 *
 */
public class Current {

    private Integer dt;
    private Integer sunrise;
    private Integer sunset;
    private double temp;
    @SerializedName("feels_like")
    private double feelslike;
    private Integer pressure;
    private Integer humidity;
    @SerializedName("dew_point")
    private double dewpoint;
    private double uvi;
    private Integer clouds;
    private Integer visibility;
    @SerializedName("wind_speed")
    private double windspeed;
    @SerializedName("wind_deg")
    private Integer winddeg;
    @SerializedName("wind_gust")
    private double windgust;
    private List<Weather> weather;
    private Rain rain;
    private Snow snow;

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

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsLike() {
        return feelslike;
    }

    public void setFeelsLike(double feelslike) {
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

    public double getUvi() {
        return uvi;
    }

    public void setUvi(double uvi) {
        this.uvi = uvi;
    }

    public Integer getClouds() {
        return clouds;
    }

    public void setClouds(Integer clouds) {
        this.clouds = clouds;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
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

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;

    }

    public double getWindGust() {
        return windgust;
    }

    public void setWindGust(double windgust) {
        this.windgust = windgust;
    }

}
