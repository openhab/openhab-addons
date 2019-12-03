/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PhilipsAirConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Michal Boronski - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirConfiguration {

    public static final String CONFIG_KEY = "key";
    public static final String CONFIG_DEF_DEVICE_UUID = "deviceUUID";
    public static final String CONFIG_DEF_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_DEF_MODEL_ID = "modelid";

    /**
     * Hostname or IP address of Air Purifier device
     */
    public static final String CONFIG_HOST = "host";

    public static final int MIN_REFESH_INTERVAL = 5;

    /**
     * Data retrieval rate from the device
     */
    private int refreshInterval = 60;

    private String host = "";
    private String deviceUUID = "";
    private String key = "";
    private String modelid = "";

    public void updateFromProperties(Map<String, Object> properties) {
        Validate.notNull(properties);

        for (Map.Entry<String, Object> e : properties.entrySet()) {
            switch (e.getKey()) {
            case CONFIG_KEY:
                setKey((String) e.getValue());
                break;
            case CONFIG_HOST:
                setHost((String) e.getValue());
                break;
            case CONFIG_DEF_REFRESH_INTERVAL:
                setRefreshInterval((Integer) e.getValue());
                break;
            case CONFIG_DEF_DEVICE_UUID:
                setDeviceUUID((String) e.getValue());
                break;
            case CONFIG_DEF_MODEL_ID:
                setModelid((String) e.getValue());
                break;
            }

        }
    }

    public void updateFromProperties(Dictionary<String, Object> properties) {
        Validate.notNull(properties);
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        updateFromProperties(dictCopy);
    }

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

    public String getModelid() {
        return modelid;
    }

    public void setModelid(String modelid) {
        this.modelid = modelid;
    }
}
