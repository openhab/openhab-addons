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
package org.openhab.binding.airvisualnode.internal.handler;

import static org.openhab.binding.airvisualnode.internal.AirVisualNodeBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.MICRO;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.SIUnits.CUBIC_METRE;
import static org.openhab.core.library.unit.SIUnits.GRAM;
import static org.openhab.core.library.unit.Units.ONE;
import static org.openhab.core.library.unit.Units.PARTS_PER_MILLION;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airvisualnode.internal.config.AirVisualNodeConfig;
import org.openhab.binding.airvisualnode.internal.dto.MeasurementsInterface;
import org.openhab.binding.airvisualnode.internal.dto.NodeDataInterface;
import org.openhab.binding.airvisualnode.internal.dto.airvisual.NodeData;
import org.openhab.binding.airvisualnode.internal.dto.airvisualpro.ProNodeData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * The {@link AirVisualNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Victor Antonovich - Initial contribution
 */
@NonNullByDefault
public class AirVisualNodeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AirVisualNodeHandler.class);

    public static final String NODE_JSON_FILE = "latest_config_measurements.json";
    private static final long DELAY_IN_MS = 500;

    private final Gson gson;
    private @Nullable ScheduledFuture<?> pollFuture;
    private long refreshInterval;
    private String nodeAddress = "";
    private String nodeUsername = "";
    private String nodePassword = "";
    private String nodeShareName = "";
    private @Nullable NodeDataInterface nodeData;
    private boolean isProVersion = false;

    public AirVisualNodeHandler(Thing thing) {
        super(thing);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AirVisual Node handler");

        AirVisualNodeConfig config = getConfigAs(AirVisualNodeConfig.class);

        if (config.address.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node address must be set");
            return;
        }
        if (config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node password must be set");
            return;
        }

        this.nodeAddress = config.address;
        this.nodeUsername = config.username;
        this.nodePassword = config.password;
        this.nodeShareName = config.share;
        this.refreshInterval = config.refresh * 1000L;

        try {
            var jsonData = gson.fromJson(getNodeJsonData(), Map.class);
            this.isProVersion = jsonData.get("measurements") instanceof ArrayList;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Can't get node json");
            return;
        }

        if (!this.isProVersion) {
            removeProChannels();
        }

        schedulePoll();
    }

    private void removeProChannels() {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        channels.removeIf(channel -> isProChannel(channel.getLabel()));
        replaceChannels(channels);
    }

    private boolean isProChannel(@Nullable String channelLabel) {
        if (channelLabel == null || channelLabel.isBlank()) {
            return false;
        }
        return "PM0.1".equals(channelLabel) || "PM10".equals(channelLabel);
    }

    private void replaceChannels(List<Channel> channels) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), true);
        } else {
            logger.debug("Can not handle command '{}'", command);
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private synchronized void stopPoll() {
        ScheduledFuture<?> localFuture = pollFuture;
        if (localFuture != null) {
            localFuture.cancel(false);
        }
    }

    private synchronized void schedulePoll() {
        logger.debug("Scheduling poll for {}}ms out, then every {} ms", DELAY_IN_MS, refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, DELAY_IN_MS, refreshInterval, TimeUnit.MILLISECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollNode();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("Could not connect to Node", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void pollNode() throws IOException {
        String jsonData = getNodeJsonData();

        NodeDataInterface currentNodeData;
        if (isProVersion) {
            currentNodeData = gson.fromJson(jsonData, ProNodeData.class);
        } else {
            currentNodeData = gson.fromJson(jsonData, NodeData.class);
        }
        NodeDataInterface localNodeDate = nodeData;
        if (localNodeDate == null
                || currentNodeData.getStatus().getDatetime() > localNodeDate.getStatus().getDatetime()) {
            nodeData = currentNodeData;
            // Update all channels from the updated Node data
            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID().getId(), false);
            }
        }
    }

    private String getNodeJsonData() throws IOException {
        String url = "smb://" + nodeAddress + "/" + nodeShareName + "/" + NODE_JSON_FILE;
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, nodeUsername, nodePassword);
        try (SmbFileInputStream in = new SmbFileInputStream(new SmbFile(url, auth))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void updateChannel(String channelId, boolean force) {
        NodeDataInterface localnodeData = nodeData;
        if (localnodeData != null && (force || isLinked(channelId))) {
            State state = getChannelState(channelId, localnodeData);
            logger.debug("Update channel {} with state {}", channelId, state);
            updateState(channelId, state);
        }
    }

    private State getChannelState(String channelId, NodeDataInterface nodeData) {
        State state = UnDefType.UNDEF;

        // Handle system channel IDs separately, because 'switch/case' expressions must be constant expressions
        if (CHANNEL_BATTERY_LEVEL.equals(channelId)) {
            state = new DecimalType(BigDecimal.valueOf(nodeData.getStatus().getBattery()).longValue());
        } else if (CHANNEL_WIFI_STRENGTH.equals(channelId)) {
            state = new DecimalType(
                    BigDecimal.valueOf(Math.max(0, nodeData.getStatus().getWifiStrength() - 1)).longValue());
        } else {
            MeasurementsInterface measurements = nodeData.getMeasurements();
            // Handle binding-specific channel IDs
            switch (channelId) {
                case CHANNEL_CO2:
                    state = new QuantityType<>(measurements.getCo2Ppm(), PARTS_PER_MILLION);
                    break;
                case CHANNEL_HUMIDITY:
                    state = new QuantityType<>(measurements.getHumidityRH(), PERCENT);
                    break;
                case CHANNEL_AQI_US:
                    state = new QuantityType<>(measurements.getPm25AQIUS(), ONE);
                    break;
                case CHANNEL_PM_25:
                    // PM2.5 is in ug/m3
                    state = new QuantityType<>(measurements.getPm25Ugm3(), MICRO(GRAM).divide(CUBIC_METRE));
                    break;
                case CHANNEL_PM_10:
                    // PM10 is in ug/m3
                    state = new QuantityType<>(measurements.getPm10Ugm3(), MICRO(GRAM).divide(CUBIC_METRE));
                    break;
                case CHANNEL_PM_01:
                    // PM0.1 is in ug/m3
                    state = new QuantityType<>(measurements.getPm01Ugm3(), MICRO(GRAM).divide(CUBIC_METRE));
                    break;
                case CHANNEL_TEMP_CELSIUS:
                    state = new QuantityType<>(measurements.getTemperatureC(), CELSIUS);
                    break;
                case CHANNEL_TIMESTAMP:
                    // It seem the Node timestamp is Unix timestamp converted from UTC time plus timezone offset.
                    // Not sure about DST though, but it's best guess at now
                    Instant instant = Instant.ofEpochMilli(nodeData.getStatus().getDatetime() * 1000L);
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
                    ZoneId zoneId = ZoneId.of(nodeData.getSettings().getTimezone());
                    ZoneRules zoneRules = zoneId.getRules();
                    zonedDateTime.minus(Duration.ofSeconds(zoneRules.getOffset(instant).getTotalSeconds()));
                    if (zoneRules.isDaylightSavings(instant)) {
                        zonedDateTime.minus(Duration.ofSeconds(zoneRules.getDaylightSavings(instant).getSeconds()));
                    }
                    state = new DateTimeType(zonedDateTime);
                    break;
                case CHANNEL_USED_MEMORY:
                    state = new DecimalType(BigDecimal.valueOf(nodeData.getStatus().getUsedMemory()).longValue());
                    break;
            }
        }

        return state;
    }
}
