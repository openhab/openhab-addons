/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.luftdateninfo.internal.LuftdatenInfoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PMHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class BaseSensorHandler extends BaseThingHandler {

    protected static final Logger logger = LoggerFactory.getLogger(BaseSensorHandler.class);

    protected @Nullable LuftdatenInfoConfiguration config;
    protected @Nullable ScheduledFuture<?> refreshJob;
    protected int refreshInterval = 5;

    protected int configStatus = -1;
    protected final static int CONFIG_OK = 0;
    protected final static int CONFIG_IS_NULL = 1;
    protected final static int CONFIG_SENSOR_IS_NULL = 2;
    protected final static int CONFIG_SENSOR_NUMBER = 3;

    protected int updateStatus = -1;
    protected final static int UPDATE_OK = 0;
    protected final static int UPDATE_CONNECTION_ERROR = 1;
    protected final static int UPDATE_VALUE_ERROR = 2;
    protected final static int UPDATE_VALUE_EMPTY = 3;

    protected int LC_UNKNOWN = -1;
    protected int LC_RUNNING = 0;
    protected int LC_INITIALIZING = 1;
    protected int LC_DISPOSED = 2;
    protected int lifecycleStatus = LC_UNKNOWN;

    public BaseSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateFromCache();
        }
    }

    @Override
    public void initialize() {
        lifecycleStatus = LC_INITIALIZING;
        scheduler.execute(() -> {
            config = getConfigAs(LuftdatenInfoConfiguration.class);
            configStatus = checkConfig(config);
            if (configStatus == CONFIG_OK) {
                updateStatus = updateChannels();
                if (updateStatus == UPDATE_OK) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Start refresh job at interval {} min.", refreshInterval);
                    if (refreshJob != null) {
                        refreshJob.cancel(true);
                    }
                    refreshJob = scheduler.scheduleWithFixedDelay(this::updateChannels, 0, refreshInterval,
                            TimeUnit.MINUTES);
                } else {
                    switch (updateStatus) {
                        case UPDATE_CONNECTION_ERROR:
                            logger.warn("Update failed due to Connection error. Trying to recover in next refresh");
                            break;
                        case UPDATE_VALUE_ERROR:
                            logger.warn(
                                    "Sensor values doesn't match - please check if Sensor ID is delivering the correct Thing channel values");
                            break;
                        case UPDATE_VALUE_EMPTY:
                            logger.warn(
                                    "No values deliverd by Sensor. Please check for valid Sensor ID in configuration");
                            break;
                    }
                    updateStatus(ThingStatus.OFFLINE);
                }
            } else {
                logger.warn("Configuration not valid. Sensor ID as a number is mandatory!");
                updateStatus(ThingStatus.OFFLINE);
            }
            lifecycleStatus = LC_RUNNING;
        });

    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        lifecycleStatus = LC_DISPOSED;
    }

    /**
     * Checks if config is valid - a) not null and b) sensorid is a number
     *
     * @param c
     * @return
     */
    private int checkConfig(@Nullable LuftdatenInfoConfiguration c) {
        if (c != null) {
            if (c.sensorid != null) {
                try {
                    Integer.parseInt(c.sensorid);
                    return CONFIG_OK;
                } catch (NumberFormatException t) {
                    return CONFIG_SENSOR_NUMBER;
                }
            } else {
                return CONFIG_SENSOR_IS_NULL;
            }
        } else {
            return CONFIG_IS_NULL;
        }
    }

    public int getLifecycleStatus() {
        return lifecycleStatus;
    }

    protected abstract int updateChannels();

    protected abstract void updateFromCache();
}
