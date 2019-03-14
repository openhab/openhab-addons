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
import java.util.List;

import org.openhab.binding.hdl.internal.handler.HdlPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The device class defines all the support HDL Device that is
 * used across the whole binding.
 *
 * @author stigla - Initial contribution
 */
public abstract class Device {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

    private int subNet = -1;
    private String serialNr = "";
    private int deviceID = -1;
    private boolean updated;

    private HashMap<String, Object> properties = new HashMap<>();

    public Device(DeviceConfiguration c) {
        this.subNet = c.getsubNet();
        this.deviceID = c.getdeviceID();
        this.serialNr = c.getSerialNr();
        this.setProperties(new HashMap<>(c.getProperties()));
    }

    public abstract DeviceType getType();

    public static Device create(String serialNr, List<DeviceConfiguration> configurations) {
        Device returnValue = null;
        for (DeviceConfiguration c : configurations) {
            if (c.getSerialNr().toUpperCase().equals(serialNr.toUpperCase())) {
                return create(c);
            }
        }
        return returnValue;
    }

    public static Device create(HdlPacket p, List<DeviceConfiguration> configurations) {
        Device device = Device.create(p.serialNr, configurations);
        if (device == null) {
            LOGGER.warn("Can't create device from received message, returning NULL.");
            return null;
        }
        return Device.update(p, configurations, device);
    }

    public static Device create(DeviceConfiguration c) {
        switch (c.getDeviceType()) {
            case MDT0601_233:
                return new MDT0601(c);
            case ML01:
                return new ML01(c);
            case MPL8_48_FH:
                return new MPL848FH(c);
            case MPT04_48:
                return new MPT0448(c);
            case MR1216_233:
                return new MR1216(c);
            case MRDA06:
                return new MRDA06(c);
            case MS08Mn_2C:
                return new MS08Mn2C(c);
            case MS12_2C:
                return new MS122C(c);
            case MS24:
                return new MS24(c);
            case MW02_231:
                return new MW02(c);
            default:
                return new UnsupportedDevice(c);
        }
    }

    public static Device update(HdlPacket p, List<DeviceConfiguration> configurations, Device device) {
        switch (device.getType()) {
            case MDT0601_233:
                MDT0601 mdt0601233 = (MDT0601) device;
                mdt0601233.treatHDLPacketForDevice(p);
                break;
            case ML01:
                ML01 ml01 = (ML01) device;
                ml01.treatHDLPacketForDevice(p);
                break;
            case MPL8_48_FH:
                MPL848FH mpl848fh = (MPL848FH) device;
                mpl848fh.treatHDLPacketForDevice(p);
                break;
            case MPT04_48:
                MPT0448 mpt0448 = (MPT0448) device;
                mpt0448.treatHDLPacketForDevice(p);
                break;
            case MR1216_233:
                MR1216 mr1216233 = (MR1216) device;
                mr1216233.treatHDLPacketForDevice(p);
                break;
            case MRDA06:
                MRDA06 mrda06 = (MRDA06) device;
                mrda06.treatHDLPacketForDevice(p);
                break;
            case MS08Mn_2C:
                MS08Mn2C ms08mn2c = (MS08Mn2C) device;
                ms08mn2c.treatHDLPacketForDevice(p);
                break;
            case MS12_2C:
                MS122C ms122c = (MS122C) device;
                ms122c.treatHDLPacketForDevice(p);
                break;
            case MS24:
                MS24 ms24 = (MS24) device;
                ms24.treatHDLPacketForDevice(p);
                break;
            case MW02:
                MW02 mw02 = (MW02) device;
                mw02.treatHDLPacketForDevice(p);
                break;
            default:
                LOGGER.debug("In HDLPacket Type: {} but unhandled device: {} in Device.", p.sourcedeviceType,
                        device.getType());
                break;
        }
        return device;
    }

    protected static int ushort(byte h, byte l) {
        return ((h << 8) & 0xff00) | (l & 0xff);
    }

    public String getSerialNr() {
        return this.serialNr;
    }

    public void setSerialNr(String SerialNr) {
        this.serialNr = SerialNr;
    }

    public final int getsubNet() {
        return subNet;
    }

    public final void setsubNet(int subNet) {
        this.subNet = subNet;
    }

    public final int getdeviceID() {
        return deviceID;
    }

    public final void setdeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    /**
     * @return the properties
     */
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }

    @Override
    public String toString() {
        return this.getType().toString() + this.getSerialNr();
    }
}
