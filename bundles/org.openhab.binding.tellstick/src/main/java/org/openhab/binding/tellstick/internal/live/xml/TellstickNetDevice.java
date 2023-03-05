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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tellstick.device.iface.Device;
import org.tellstick.enums.DeviceType;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TellstickNetDevice implements Device {
    @XmlAttribute(name = "id")
    int deviceId;
    private String protocol;
    private String model;
    @XmlAttribute()
    private String name;
    @XmlAttribute()
    @XmlJavaTypeAdapter(value = NumberToBooleanMapper.class)
    private Boolean online;
    @XmlAttribute
    private int state;
    @XmlAttribute
    private String statevalue;
    @XmlAttribute
    private int methods;

    private boolean updated;

    public TellstickNetDevice() {
    }

    public TellstickNetDevice(int id) {
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
    public String getModel() {
        return model;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.DEVICE;
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

    public void setModel(String model) {
        this.model = model;
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

    public void setUpdated(boolean b) {
        this.updated = b;
    }

    public boolean isUpdated() {
        return updated;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStatevalue() {
        return statevalue;
    }

    public void setStatevalue(String statevalue) {
        this.statevalue = statevalue;
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
        TellstickNetDevice other = (TellstickNetDevice) obj;
        return deviceId == other.deviceId;
    }

    public int getMethods() {
        return methods;
    }

    public void setMethods(int methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "TellstickNetDevice [deviceId=" + deviceId + ", name=" + name + ", online=" + online + ", state=" + state
                + ", statevalue=" + statevalue + ", updated=" + updated + "]";
    }
}
