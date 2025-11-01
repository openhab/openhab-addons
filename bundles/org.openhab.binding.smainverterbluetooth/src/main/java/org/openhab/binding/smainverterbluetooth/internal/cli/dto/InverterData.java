package org.openhab.binding.smainverterbluetooth.internal.cli.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InverterData } is the internal class for OpenHAB inverter identity information
 * from a SMA Solar Inverter Bluetooth to CLI.
 * 
 * @author Lee Charlton - Initial contribution
 */

// {"code" : 0, "message" : "Success", "DATA" :
// {"daily" : 1888, "total" : 42555658, "watts" : 165, "temperature" : 27.62, "acvolts" : 241.02,
// "time" : "Fri, 31 Oct 2025 16:10:53 GMT Standard Time"}
@NonNullByDefault
public class InverterData {
    private int code = 8;
    private String message = "failed";
    private InverterDataValues data = new InverterDataValues();

    private class InverterDataValues {
        private int daily = 0;
        private long total = 0L;
        private int watts = 0;
        private double temperature = 0.0;
        private double acvolts = 0.0;
        private String time = "";
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getDaily() {
        return data.daily;
    }

    public long getTotal() {
        return data.total;
    }

    public int getSpotPower() {
        return data.watts;
    }

    public double getSpotTemperature() {
        return data.temperature;
    }

    public double getSpotACVolts() {
        return data.acvolts;
    }

    public String getTime() {
        return data.time;
    }
}
