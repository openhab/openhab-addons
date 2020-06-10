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

import static org.openhab.binding.luftdateninfo.internal.LuftdatenInfoBindingConstants.*;
import static org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.luftdateninfo.internal.LuftdatenInfoConfiguration;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PMHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PMHandler extends BaseThingHandler {

    private static final Logger logger = LoggerFactory.getLogger(PMHandler.class);

    private @Nullable LuftdatenInfoConfiguration config;
    private @Nullable ScheduledFuture<?> refreshJob;
    private int refreshInterval = 1;

    public PMHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("handleCommand: Channel: {}, Command: {}", channelUID.getId(), command.toString());
        System.out.println("handleCommand: " + config.sensorid + " Channel: " + channelUID.getId() + ", Command: "
                + command.toString());
        if (command instanceof RefreshType) {
            System.out.println("schedule refresh");
            scheduler.schedule(this::updateChannels, 1, TimeUnit.SECONDS);
        }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(LuftdatenInfoConfiguration.class);
        System.out.println("Init: " + config.sensorid);
        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        // Example for background initialization:
        scheduler.execute(() -> {
            // updateStatus(ThingStatus.INITIALIZING);
            if (updateChannels()) {
                updateStatus(ThingStatus.ONLINE);
                triggerSchedule();
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private boolean updateChannels() {
        String response = HTTPHandler.getResponse(config.sensorid);
        if (response != null) {
            List<SensorDataValue> valueList = HTTPHandler.getValues(response);
            if (valueList != null) {
                if (HTTPHandler.isParticulate(valueList)) {

                    Iterator<SensorDataValue> iter = valueList.iterator();
                    while (iter.hasNext()) {
                        SensorDataValue v = iter.next();
                        if (v.getValue_type().equals(P1)) {
                            logger.info("Update Channel {}: {}", PM100_CHANNEL, v.getValue());
                            System.out.println("Update Channel " + PM100_CHANNEL + v.getValue());
                            updateState(PM100_CHANNEL, new DecimalType(v.getValue()));
                        } else if (v.getValue_type().equals(P2)) {
                            logger.info("Update Channel {}: {}", PM25_CHANNEL, v.getValue());
                            System.out.println("Update Channel " + PM25_CHANNEL + v.getValue());
                            updateState(PM25_CHANNEL, new DecimalType(v.getValue()));
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void triggerSchedule() {
        System.out.println("triggerSchedule");
        logger.debug("Start refresh job at interval {} min.", refreshInterval);
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateChannels, 0, 5, TimeUnit.MINUTES);

    }

}
