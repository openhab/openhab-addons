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

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link CircuitScheduler} represents a circuit in the digitalSTROM-System and manages the priorities and
 * execution times for the {@link SensorJob}s on this circuit.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class CircuitScheduler {

    private final Logger logger = LoggerFactory.getLogger(CircuitScheduler.class);

    private class SensorJobComparator implements Comparator<SensorJob> {

        @Override
        public int compare(SensorJob job1, SensorJob job2) {
            return ((Long) job1.getInitalisationTime()).compareTo(job2.getInitalisationTime());
        }
    }

    private final DSID meterDSID;
    private long nextExecutionTime = System.currentTimeMillis();
    private final PriorityQueue<SensorJob> sensorJobQueue = new PriorityQueue<>(10, new SensorJobComparator());
    private final Config config;

    /**
     * Creates a new {@link CircuitScheduler}.
     *
     * @param meterDSID must not be null
     * @param config must not be null
     * @throws IllegalArgumentException if the meterDSID is null
     */
    public CircuitScheduler(DSID meterDSID, Config config) {
        if (meterDSID == null) {
            throw new IllegalArgumentException("The meterDSID must not be null!");
        }
        this.meterDSID = meterDSID;
        this.config = config;
    }

    /**
     * Creates a new {@link CircuitScheduler} and add the first {@link SensorJob} to this {@link CircuitScheduler}.
     *
     * @param sensorJob to add, must not be null
     * @param config must not be null
     */
    public CircuitScheduler(SensorJob sensorJob, Config config) {
        this.meterDSID = sensorJob.getMeterDSID();
        this.sensorJobQueue.add(sensorJob);
        this.config = config;
        logger.debug("create circuitScheduler: {} and add sensorJob: {}", this.getMeterDSID(),
                sensorJob.getDSID().toString());
    }

    /**
     * Returns the meterDSID of the dS-Meter in which the {@link SensorJob}s will be executed.
     *
     * @return meterDSID
     */
    public DSID getMeterDSID() {
        return this.meterDSID;
    }

    /**
     * Adds a new SensorJob to this {@link CircuitScheduler}, if no {@link SensorJob} with a higher priority exists.
     *
     * @param sensorJob to add
     */
    public void addSensorJob(SensorJob sensorJob) {
        synchronized (sensorJobQueue) {
            if (!this.sensorJobQueue.contains(sensorJob)) {
                sensorJobQueue.add(sensorJob);
                logger.debug("Add sensorJob: {} to circuitScheduler: {}", sensorJob.toString(), this.getMeterDSID());
            } else if (checkSensorJobPrio(sensorJob)) {
                logger.debug("add sensorJob: {} with higher priority to circuitScheduler: {}", sensorJob.toString(),
                        this.getMeterDSID());
            } else {
                logger.debug("sensorJob: {} allready exist with a higher priority", sensorJob.getDSID());
            }
        }
    }

    private boolean checkSensorJobPrio(SensorJob sensorJob) {
        synchronized (sensorJobQueue) {
            for (Iterator<SensorJob> iter = sensorJobQueue.iterator(); iter.hasNext();) {
                SensorJob existSensorJob = iter.next();
                if (existSensorJob.equals(sensorJob)) {
                    if (sensorJob.getInitalisationTime() < existSensorJob.getInitalisationTime()) {
                        iter.remove();
                        sensorJobQueue.add(sensorJob);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the next {@link SensorJob} which can be executed or null, if there are no more {@link SensorJob} to
     * execute or the wait time between the {@link SensorJob}s executions has not expired yet.
     *
     * @return next SensorJob or null
     */
    public SensorJob getNextSensorJob() {
        synchronized (sensorJobQueue) {
            if (sensorJobQueue.peek() != null && this.nextExecutionTime <= System.currentTimeMillis()) {
                nextExecutionTime = System.currentTimeMillis() + config.getSensorReadingWaitTime();
                return sensorJobQueue.poll();
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the time when the next {@link SensorJob} can be executed.
     *
     * @return next SesnorJob execution time
     */
    public Long getNextExecutionTime() {
        return this.nextExecutionTime;
    }

    /**
     * Returns the delay when the next {@link SensorJob} can be executed.
     *
     * @return next SesnorJob execution delay
     */
    public Long getNextExecutionDelay() {
        long delay = this.nextExecutionTime - System.currentTimeMillis();
        return delay > 0 ? delay : 0;
    }

    /**
     * Removes all {@link org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob}
     * of a specific {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device} with the
     * given {@link DSID}.
     *
     * @param dSID of the device
     */
    public void removeSensorJob(DSID dSID) {
        synchronized (sensorJobQueue) {
            for (Iterator<SensorJob> iter = sensorJobQueue.iterator(); iter.hasNext();) {
                SensorJob job = iter.next();
                if (job.getDSID().equals(dSID)) {
                    iter.remove();
                    logger.debug("Remove SensorJob with ID {}.", job.getID());
                }
            }
        }
    }

    /**
     * Removes the {@link SensorJob} with the given ID .
     *
     * @param id of the {@link SensorJob}
     */
    public void removeSensorJob(String id) {
        synchronized (sensorJobQueue) {
            for (Iterator<SensorJob> iter = sensorJobQueue.iterator(); iter.hasNext();) {
                SensorJob job = iter.next();
                if (job.getID().equals(id)) {
                    iter.remove();
                    logger.debug("Remove SensorJob with ID {}.", id);
                    return;
                }
            }
            logger.debug("No SensorJob with ID {} found, cannot remove a not existing SensorJob.", id);
        }
    }

    /**
     * Returns true, if there are no more {@link SensorJob}s to execute, otherwise false.
     *
     * @return no more SensorJobs? (true | false)
     */
    public boolean noMoreJobs() {
        synchronized (sensorJobQueue) {
            return this.sensorJobQueue.isEmpty();
        }
    }
}
