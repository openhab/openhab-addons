package org.openhab.binding.ems.dto;

import java.util.List;

public class OnecallWeather {
    public double lat; // ": 51.44,
    public double lon; // ": -10,
    public String timezone; // ": "Europe/Dublin",
    public int timezone_offset; // ": 3600,
    public WeatherCurrent current;
    public List<HourForecast> hourly;
    public List<DailyForecast> daily;
}
