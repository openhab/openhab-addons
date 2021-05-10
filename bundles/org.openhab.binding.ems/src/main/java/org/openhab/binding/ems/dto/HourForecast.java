package org.openhab.binding.ems.dto;

import java.util.List;

public class HourForecast {
    public int dt; // ":1620507600,
    public double temp; // ":284.43,
    public double feels_like; // ":283.73,
    public int pressure; // ":987,
    public int humidity; // ":81,
    public double dew_point; // ":281.29,
    public double uvi; // ":0,
    public int clouds; // ":65,
    public int visibility; // ":10000,
    public double wind_speed; // ":13.78,
    public int wind_deg; // ":190,
    public double wind_gust; // ":17.82,
    public List<WeatherDescription> weather; // ":[{"id":500,"main":"Rain","description":"light rain","icon":"10n"}],
    public double pop; // ":0.68,
    public HourRain rain; // ":{"1h":0.23}
}
