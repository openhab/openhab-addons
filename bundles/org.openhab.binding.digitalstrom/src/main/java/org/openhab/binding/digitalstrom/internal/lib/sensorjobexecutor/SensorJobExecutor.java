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
package org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor;

import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensorJobExecutor} is the implementation of the {@link AbstractSensorJobExecutor} to execute
 * digitalSTROM-Device {@link SensorJob}'s e.g.
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl.DeviceConsumptionSensorJob} and
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl.DeviceOutputValueSensorJob}.
 * <p>
 * In addition priorities can be assigned to jobs, but the following list shows the maximum evaluation of a
 * {@link SensorJob} per priority.
 * </p>
 * <ul>
 * <li>low priority: read cycles before execution is set in
 * {@link org.openhab.binding.digitalstrom.internal.lib.config.Config}</li>
 * <li>medium priority: read cycles before execution is set in
 * {@link org.openhab.binding.digitalstrom.internal.lib.config.Config}</li>
 * <li>high priority: read cycles before execution 0</li>
 * </ul>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SensorJobExecutor extends AbstractSensorJobExecutor {

    private final Logger logger = LoggerFactory.getLogger(SensorJobExecutor.class);

    private final long mediumFactor = super.config.getSensorReadingWaitTime() * super.config.getMediumPriorityFactor();
    private final long lowFactor = super.config.getSensorReadingWaitTime() * super.config.getLowPriorityFactor();

    /**
     * Creates a new {@link SensorJobExecutor}.
     *
     * @param connectionManager must not be null
     */
    public SensorJobExecutor(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and high-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.mediumFactor);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and medium-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.lowFactor);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and low-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }
}
