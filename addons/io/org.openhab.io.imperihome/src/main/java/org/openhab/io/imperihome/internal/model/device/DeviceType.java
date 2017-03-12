/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

/**
 * Device type enumeration. Contains ImperiHome API type strings.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public enum DeviceType {

    SWITCH("DevSwitch"),
    DIMMER("DevDimmer"),
    CAMERA("DevCamera"),
    CO2("DevCO2"),
    CO2_ALERT("DevCO2Alert"),
    DOOR("DevDoor"),
    ELECTRICITY("DevElectricity"),
    FLOOD("DevFlood"),
    GENERIC_SENSOR("DevGenericSensor"),
    HYGROMETRY("DevHygrometry"),
    LOCK("DevLock"),
    LUMINOSITY("DevLuminosity"),
    MOTION("DevMotion"),
    MULTI_SWITCH("DevMultiSwitch"),
    NOISE("DevNoise"),
    PLAYER("DevPlayer"),
    PLAYLIST("DevPlaylist"),
    PRESSURE("DevPressure"),
    RAIN("DevRain"),
    RGB_LIGHT("DevRGBLight"),
    SCENE("DevScene"),
    SHUTTER("DevShutter"),
    SMOKE("DevSmoke"),
    TEMPERATURE("DevTemperature"),
    TEMP_HYGRO("DevTempHygro"),
    THERMOSTAT("DevThermostat"),
    UV("DevUV"),
    WIND("DevWind");

    private final String apiString;

    DeviceType(String apiString) {
        this.apiString = apiString;
    }

    public String getApiString() {
        return apiString;
    }

    public static DeviceType forApiString(String apiString) {
        for (DeviceType deviceType : values()) {
            if (deviceType.getApiString().equalsIgnoreCase(apiString.trim())) {
                return deviceType;
            }
        }

        return null;
    }

}
