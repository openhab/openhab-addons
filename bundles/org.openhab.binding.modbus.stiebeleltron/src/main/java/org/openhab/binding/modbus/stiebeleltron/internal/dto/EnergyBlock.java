package org.openhab.binding.modbus.stiebeleltron.internal.dto;

public class EnergyBlock {

    public int production_heat_today;
    public int production_heat_total_low;
    public int production_heat_total_high;
    public int production_water_today;
    public int production_water_total_low;
    public int production_water_total_high;

    public int consumption_heat_today;
    public int consumption_heat_total_low;
    public int consumption_heat_total_high;
    public int consumption_water_today;
    public int consumption_water_total_low;
    public int consumption_water_total_high;
}