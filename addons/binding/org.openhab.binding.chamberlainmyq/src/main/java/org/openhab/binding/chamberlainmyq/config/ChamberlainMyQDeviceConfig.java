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
package org.openhab.binding.chamberlainmyq.config;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.chamberlainmyq.internal.json.Attribute;
import org.openhab.binding.chamberlainmyq.internal.json.Device;

/**
 * The {@link ChamberlainMyQDeviceConfig} class represents the common configuration
 * parameters of a MyQ Device
 *
 * @author Scott Hanson - Initial contribution
 *
 */
public class ChamberlainMyQDeviceConfig {
    private int deviceId;
    private String deviceType;
    private int deviceTypeId;
    private int state;
    private String description;
    private String serialNumber;
    private int parentId;
    private boolean online;

    public ChamberlainMyQDeviceConfig(Map<String, String> properties) {
        this.deviceId = (int) Double.parseDouble(properties.get(MYQ_ID).toString());
        this.deviceType = properties.get(MYQ_TYPE).toString();
        this.deviceTypeId = (int) Double.parseDouble(properties.get(MYQ_TYPEID).toString());
        this.state = (int) Double.parseDouble(properties.get(MYQ_STATE).toString());
        this.description = properties.get(MYQ_DESC).toString();
        this.serialNumber = properties.get(MYQ_SERIAL).toString();
        this.parentId = (int) Double.parseDouble(properties.get(MYQ_PARENT).toString());
        this.online = Boolean.valueOf(properties.get(MYQ_ONLINE).toString());
    }

    public ChamberlainMyQDeviceConfig(Device myQDevice) {
        this.deviceId = myQDevice.getMyQDeviceId();
        readConfig(myQDevice);
    }

    public void readConfig(Device myQDevice) {
        this.deviceType = myQDevice.getMyQDeviceTypeName();
        this.serialNumber = myQDevice.getSerialNumber();
        this.deviceTypeId = myQDevice.getMyQDeviceTypeId();
        this.parentId = myQDevice.getParentMyQDeviceId();

        for (Attribute attr : myQDevice.getAttributes()) {
            String attributeName = attr.getAttributeDisplayName();
            String attributeValue = attr.getValue();
            if (attributeName.compareTo(MYQ_DESC) == 0) {
                description = attributeValue;
            }
            if (attributeName.compareTo(CHANNEL_DOOR_STATE) == 0) {
                state = (int) Double.parseDouble(attributeValue);
            }
            if (attributeName.compareTo(CHANNEL_LIGHT_STATE) == 0) {
                state = (int) Double.parseDouble(attributeValue);
            }
            if (attributeName.compareTo(MYQ_ONLINE) == 0) {
                this.online = Boolean.valueOf(attributeValue);
            }
        }

        if (description.isEmpty()) {
            this.description = this.serialNumber;
        }
    }

    public String asString() {
        return ("DeviceID: " + deviceId + "\n" + "Device Type: " + deviceType + "\n" + "Device Type Id:  "
                + deviceTypeId + "\n" + "Parent Id:  " + parentId + "\n" + "Model Serial:  " + serialNumber + "\n"
                + "Description:  " + description + "\n" + "State:  " + state + "\n" + "Online:  " + online + "\n");
    }

    public boolean validateConfig() {
        if (this.deviceId == 0) {
            return false;
        }
        return true;
    }

    public String getDeviceStatusStr() {
        switch (state) {
            case 1:
            case 9:
                return "Open";
            case 2:
                return "Closed";
            case 3:
                return "Partially Open/Closed";
            case 4:
                return "Opening";
            case 5:
                return "Closing";
            case 8:
                return "Moving";
            default:
                return "Unknown";
        }
    }

    public OnOffType getDoorStatusOnOff() {
        if (isDoorClosed()) {
            return OnOffType.OFF;
        } else {
            return OnOffType.ON;
        }
    }

    public OnOffType getLightStatusOnOff() {
        if (state == 1) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    public PercentType getDeviceStatusPercent() {
        if (isDoorOpen()) {
            return PercentType.ZERO;
        }
        if (isDoorClosed()) {
            return PercentType.HUNDRED;
        }
        return new PercentType(50);
    }

    public OpenClosedType isDoorClosedContact() {
        if (isDoorClosed()) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.OPEN;
    }

    public OpenClosedType isDoorOpenContact() {
        if (isDoorOpen()) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.OPEN;
    }

    public boolean isDoorClosed() {
        if (state == 2) {
            return true;
        }
        return false;
    }

    public boolean isDoorOpen() {
        if (state == 1 || state == 9) {
            return true;
        }
        return false;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getDeviceIdString() {
        return Integer.toString(deviceId);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public int getParentId() {
        return parentId;
    }

    public int getState() {
        return state;
    }

    public boolean getOnline() {
        return online;
    }

    public ThingStatus getThingOnline() {
        if (online) {
            return ThingStatus.ONLINE;
        } else {
            return ThingStatus.OFFLINE;
        }
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(MYQ_ID, getDeviceId());
        properties.put(MYQ_TYPE, getDeviceType());
        properties.put(MYQ_PARENT, getParentId());
        properties.put(MYQ_SERIAL, getSerialNumber());
        properties.put(MYQ_TYPEID, getDeviceTypeId());
        properties.put(MYQ_STATE, getState());
        properties.put(MYQ_DESC, getDescription());
        properties.put(MYQ_ONLINE, getOnline());
        return properties;
    }
}
