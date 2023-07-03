package org.openhab.binding.toyota.internal.dto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class Vehicle {
    public static Type LIST_CLASS = new TypeToken<List<Vehicle>>() {
    }.getType();

    public boolean isEntitled;
    public boolean hasAutomaticMMRegistration;
    public int vehicleId;
    public String vin;
    public String modelName;
    public String modelCode;
    public String productionYear;
    public String imageUrl;
    public String smallImageUrl;
    public String alias;
    public String licensePlate;
    public String exteriorColour;
    public String transmission;
    public String transmissionType;
    public String engine;
    public String fuel;
    public double horsePower;
    public boolean hybrid;
    public boolean owner;
    public String source;
    public boolean isNc;
    public ArrayList<Device> devices;
    public String deliveryCountry;
    public String productionDate;
    public ArrayList<String> features;
    public boolean isOneAppMigrated;
}
