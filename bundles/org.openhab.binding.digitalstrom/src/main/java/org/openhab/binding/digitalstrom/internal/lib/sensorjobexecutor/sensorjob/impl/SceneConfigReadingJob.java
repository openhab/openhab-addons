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
package org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl;

import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceSceneSpec;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneConfigReadingJob} is the implementation of a {@link SensorJob}
 * for reading out a scene output value of a digitalSTROM-Device and store it into the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneConfigReadingJob implements SensorJob {

    private final Logger logger = LoggerFactory.getLogger(SceneConfigReadingJob.class);

    private final Device device;
    private short sceneID = 0;
    private final DSID meterDSID;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link SceneConfigReadingJob} for the given {@link Device} and the given sceneID.
     *
     * @param device to update
     * @param sceneID to update
     */
    public SceneConfigReadingJob(Device device, short sceneID) {
        this.device = device;
        this.sceneID = sceneID;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        DeviceSceneSpec sceneConfig = digitalSTROM.getDeviceSceneMode(token, device.getDSID(), null, null, sceneID);

        if (sceneConfig != null) {
            device.addSceneConfig(sceneID, sceneConfig);
            logger.debug("UPDATED scene configuration for dSID: {}, sceneID: {}, configuration: {}",
                    this.device.getDSID(), sceneID, sceneConfig);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SceneConfigReadingJob other) {
            String str = other.device.getDSID().getValue() + "-" + other.sceneID;
            return (this.device.getDSID().getValue() + "-" + this.sceneID).equals(str);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.sceneID).hashCode();
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
        return "SceneConfigReadingJob [sceneID: " + sceneID + ", deviceDSID : " + device.getDSID().getValue()
                + ", meterDSID=" + meterDSID + ", initalisationTime=" + initalisationTime + "]";
    }

    @Override
    public String getID() {
        return getID(device, sceneID);
    }

    /**
     * Returns the id for a {@link SceneConfigReadingJob} with the given {@link Device} and sceneID.
     *
     * @param device to update
     * @param sceneID to update
     * @return id
     */
    public static String getID(Device device, Short sceneID) {
        return SceneConfigReadingJob.class.getSimpleName() + "-" + device.getDSID().getValue() + "-" + sceneID;
    }
}
