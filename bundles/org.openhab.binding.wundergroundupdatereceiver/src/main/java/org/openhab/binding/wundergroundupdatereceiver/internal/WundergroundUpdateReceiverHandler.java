/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
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

    public String getStationId() {
        return config.stationId;
    }

    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverHandler.class);
    private final WundergroundUpdateReceiverServlet wundergroundUpdateReceiverServlet;
    private final WundergroundUpdateReceiverDiscoveryService discoveryService;
    private final WundergroundUpdateReceiverUnknownChannelTypeProvider channelTypeProvider;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final ManagedThingProvider managedThingProvider;

    private final ChannelUID dateutcDatetimeChannel;
    private final ChannelUID lastReceivedChannel;
    private final ChannelUID queryStateChannel;
    private final ChannelUID queryTriggerChannel;

    private WundergroundUpdateReceiverConfiguration config = new WundergroundUpdateReceiverConfiguration();

    public WundergroundUpdateReceiverHandler(Thing thing,
            WundergroundUpdateReceiverServlet wunderGroundUpdateReceiverServlet,
            WundergroundUpdateReceiverDiscoveryService discoveryService,
            WundergroundUpdateReceiverUnknownChannelTypeProvider channelTypeProvider,
            ChannelTypeRegistry channelTypeRegistry, ManagedThingProvider managedThingProvider) {
        super(thing);
        this.discoveryService = discoveryService;
        this.channelTypeProvider = channelTypeProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.managedThingProvider = managedThingProvider;

        final ChannelGroupUID metadatGroupUID = new ChannelGroupUID(getThing().getUID(), METADATA_GROUP);

        this.dateutcDatetimeChannel = new ChannelUID(metadatGroupUID, DATEUTC_DATETIME);
        this.lastReceivedChannel = new ChannelUID(metadatGroupUID, LAST_RECEIVED);
        this.queryTriggerChannel = new ChannelUID(metadatGroupUID, LAST_QUERY_TRIGGER);
        this.queryStateChannel = new ChannelUID(metadatGroupUID, LAST_QUERY_STATE);

        this.wundergroundUpdateReceiverServlet = Objects.requireNonNull(wunderGroundUpdateReceiverServlet);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Ignoring command {}", command);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        this.wundergroundUpdateReceiverServlet.handlerConfigUpdated(this);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(WundergroundUpdateReceiverConfiguration.class);
        wundergroundUpdateReceiverServlet.addHandler(this);
        @Nullable
        Map<String, String[]> requestParameters = discoveryService.getUnhandledStationRequest(config.stationId);
        if (requestParameters != null && thing.getChannels().isEmpty()) {
            final String[] noValues = new String[0];
            ThingBuilder thingBuilder = editThing();
            List.of(LAST_RECEIVED, LAST_QUERY_TRIGGER, DATEUTC_DATETIME, LAST_QUERY_STATE)
                    .forEach((String channelId) -> buildChannel(thingBuilder, channelId, noValues));
            requestParameters
                    .forEach((String parameter, String[] query) -> buildChannel(thingBuilder, parameter, query));
            updateThing(thingBuilder.build());
        }
        discoveryService.removeUnhandledStationId(config.stationId);
        if (wundergroundUpdateReceiverServlet.isActive()) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Wunderground update receiver listening for updates to station id {}", config.stationId);
            return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                wundergroundUpdateReceiverServlet.getErrorDetail());
    }

    @Override
    public void dispose() {
        wundergroundUpdateReceiverServlet.removeHandler(this.getStationId());
        super.dispose();
        logger.debug("Wunderground update receiver stopped listening for updates to station id {}", config.stationId);
    }

    public void updateChannelStates(Map<String, String> requestParameters) {
        requestParameters.forEach(this::updateChannelState);
        updateState(lastReceivedChannel, new DateTimeType());
        updateState(dateutcDatetimeChannel, safeResolvUtcDateTime(requestParameters.getOrDefault(DATEUTC, NOW)));
        String lastQuery = requestParameters.getOrDefault(LAST_QUERY, "");
        if (lastQuery.isEmpty()) {
            return;
        }
        updateState(queryStateChannel, StringType.valueOf(lastQuery));
        triggerChannel(queryTriggerChannel, lastQuery);
    }

    private void buildChannel(ThingBuilder thingBuilder, String parameter, String... query) {
        @Nullable
        WundergroundUpdateReceiverParameterMapping channelTypeMapping = WundergroundUpdateReceiverParameterMapping
                .getOrCreateMapping(parameter, String.join("", query), channelTypeProvider);
        if (channelTypeMapping == null) {
            return;
        }
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeMapping.channelTypeId);
        if (channelType == null) {
            return;
        }
        ChannelBuilder channelBuilder = ChannelBuilder
                .create(new ChannelUID(thing.getUID(), channelTypeMapping.channelGroup, parameter))
                .withType(channelTypeMapping.channelTypeId).withAcceptedItemType(channelType.getItemType());
        thingBuilder.withChannel(channelBuilder.build());
    }

    private DateTimeType safeResolvUtcDateTime(String dateUtc) {
        if (!dateUtc.isEmpty() && !NOW.equals(dateUtc)) {
            try {
                // Supposedly the format is "yyyy-MM-dd hh:mm:ss" from the device
                return new DateTimeType(ZonedDateTime.parse(dateUtc.replace(" ", "T") + "Z"));
            } catch (Exception ex) {
                logger.warn("The device is submitting unparsable datetime values: {}", dateUtc);
            }
        }
        return new DateTimeType();
    }

    public void updateChannelState(String channelId, String[] stateParts) {
        updateChannelState(channelId, String.join("", stateParts));
    }

    public void updateChannelState(String parameterName, String state) {
        Optional<Channel> channel = getThing().getChannels().stream()
                .filter(ch -> parameterName.equals(ch.getUID().getIdWithoutGroup())).findFirst();
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
            Unit<? extends Quantity<?>> unit = WundergroundUpdateReceiverParameterMapping.getUnit(parameterName);
            if (unit != null) {
                updateState(channelUID, new QuantityType<>(numberValue, unit));
            } else if (LOW_BATTERY.equals(parameterName)) {
                updateState(channelUID, OnOffType.from(state));
            } else {
                updateState(channelUID, new DecimalType(numberValue));
            }
        } else if (this.discoveryService.isDiscovering()
                && !WundergroundUpdateReceiverParameterMapping.isExcluded(parameterName)
                && this.managedThingProvider.get(this.thing.getUID()) != null) {
            ThingBuilder thingBuilder = editThing();
            buildChannel(thingBuilder, parameterName, state);
            updateThing(thingBuilder.build());
        }
    }
}
