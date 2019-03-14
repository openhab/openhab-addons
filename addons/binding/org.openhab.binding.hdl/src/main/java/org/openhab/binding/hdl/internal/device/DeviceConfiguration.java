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
package org.openhab.binding.hdl.internal.device;

import java.util.HashMap;
import java.util.Map;

/**
 * The DeviceConfiguration class set all the configurations for HDL Devices
 *
 * @author stigla - Initial contribution
 */
public final class DeviceConfiguration {

    private String serialNr = null;
    private int subNet = -1;
    private int deviceID = -1;
    private DeviceType deviceType = null;

    /** Extended configuration properties **/
    private HashMap<String, Object> properties = new HashMap<>();

    private DeviceConfiguration() {
    }

    public static DeviceConfiguration create(String SerialNr, int subNet, int deviceID, DeviceType deviceType) {
        DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues(SerialNr, subNet, deviceID, deviceType);

        return configuration;
    }

    public static DeviceConfiguration create(DeviceInformation di) {
        DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues(di.getSerialNr(), di.getsubNet(), di.getdeviceID(), di.getDeviceType());
        return configuration;
    }

    private void setValues(String SerialNr, int subNet, int deviceID, DeviceType deviceType) {
        setValues(subNet, deviceID, deviceType);
        this.serialNr = SerialNr;
    }

    private void setValues(int subNet, int deviceID, DeviceType deviceType) {
        this.subNet = subNet;
        this.deviceID = deviceID;
        this.deviceType = deviceType;
    }

    public int getsubNet() {
        return subNet;
    }

    public int getdeviceID() {
        return deviceID;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getSerialNr() {
        if (serialNr == null) {
            return "";
        } else {
            return serialNr;
        }
    }

    public void setSerialNr(String SerialNr) {
        this.serialNr = SerialNr;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

}
