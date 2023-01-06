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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.SensorJob;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractSensorJobExecutor} provides the working process to execute implementations of {@link SensorJob}'s
 * in the time interval set at the {@link Config}.
 * <p>
 * The following methods can be overridden by subclasses to implement an execution priority:
 * </p>
 * <ul>
 * <li>{@link #addLowPriorityJob(SensorJob)}</li>
 * <li>{@link #addMediumPriorityJob(SensorJob)}</li>
 * <li>{@link #addHighPriorityJob(SensorJob)}</li>
 * </ul>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public abstract class AbstractSensorJobExecutor {

    private final Logger logger = LoggerFactory.getLogger(AbstractSensorJobExecutor.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private Map<DSID, ScheduledFuture<?>> pollingSchedulers;

    private final DsAPI dSAPI;
    protected Config config;
    private final ConnectionManager connectionManager;

    private final List<CircuitScheduler> circuitSchedulerList = new LinkedList<>();

    private class ExecutorRunnable implements Runnable {
        private final CircuitScheduler circuit;

        public ExecutorRunnable(CircuitScheduler circuit) {
            this.circuit = circuit;
        }

        @Override
        public void run() {
            // pollingSchedulers is not final and might be set to null by another thread. See #8214
            Map<DSID, ScheduledFuture<?>> pollingSchedulers = AbstractSensorJobExecutor.this.pollingSchedulers;

            SensorJob sensorJob = circuit.getNextSensorJob();
            DSID meter = circuit.getMeterDSID();

            if (sensorJob != null) {
                sensorJob.execute(dSAPI, connectionManager.getSessionToken());
            }
            if (circuit.noMoreJobs() && pollingSchedulers != null) {
                logger.debug("no more jobs... stop circuit schedduler with id = {}", meter);
                ScheduledFuture<?> scheduler = pollingSchedulers.get(meter);
                if (scheduler != null) {
                    scheduler.cancel(true);
                }
            }
        }
    }

    /**
     * Creates a new {@link AbstractSensorJobExecutor}.
     *
     * @param connectionManager must not be null
     */
    public AbstractSensorJobExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        config = connectionManager.getConfig();
        this.dSAPI = connectionManager.getDigitalSTROMAPI();
    }

    /**
     * Stops all circuit schedulers.
     */
    public synchronized void shutdown() {
        if (pollingSchedulers != null) {
            for (ScheduledFuture<?> scheduledExecutor : pollingSchedulers.values()) {
                scheduledExecutor.cancel(true);
            }
            pollingSchedulers = null;
            logger.debug("stop all circuit schedulers.");
        }
    }

    /**
     * Starts all circuit schedulers.
     */
    public synchronized void startExecutor() {
        logger.debug("start all circuit schedulers.");
        if (pollingSchedulers == null) {
            pollingSchedulers = new HashMap<>();
        }
        if (circuitSchedulerList != null && !circuitSchedulerList.isEmpty()) {
            for (CircuitScheduler circuit : circuitSchedulerList) {
                startSchedduler(circuit);
            }
        }
    }

    private void startSchedduler(CircuitScheduler circuit) {
        if (pollingSchedulers != null) {
            if (pollingSchedulers.get(circuit.getMeterDSID()) == null
                    || pollingSchedulers.get(circuit.getMeterDSID()).isCancelled()) {
                pollingSchedulers.put(circuit.getMeterDSID(),
                        scheduler.scheduleWithFixedDelay(new ExecutorRunnable(circuit), circuit.getNextExecutionDelay(),
                                config.getSensorReadingWaitTime(), TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * Adds a high priority {@link SensorJob}.
     *
     * @param sensorJob to add
     */
    public void addHighPriorityJob(SensorJob sensorJob) {
        // can be Overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds a medium priority {@link SensorJob}.
     *
     * @param sensorJob to add
     */
    public void addMediumPriorityJob(SensorJob sensorJob) {
        // can be overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds a low priority {@link SensorJob}.
     *
     * @param sensorJob to add
     */
    public void addLowPriorityJob(SensorJob sensorJob) {
        // can be overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds a {@link SensorJob} with a given priority .
     *
     * @param sensorJob to add
     * @param priority to update
     */
    public void addPriorityJob(SensorJob sensorJob, long priority) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(priority);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and priority {} to AbstractJobExecutor",
                sensorJob.getDSID(), priority);
    }

    /**
     * Adds the given {@link SensorJob}.
     *
     * @param sensorJob to add
     */
    protected void addSensorJobToCircuitScheduler(SensorJob sensorJob) {
        synchronized (this.circuitSchedulerList) {
            CircuitScheduler circuit = getCircuitScheduler(sensorJob.getMeterDSID());
            if (circuit != null) {
                circuit.addSensorJob(sensorJob);
            } else {
                circuit = new CircuitScheduler(sensorJob, config);
                this.circuitSchedulerList.add(circuit);
            }
            startSchedduler(circuit);
        }
    }

    private CircuitScheduler getCircuitScheduler(DSID dsid) {
        for (CircuitScheduler circuit : this.circuitSchedulerList) {
            if (circuit.getMeterDSID().equals(dsid)) {
                return circuit;
            }
        }
        return null;
    }

    /**
     * Removes all SensorJobs of a specific {@link Device}.
     *
     * @param device to remove
     */
    public void removeSensorJobs(Device device) {
        if (device != null) {
            CircuitScheduler circuit = getCircuitScheduler(device.getMeterDSID());
            if (circuit != null) {
                circuit.removeSensorJob(device.getDSID());
            }
        }
    }

    /**
     * Removes the {@link SensorJob} with the given ID.
     *
     * @param device needed for the meterDSID
     * @param ID of the {@link SensorJob} to remove
     */
    public void removeSensorJob(Device device, String ID) {
        if (device != null && ID != null) {
            CircuitScheduler circuit = getCircuitScheduler(device.getMeterDSID());
            if (circuit != null) {
                circuit.removeSensorJob(ID);
            }
        }
    }
}
