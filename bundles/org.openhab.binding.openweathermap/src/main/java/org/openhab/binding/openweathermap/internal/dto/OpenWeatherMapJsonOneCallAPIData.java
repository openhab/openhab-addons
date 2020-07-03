package org.openhab.binding.openweathermap.internal.dto;

import java.util.List;

import org.openhab.binding.openweathermap.internal.dto.forecast.daily.DailyForecast;
import org.openhab.binding.openweathermap.internal.dto.forecast.hourly.HourlyForecast;
import org.openhab.binding.openweathermap.internal.dto.weather.Current;

/**
 * The {@link OpenWeatherMapJsonOneCallAPIData} is the Java class used to map the JSON response to an OpenWeatherMap
 * request.
 *
 * @author MPH80 - Initial contribution
 *
 */
public class OpenWeatherMapJsonOneCallAPIData {

    private double lat;
    private double lon;
    private String timezone;
    private String timezone_offset;
    private Current current;
    private List<HourlyForecast> hourly;
    private List<DailyForecast> daily;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone_offset() {
        return timezone_offset;
    }

    public void setTimezone_offset(String timezone_offset) {
        this.timezone_offset = timezone_offset;
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public List<HourlyForecast> getHourly() {
        return hourly;
    }

    public void setHourly(List<HourlyForecast> hourly) {
        this.hourly = hourly;
    }

    public List<DailyForecast> getDaily() {
        return daily;
    }

    public void setDaily(List<DailyForecast> daily) {
        this.daily = daily;
    }

}
