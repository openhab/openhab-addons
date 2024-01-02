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
package org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor;

import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneReadingJobExecutor} is the implementation of the {@link AbstractSensorJobExecutor} to execute
 * digitalSTROM-Device scene configuration {@link SensorJob}'s e.g.
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl.SceneConfigReadingJob} and
 * {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl.SceneOutputValueReadingJob}.
 * <p>
 * In addition priorities can be assigned to jobs therefore the {@link SceneReadingJobExecutor} offers the methods
 * {@link #addHighPriorityJob(SensorJob)}, {@link #addMediumPriorityJob(SensorJob)} and
 * {@link #addLowPriorityJob(SensorJob)}.
 * </p>
 * <p>
 * <b>NOTE:</b><br>
 * In contrast to the {@link SensorJobExecutor} the {@link SceneReadingJobExecutor} will execute {@link SensorJob}'s
 * with high priority always before medium priority {@link SensorJob}s and so on.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneReadingJobExecutor extends AbstractSensorJobExecutor {

    private Logger logger = LoggerFactory.getLogger(SceneReadingJobExecutor.class);

    /**
     * Creates a new {@link SceneReadingJobExecutor}.
     *
     * @param connectionManager must not be null
     */
    public SceneReadingJobExecutor(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(0);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and high-priority to SceneReadingSobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(1);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and medium-priority to SceneReadingJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(2);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and low-priority to SceneReadingJobExecutor",
                sensorJob.getDSID());
    }
}
