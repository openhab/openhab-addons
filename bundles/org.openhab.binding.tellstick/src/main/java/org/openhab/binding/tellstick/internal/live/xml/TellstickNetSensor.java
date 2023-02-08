/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.live.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tellstick.device.iface.Device;
import org.tellstick.enums.DeviceType;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sensor")
public class TellstickNetSensor implements Device {
    @XmlAttribute(name = "id")
    int deviceId;
    @XmlAttribute()
    private String protocol;
    @XmlAttribute()
    private String name;
    @XmlAttribute()
    @XmlJavaTypeAdapter(value = NumberToBooleanMapper.class)
    private Boolean online;
    @XmlElement(name = "data")
    private List<DataTypeValue> data;
    @XmlAttribute()
    private Long lastUpdated;
    private boolean updated;
    @XmlAttribute()
    private Long battery;

    public TellstickNetSensor() {
    }

    public TellstickNetSensor(int id) {
        this.deviceId = id;
    }

    @Override
    public int getId() {
        return deviceId;
    }

    @Override
    public String getUUId() {
        return Integer.toString(deviceId);
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SENSOR;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getOnline() {
        return online;
    }

    // @XmlJavaTypeAdapter(value = NumberToBooleanMapper.class)
    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<DataTypeValue> getData() {
        return data;
    }

    public void setData(List<DataTypeValue> data) {
        this.data = data;
    }

    @Override
    public String getModel() {
        return null;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + deviceId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TellstickNetSensor other = (TellstickNetSensor) obj;
        return deviceId == other.deviceId;
    }

    public void setUpdated(boolean b) {
        this.updated = b;
    }

    public boolean isUpdated() {
        return updated;
    }

    public boolean isSensorOfType(LiveDataType type) {
        boolean res = false;
        if (data != null) {
            for (DataTypeValue val : data) {
                if (val.getName() == type) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public Long getBattery() {
        return battery;
    }

    public void setBattery(Long battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TellstickNetSensor [deviceId=").append(deviceId).append(", protocol=").append(protocol)
                .append(", name=").append(name).append(", online=").append(online).append(", data=").append(data)
                .append(", lastUpdated=").append(lastUpdated).append(", updated=").append(updated).append("]");
        return builder.toString();
    }
}
