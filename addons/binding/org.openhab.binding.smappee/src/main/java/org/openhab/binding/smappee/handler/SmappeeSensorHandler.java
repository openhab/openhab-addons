/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.handler;

import static org.openhab.binding.smappee.SmappeeBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smappee.internal.SmappeeSensorConsumptionRecord;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeSensorHandler} is responsible for handling commands and sets the actual status for a sensor.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeSensorHandler extends AbstractSmappeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SmappeeSensorHandler.class);

    ScheduledFuture<?> scheduledJob;

    private String thingId;
    private String channelid;

    public SmappeeSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SmappeeService smappeeService = getSmappeeService();
        if (smappeeService == null || !smappeeService.isInitialized()) {
            return;
        }

        if (command instanceof RefreshType) {
            readSensor(smappeeService);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    public void newState(SmappeeSensorConsumptionRecord readings) {
        if (readings != null) {
            if (channelid == "1") {
                updateState(CHANNEL_SENSOR_VALUE, new DecimalType(readings.value1));
            } else if (channelid == "2") {
                updateState(CHANNEL_SENSOR_VALUE, new DecimalType(readings.value2));
            }
        }
    }

    @Override
    public void initialize() {
        thingId = thing.getConfiguration().get(PARAMETER_SENSOR_ID).toString();
        channelid = thing.getConfiguration().get(PARAMETER_SENSOR_CHANNEL_ID).toString();

        updateStatus(ThingStatus.ONLINE);

        // start automatic refresh
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        // stop automatic refresh
        if (scheduledJob != null) {
            scheduledJob.cancel(true);
        }
    }

    public void startAutomaticRefresh() {
        SmappeeService smappeeService = getSmappeeService();
        Runnable runnable = () -> {
            if (smappeeService != null && smappeeService.isInitialized()) {
                readSensor(smappeeService);
            }
        };

        scheduledJob = scheduler.scheduleWithFixedDelay(runnable, 0, smappeeService.config.pollingInterval,
                TimeUnit.MILLISECONDS);
    }

    private void readSensor(SmappeeService smappeeService) {
        SmappeeSensorConsumptionRecord readings = smappeeService.getLatestSensorConsumption(thingId);
        if (readings == null) {
            logger.warn("failed to read to read power consumption for sensor {}", thingId);
        } else {
            newState(readings);
        }
    }

}
