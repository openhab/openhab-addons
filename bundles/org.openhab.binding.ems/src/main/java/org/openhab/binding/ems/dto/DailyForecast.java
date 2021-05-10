package org.openhab.binding.ems.dto;

import java.util.List;

public class DailyForecast {
    public int dt; // ":1620475200,
    public int sunrise; // ":1620449957,
    public int sunset; // ":1620504821,
    public int moonrise; // ":1620447540,
    public int moonset; // ":1620492060,
    public double moon_phase; // ":0.9,
    public DayTemperature temp; // ":{"day":284.62,"min":283.83,"max":285.33,"night":284.4,"eve":284.54,"morn":284.62},
    public DayTemperature feels_like; // ":{"day":284.07,"night":284.25,"eve":283.88,"morn":284.25},
    public int pressure; // ":993,
    public int humidity; // ":86,
    public double dew_point; // ":282.44,
    public double wind_speed; // ":14.63,
    public int wind_deg; // ":120,
    public double wind_gust; // ":19.66,
    public List<WeatherDescription> weather; // ":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],
    public int clouds; // ":25,
    public double pop; // ":1,
    public double rain; // ":11.17,
    public double uvi; // ":6.25
}
