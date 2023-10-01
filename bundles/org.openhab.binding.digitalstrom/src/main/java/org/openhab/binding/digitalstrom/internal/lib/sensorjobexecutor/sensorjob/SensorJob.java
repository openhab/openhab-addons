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
package org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

/**
 * The {@link SensorJob} represents an executable job to read out digitalSTROM-Sensors or device configurations like
 * scene values.<br>
 * It can be added to an implementation of the
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.AbstractSensorJobExecutor} e.g.
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.SceneReadingJobExecutor} or
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.SensorJobExecutor}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SensorJob {

    /**
     * Returns the dSID of the {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device}
     * for which this job is to be created.
     *
     * @return dSID from the device
     */
    DSID getDSID();

    /**
     * Returns the dSID of the digitalSTROM-Meter on which this job is to be created.
     *
     * @return dSID from the device meter
     */
    DSID getMeterDSID();

    /**
     * Executes the SensorJob.
     *
     * @param dSAPI must not be null
     * @param sessionToken to login
     */
    void execute(DsAPI dSAPI, String sessionToken);

    /**
     * Returns the time when the {@link SensorJob} was initialized.
     *
     * @return the initialization time
     */
    long getInitalisationTime();

    /**
     * Sets the time when the {@link SensorJob} was initialized e.g. to manages the priority of this {@link SensorJob}.
     *
     * @param time to set
     */
    void setInitalisationTime(long time);

    /**
     * Returns the id of this {@link SensorJob}.
     *
     * @return id
     */
    String getID();
}
