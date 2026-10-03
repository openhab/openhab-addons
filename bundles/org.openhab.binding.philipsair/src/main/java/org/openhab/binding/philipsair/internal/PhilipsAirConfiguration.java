/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipsair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PhilipsAirConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Michal Boronski - Initial contribution
 * @author Marcel Verpaalen - Add configuration properties and timeout handling
 */
@NonNullByDefault
public class PhilipsAirConfiguration {

    public static final String CONFIG_KEY = "key";
    public static final String CONFIG_DEF_DEVICE_UUID = "deviceUUID";
    public static final String CONFIG_DEF_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_DEF_HUMIDITY_OFFSET = "humidityOffset";
    public static final String CONFIG_DEF_TEMPERATURE_OFFSET = "temperatureOffset";

    /**
     * Hostname or IP address of Air Purifier device
     */
    public static final String CONFIG_HOST = "host";

    public static final int MIN_REFRESH_INTERVAL = 5;

    /**
     * Data retrieval rate from the device
     */
    private int refreshInterval = 60;

    private String host = "";
    private String deviceUUID = "";
    private String key = "";
    private float temperatureOffset;
    private float humidityOffset;
    private String deviceProfile = PhilipsAirBindingConstants.PROFILE_AUTO;

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public float getTemperatureOffset() {
        return temperatureOffset;
    }

    public void setTemperatureOffset(float temperatureOffset) {
        this.temperatureOffset = temperatureOffset;
    }

    public float getHumidityOffset() {
        return humidityOffset;
    }

    public void setHumidityOffset(float humidityOffset) {
        this.humidityOffset = humidityOffset;
    }

    /**
     * @return the profile of the model of a CoAP device, or {@code auto} to detect it
     */
    public String getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(String deviceProfile) {
        this.deviceProfile = deviceProfile;
    }
}
