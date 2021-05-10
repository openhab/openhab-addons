package org.openhab.binding.ems.dto;

import java.util.List;

public class WeatherCurrent {
    public int dt; // ": 1620509255,
    public int sunrisedt; // ": 1620449957,
    public int sunsetdt; // ": 1620504821,
    public double tempdt; // ": 284.43,
    public double feels_likedt; // ": 283.73,
    public int pressuredt; // ": 987,
    public int humiditydt; // ": 81,
    public double dew_pointdt; // ": 281.29,
    public int uvidt; // ": 0,
    public int cloudsdt; // ": 65,
    public int visibilitydt; // ": 10000,
    public double wind_speeddt; // ": 13.78,
    public int wind_degdt; // ": 190,
    public double wind_gustdt; // ": 17.82,
    public List<WeatherDescription> weather;
}
