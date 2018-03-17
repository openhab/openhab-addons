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

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smappee.internal.SmappeeApplianceEvent;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeApplianceHandler} is responsible for handling commands and sets the actual status for an appliance.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeApplianceHandler extends AbstractSmappeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SmappeeApplianceHandler.class);

    ScheduledFuture<?> scheduledJob;

    private String thingId;

    public SmappeeApplianceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        SmappeeService smappeeService = getSmappeeService();
        if (smappeeService == null || !smappeeService.isInitialized()) {
            return;
        }

        if (command instanceof RefreshType) {
            readAppliance(smappeeService);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    public void newState(SmappeeApplianceEvent readings) {
        if (readings != null) {
            updateState(CHANNEL_APPLIANCE_POWER, new DecimalType(readings.activePower));
            updateState(CHANNEL_APPLIANCE_LASTUPDATE, new DateTimeType(readings.getTimestamp()));
        }
    }

    @Override
    public void initialize() {

        thingId = thing.getConfiguration().get(PARAMETER_APPLIANCE_ID).toString();

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
                readAppliance(smappeeService);
            }
        };

        scheduledJob = scheduler.scheduleWithFixedDelay(runnable, 0, smappeeService.config.poll_time,
                TimeUnit.MILLISECONDS);
    }

    private void readAppliance(SmappeeService smappeeService) {
        SmappeeApplianceEvent readings = smappeeService.getLatestApplianceReading(thingId);
        if (readings == null) {
            logger.warn("failed to read to read power consumption for appliance {}", thingId);
        } else {
            newState(readings);
        }
    }

}
