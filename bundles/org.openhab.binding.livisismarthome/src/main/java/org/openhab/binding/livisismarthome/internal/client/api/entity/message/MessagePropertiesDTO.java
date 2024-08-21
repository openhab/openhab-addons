/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.api.entity.message;

/**
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class MessagePropertiesDTO {

    /**
     * Name of the referenced {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}
     */
    private String deviceName;

    /**
     * Serialnumber of the referenced
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}
     */
    private String serialNumber;

    /**
     * Locationname of the referenced
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}
     */
    private String locationName;

    /**
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return the locationName
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * @param locationName the locationName to set
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
