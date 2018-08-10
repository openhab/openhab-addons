/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airvisualnode.handler;

import static org.openhab.binding.airvisualnode.AirVisualNodeBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SIUnits.GRAM;
import static org.eclipse.smarthome.core.library.unit.SIUnits.CUBIC_METRE;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.ONE;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.PERCENT;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.PARTS_PER_MILLION;
import static org.eclipse.smarthome.core.library.unit.MetricPrefix.MICRO;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.airvisualnode.internal.config.AirVisualNodeConfig;
import org.openhab.binding.airvisualnode.internal.json.NodeData;
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
public class AirVisualNodeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AirVisualNodeHandler.class);

    public static final String NODE_JSON_FILE = "latest_config_measurements.json";

    private final Gson gson;

    private ScheduledFuture<?> pollFuture;

    private long refreshInterval;

    private String nodeAddress;

    private String nodeUsername;

    private String nodePassword;

    private String nodeShareName;

    private NodeData nodeData;

    public AirVisualNodeHandler(Thing thing) {
        super(thing);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AirVisual Node handler");

        AirVisualNodeConfig config = getConfigAs(AirVisualNodeConfig.class);

        if (config.address == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node address must be set");
            return;
        }
        this.nodeAddress = config.address;

        this.nodeUsername = config.username;

        if (config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node password must be set");
            return;
        }
        this.nodePassword = config.password;

        this.nodeShareName = config.share;

        this.refreshInterval = config.refresh * 1000L;

        schedulePoll();
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
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(false);
        }
    }

    private synchronized void schedulePoll() {
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 500, refreshInterval, TimeUnit.MILLISECONDS);
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
        NodeData currentNodeData = gson.fromJson(jsonData, NodeData.class);
        if (nodeData == null || currentNodeData.getStatus().getDatetime() > nodeData.getStatus().getDatetime()) {
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
            return IOUtils.toString(in, StandardCharsets.UTF_8.name());
        }
    }

    private void updateChannel(String channelId, boolean force) {
        if (nodeData != null && (force || isLinked(channelId))) {
            State state = getChannelState(channelId, nodeData);
            logger.debug("Update channel {} with state {}", channelId, state);
            updateState(channelId, state);
        }
    }

    private State getChannelState(String channelId, NodeData nodeData) {
        State state = UnDefType.UNDEF;

        // Handle system channel IDs separately, because 'switch/case' expressions must be constant expressions
        if (CHANNEL_BATTERY_LEVEL.equals(channelId)) {
            state = new DecimalType(BigDecimal.valueOf(nodeData.getStatus().getBattery()).longValue());
        } else if (CHANNEL_WIFI_STRENGTH.equals(channelId)) {
            state = new DecimalType(BigDecimal.valueOf(Math.max(0, nodeData.getStatus().getWifiStrength()-1)).longValue());
        } else {
            // Handle binding-specific channel IDs
            switch (channelId) {
                case CHANNEL_CO2:
                    state = new QuantityType<>(nodeData.getMeasurements().getCo2Ppm(), PARTS_PER_MILLION);
                    break;
                case CHANNEL_HUMIDITY:
                    state = new QuantityType<>(nodeData.getMeasurements().getHumidityRH(), PERCENT);
                    break;
                case CHANNEL_AQI_US:
                    state = new QuantityType<>(nodeData.getMeasurements().getPm25AQIUS(), ONE);
                    break;
                case CHANNEL_PM_25:
                    // PM2.5 is in ug/m3
                    state = new QuantityType<>(nodeData.getMeasurements().getPm25Ugm3(), MICRO(GRAM).divide(CUBIC_METRE));
                    break;
                case CHANNEL_TEMP_CELSIUS:
                    state = new QuantityType<>(nodeData.getMeasurements().getTemperatureC(), CELSIUS);
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
