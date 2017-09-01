package org.openhab.binding.robonect.model;

/**
 * Health information from the mower. This information is just included if the robonect module runs the firmware
 * 1.0 beta or higher.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class Health {
    
    private int temperature;
    
    private int humidity;

    /**
     * @return - the temperature in °C measured in the mower.
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * @return - the humidity in % measured in the mower.
     */
    public int getHumidity() {
        return humidity;
    }
}
