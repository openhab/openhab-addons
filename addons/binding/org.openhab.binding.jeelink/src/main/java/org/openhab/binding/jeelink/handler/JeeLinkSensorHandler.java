/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.jeelink.config.JeeLinkSensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract thing handler for sensors connected to a JeeLink.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class JeeLinkSensorHandler<R extends Reading> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkSensorHandler.class);

    private JeeLinkReadingListener<R> listener;
    private Class<R> clazz;

    private ScheduledFuture<?> valueUpdateJob;
    private ScheduledFuture<?> statusUpdateJob;

    private boolean initialized;

    public JeeLinkSensorHandler(Thing thing, Class<R> clazz) {
        super(thing);

        this.clazz = clazz;
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public synchronized void initialize() {
        if (initialized) {
            logger.debug("JeeLink sensor handler for thing {} ({}) is already initialized", getThing().getLabel(),
                    getThing().getUID());
            return;
        }

        statusUpdateJob = createStatusUpdateJob();
        final int updateInterval = getConfig().as(JeeLinkSensorConfig.class).updateInterval;
        if (updateInterval > 0) {
            valueUpdateJob = createUpdateJob(updateInterval);
        }

        final int bufferSize = getConfig().as(JeeLinkSensorConfig.class).bufferSize;
        String idStr = getConfig().as(JeeLinkSensorConfig.class).sensorId;
        try {
            updateStatus(ThingStatus.OFFLINE);

            Average<R> avg = null;
            if (bufferSize > 0) {
                logger.debug("Using rolling average with buffer size {}...", bufferSize);
                avg = new RollingReadingAverage<>(clazz, bufferSize);
            }

            logger.debug("Adding reading listener for id {}...", idStr);
            listener = new JeeLinkReadingListener<R>(avg) {
                @Override
                public synchronized void handleReading(R reading) {
                    if (isReadingWithinBounds(reading)) {
                        boolean initial = lastReading == null;

                        // propagate initial reading
                        if (initial) {
                            updateReadingStates(reading);
                        }
                        super.handleReading(reading);

                        // propagate every reading in live mode
                        if (!initial && updateInterval == 0) {
                            updateReadingStates(getCurrentReading());
                        }
                    }

                    // make sure status is online as soon as we get a reading
                    updateStatus(ThingStatus.ONLINE);
                }
            };

            JeeLinkHandler jlh = (JeeLinkHandler) getBridge().getHandler();
            JeeLinkReadingConverter<R> c = jlh.getConverter(clazz);
            c.addReadingListener(idStr, listener);

            initialized = true;
        } catch (NumberFormatException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Thing has an invalid sensor id: " + idStr);
            logger.debug("JeeLink sensor handler for thing {} ({}) is OFFLINE", getThing().getLabel(),
                    getThing().getUID());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (initialized) {
            JeeLinkHandler jlh = (JeeLinkHandler) getBridge().getHandler();
            JeeLinkReadingConverter<R> c = jlh.getConverter(clazz);

            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                String idStr = getConfig().as(JeeLinkSensorConfig.class).sensorId;
                c.addReadingListener(idStr, listener);
            } else {
                c.removeReadingListener(listener);
            }
        }
    }

    private ScheduledFuture<?> createUpdateJob(final int updateInterval) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.trace("JeeLink sensor cyclic propagation for thing {} ({})...", getThing().getLabel(),
                        getThing().getUID());

                if (listener != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    updateReadingStates(listener.getCurrentReading());
                }
            }
        };
        logger.debug("JeeLink sensor handler value propagation job for thing {} ({}) created with interval {} s",
                getThing().getLabel(), getThing().getUID(), updateInterval);
        return scheduler.scheduleWithFixedDelay(runnable, updateInterval, updateInterval, TimeUnit.SECONDS);
    }

    private ScheduledFuture<?> createStatusUpdateJob() {
        final int sensorTimeout = getConfig().as(JeeLinkSensorConfig.class).sensorTimeout;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null && getThing().getStatus() == ThingStatus.ONLINE
                        && (listener.getLastReadingTime() == -1
                                || System.currentTimeMillis() - listener.getLastReadingTime() > sensorTimeout * 1000)) {
                    logger.debug("Setting JeeLink sensor handler status for thing {} ({}) to OFFLINE",
                            getThing().getLabel(), getThing().getUID());
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        };
        logger.debug("JeeLink sensor timeout job for thing {} ({}) created with interval {} s", getThing().getLabel(),
                getThing().getUID(), sensorTimeout);
        return scheduler.scheduleWithFixedDelay(runnable, sensorTimeout, 1, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void dispose() {
        if (initialized) {
            JeeLinkHandler jlh = (JeeLinkHandler) getBridge().getHandler();
            JeeLinkReadingConverter<R> c = jlh.getConverter(clazz);
            c.removeReadingListener(listener);
            listener = null;

            if (valueUpdateJob != null) {
                valueUpdateJob.cancel(true);
                valueUpdateJob = null;
            }
            if (statusUpdateJob != null) {
                statusUpdateJob.cancel(true);
                statusUpdateJob = null;
            }

            initialized = false;

            super.dispose();
        }
    }

    /**
     * Override to add reading validation.
     *
     * @param reading the reading to validate.
     * @return whether the reading is valid.
     */
    public boolean isReadingWithinBounds(R reading) {
        return true;
    }

    public abstract void updateReadingStates(R reading);
}
