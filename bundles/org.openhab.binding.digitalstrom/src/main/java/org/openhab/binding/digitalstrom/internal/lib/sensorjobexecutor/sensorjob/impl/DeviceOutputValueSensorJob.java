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
package org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl;

import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceConstants;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceStateUpdateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceOutputValueSensorJob} is the implementation of a {@link SensorJob}
 * for reading out the current device output value of a digitalSTROM-Device and update the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceOutputValueSensorJob implements SensorJob {

    private final Logger logger = LoggerFactory.getLogger(DeviceOutputValueSensorJob.class);
    private final Device device;
    private short index = 0;
    private final DSID meterDSID;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link DeviceOutputValueSensorJob} for the given {@link Device}.
     *
     * @param device to update
     */
    public DeviceOutputValueSensorJob(Device device) {
        this.device = device;
        if (device.isShade()) {
            this.index = DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT;
        } else {
            this.index = DeviceConstants.DEVICE_SENSOR_OUTPUT;
        }
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        int value = digitalSTROM.getDeviceOutputValue(token, this.device.getDSID(), null, null, index);
        logger.debug("Device output value on Demand : {}, dSID: {}", value, this.device.getDSID().getValue());

        if (value != 1) {
            switch (this.index) {
                case 0:
                    this.device.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT, value));
                    return;
                case 2:
                    this.device.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION, value));
                    if (device.isBlind()) {
                        value = digitalSTROM.getDeviceOutputValue(token, this.device.getDSID(), null, null,
                                DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT);
                        logger.debug("Device angle output value on Demand : {}, dSID: {}", value,
                                this.device.getDSID().getValue());
                        if (value != 1) {
                            this.device.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE, value));
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceOutputValueSensorJob other) {
            String key = this.device.getDSID().getValue() + this.index;
            return key.equals((other.device.getDSID().getValue() + other.index));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.index).hashCode();
    }

    @Override
    public DSID getDSID() {
        return device.getDSID();
    }

    @Override
    public DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public long getInitalisationTime() {
        return this.initalisationTime;
    }

    @Override
    public void setInitalisationTime(long time) {
        this.initalisationTime = time;
    }

    @Override
    public String toString() {
        return "DeviceOutputValueSensorJob [deviceDSID : " + device.getDSID().getValue() + ", meterDSID=" + meterDSID
                + ", initalisationTime=" + initalisationTime + "]";
    }

    @Override
    public String getID() {
        return getID(device);
    }

    /**
     * Returns the id for a {@link DeviceOutputValueSensorJob} with the given {@link Device}.
     *
     * @param device to update
     * @return id
     */
    public static String getID(Device device) {
        return DeviceOutputValueSensorJob.class.getSimpleName() + "-" + device.getDSID().getValue();
    }
}
