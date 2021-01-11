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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WundergroundUpdateReceiverHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
public class WundergroundUpdateReceiverHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverHandler.class);
    private final WundergroundUpdateReceiverServlet wundergroundUpdateReceiverServlet;
    private final ChannelUID lastReceivedChannel;
    private final ChannelUID queryStateChannel;
    private final ChannelUID queryTriggerChannel;
    private WundergroundUpdateReceiverConfiguration config = new WundergroundUpdateReceiverConfiguration();

    public String getStationId() {
        return config.stationId;
    }

    public WundergroundUpdateReceiverHandler(Thing thing,
            @Nullable WundergroundUpdateReceiverServlet wunderGroundUpdateReceiverServlet) {
        super(thing);
        lastReceivedChannel = new ChannelUID(new ChannelGroupUID(getThing().getUID(), "metadata"),
                LAST_RECEIVED_DATETIME);
        queryTriggerChannel = new ChannelUID(new ChannelGroupUID(getThing().getUID(), "metadata"),
                LAST_QUERY + "-trigger");
        queryStateChannel = new ChannelUID(new ChannelGroupUID(getThing().getUID(), "metadata"), LAST_QUERY + "-state");
        this.wundergroundUpdateReceiverServlet = Objects.requireNonNull(wunderGroundUpdateReceiverServlet);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Ignoring command {}", command);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        this.wundergroundUpdateReceiverServlet.handlerConfigUpdated(this);
    }

    @Override
    public void initialize() {
        this.config = Objects.requireNonNull(getConfigAs(WundergroundUpdateReceiverConfiguration.class));
        wundergroundUpdateReceiverServlet.addHandler(this);
        if (wundergroundUpdateReceiverServlet.isActive()) {
            updateStatus(ThingStatus.ONLINE);
            logger.info("Wunderground update receiver listening for updates to station id {}", config.stationId);
            return;
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        wundergroundUpdateReceiverServlet.removeHandler(this.getStationId());
        super.dispose();
        logger.info("Wunderground update receiver stopped listening for updates to station id {}", config.stationId);
    }

    public void updateChannelStates(WundergroundUpdateReceiverHandler handler, Map<String, String> channelIds) {
        channelIds.forEach((channelId, state) -> {
            if (WundergroundUpdateReceiverBindingConstants.CHANNEL_KEYS.contains(channelId)) {
                updateChannelState(channelId, state);
            }
        });
        updateState(lastReceivedChannel, StringType.valueOf(UUID.randomUUID().toString()));
        String lastQuery = channelIds.getOrDefault(LAST_QUERY, "");
        if ("".equals(lastQuery)) {
            return;
        }
        updateState(queryStateChannel, StringType.valueOf(lastQuery));
        triggerChannel(queryTriggerChannel, lastQuery);
    }

    public void updateChannelState(String channelId, String[] stateParts) {
        updateChannelState(channelId, String.join("", stateParts));
    }

    public void updateChannelState(String channelId, String state) {
        Optional<Channel> channel = getThing().getChannels().stream()
                .filter(ch -> channelId.equals(ch.getUID().getIdWithoutGroup())).findFirst();
        if (channel.isPresent()) {
            ChannelUID channelUID = channel.get().getUID();
            @Nullable
            Float numberValue = null;
            try {
                numberValue = Float.valueOf(state);
            } catch (NumberFormatException ignored) {
            }

            if (numberValue == null) {
                updateState(channelUID, StringType.valueOf(state));
                return;
            }
            @Nullable
            Unit<? extends Quantity<?>> unit = CHANNEL_UNIT_MAPPING.get(channelId);
            if (unit != null) {
                updateState(channelUID, new QuantityType<>(numberValue, unit));
            } else {
                updateState(channelUID, new DecimalType(numberValue));
            }
        }
    }
}
