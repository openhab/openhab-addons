package org.openhab.binding.vera2.controller.Vera.json;

import java.util.List;

public class Sdata {
    public String full;
    public String version;
    public String model;
    public String zwave_heal;
    public String temperature;
    public String serial_number;
    public String fwd1;
    public String fwd2;
    public String ir;
    public String irtx;
    public String loadtime;
    public String dataversion;
    public String state;
    public String comment;
    public List<Section> sections;
    public List<Room> rooms;
    public List<Scene> scenes;
    public List<Device> devices;
    public List<Categorie> categories;
}