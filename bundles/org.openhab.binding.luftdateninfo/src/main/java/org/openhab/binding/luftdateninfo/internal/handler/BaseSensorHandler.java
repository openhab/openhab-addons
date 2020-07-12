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

    protected static final int REFRESH_INTERVAL_MIN = 5;
    protected static final HTTPHandler HTTP = new HTTPHandler();
    private static final LuftdatenInfoConfiguration DEFAULT_CONFIG = new LuftdatenInfoConfiguration();
    protected final Logger logger = LoggerFactory.getLogger(BaseSensorHandler.class);

    protected LuftdatenInfoConfiguration config = DEFAULT_CONFIG;
    protected @Nullable ScheduledFuture<?> refreshJob;

    protected ConfigStatus configStatus = ConfigStatus.UNKNOWN;

    public enum ConfigStatus {
        OK,
        IS_NULL,
        SENSOR_IS_NULL,
        SENSOR_NOT_A_NUMBER,
        UNKNOWN
    };

    protected UpdateStatus updateStatus = UpdateStatus.UNKNOWN;

    public enum UpdateStatus {
        OK,
        CONNECTION_ERROR,
        VALUE_ERROR,
        VALUE_EMPTY,
        UNKNOWN
    }

    protected LifecycleStatus lifecycleStatus = LifecycleStatus.UNKNOWN;

    public enum LifecycleStatus {
        UNKNOWN,
        RUNNING,
        INITIALIZING,
        DISPOSED
    }

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
        lifecycleStatus = LifecycleStatus.INITIALIZING;
        scheduler.execute(() -> {
            startUp();
        });
    }

    private void startUp() {
        config = getConfigAs(LuftdatenInfoConfiguration.class);
        configStatus = checkConfig(config);
        if (configStatus == ConfigStatus.OK) {
            update();
            if (updateStatus == UpdateStatus.OK) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Start refresh job at interval {} min.", REFRESH_INTERVAL_MIN);
                startSchedule();
            } else {
                switch (updateStatus) {
                    case CONNECTION_ERROR:
                        logger.warn("Update failed due to Connection error. Trying to recover in next refresh");
                        // start job even if first update isn't valid
                        startSchedule();
                        updateStatus(ThingStatus.OFFLINE);
                        break;
                    case VALUE_EMPTY:
                        logger.warn("No values deliverd by Sensor.  Trying to recover in next refresh");
                        // start job even if first update isn't valid
                        startSchedule();
                        updateStatus(ThingStatus.ONLINE);
                        break;
                    case VALUE_ERROR:
                        logger.warn(
                                "Sensor values doesn't match - please check if Sensor ID is delivering the correct Thing channel values");
                        updateStatus(ThingStatus.OFFLINE);
                        break;
                    default:
                        logger.warn("Error during update - please check your config data");
                        updateStatus(ThingStatus.OFFLINE);
                        break;
                }
            }
        } else {
            logger.warn("Configuration not valid. Sensor ID as a number is mandatory!");
            updateStatus(ThingStatus.OFFLINE);
        }
        lifecycleStatus = LifecycleStatus.RUNNING;
    }

    private void startSchedule() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            if (localRefreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::update, 5, REFRESH_INTERVAL_MIN, TimeUnit.MINUTES);
            } // else - scheduler is already running!
        } else {
            refreshJob = scheduler.scheduleWithFixedDelay(this::update, 5, REFRESH_INTERVAL_MIN, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
        lifecycleStatus = LifecycleStatus.DISPOSED;
    }

    /**
     * Checks if config is valid - a) not null and b) sensorid is a number
     *
     * @param c
     * @return
     */
    private ConfigStatus checkConfig(@Nullable LuftdatenInfoConfiguration c) {
        if (c != null) {
            try {
                Integer.parseInt(c.sensorid);
                return ConfigStatus.OK;
            } catch (NumberFormatException t) {
                return ConfigStatus.SENSOR_NOT_A_NUMBER;
            }
        } else {
            return ConfigStatus.IS_NULL;
        }
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    protected void update() {
        String response = HTTP.getResponse(config.sensorid);
        updateStatus = updateChannels(response);
    }

    protected abstract UpdateStatus updateChannels(@Nullable String json);

    protected abstract void updateFromCache();
}
