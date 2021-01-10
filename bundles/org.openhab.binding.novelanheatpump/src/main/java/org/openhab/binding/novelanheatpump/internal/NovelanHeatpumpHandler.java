/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.novelanheatpump.internal;

import static org.openhab.binding.novelanheatpump.internal.NovelanHeatpumpBindingConstants.CHANNEL_1;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NovelanHeatpumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 */
@NonNullByDefault
public class NovelanHeatpumpHandler extends BaseThingHandler {
    private @Nullable ScheduledFuture<?> refreshJob;

    private final Logger logger = LoggerFactory.getLogger(NovelanHeatpumpHandler.class);

    private @Nullable NovelanHeatpumpConfiguration config;
    private @Nullable HeatpumpConnector connector;

    public NovelanHeatpumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NovelanHeatpump handler.");
        config = getConfigAs(NovelanHeatpumpConfiguration.class);
        logger.debug("config address = {}", config.address);
        logger.debug("config port = {}", config.port);
        logger.debug("config refresh = {}", config.refresh);

        List<String> errorMsg = new ArrayList<>();

        if (config.address.trim().isEmpty()) {
            errorMsg.add("Parameter 'address' is mandatory and must be configured");
        }

        connector = new HeatpumpConnector(config.address, config.port);

        if (errorMsg.isEmpty()) {
            ScheduledFuture<?> job = this.refreshJob;
            if (job == null || job.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublishData, 0, config.refresh,
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.join(", ", errorMsg));
        }
    }

    private void updateAndPublishData() {
        String errorMsg;
        try {
            connector.connect();
            // read all parameters
            int[] heatpumpValues = connector.getValues();
            // read all parameters
            int[] heatpumpParams = connector.getParams();

            logger.warn("read data!");
            logger.warn("Thing {}", getThing().getThingTypeUID());
            logger.warn("channels {}", getThing().getChannels());
            getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId())).forEach(channel -> {
                String channelId = channel.getUID().getId();
                logger.warn("channelId:{}", channelId);
                // State state = getValue(channelId, aqiResponse);
                // updateState(channelId, state);
            });

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return;
        } catch (UnknownHostException e) {
            errorMsg = "Configuration is incorrect. the given hostname of the Novelan heatpump is unknown";
            logger.warn("Error running aqicn.org request: {}", errorMsg);
        } catch (IOException e) {
            errorMsg = e.getMessage();
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
    }
}
