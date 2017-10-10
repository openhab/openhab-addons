/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
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

    public AirVisualNodeHandler(@NonNull Thing thing) {
        super(thing);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AirVisual Node handler");

        AirVisualNodeConfig config = getConfigAs(AirVisualNodeConfig.class);

        if (config.address == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node address must be set");
        }
        this.nodeAddress = config.address;

        this.nodeUsername = config.username;

        if (config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Node password must be set");
        }
        this.nodePassword = config.password;

        this.nodeShareName = config.share;

        this.refreshInterval = config.refresh * 1000L;

        schedulePoll();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Node poll scheduled");
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
        stopPoll();
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                poll();
            }
        }, 500, refreshInterval, TimeUnit.MILLISECONDS);
    }

    void poll() {
        try {
            logger.debug("Polling for state");
            pollNode();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("Could not connect to Node", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error connecting to Node", e);
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
        String url = "smb://" + nodeAddress +"/" + nodeShareName + "/" + NODE_JSON_FILE;
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, nodeUsername, nodePassword);
        try (SmbFileInputStream in = new SmbFileInputStream(new SmbFile(url, auth))) {
            return IOUtils.toString(new SmbFileInputStream(new SmbFile(url, auth)), StandardCharsets.UTF_8.name());
        }
    }

    private void updateChannel(String channelId, boolean force) {
        if (force || isLinked(channelId)) {
            Object value = getChannelValue(channelId, nodeData);

            // Get channel value
            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof Calendar) {
                state = new DateTimeType((Calendar) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof Float) {
                state = new DecimalType(BigDecimal.valueOf(((Float) value).floatValue()).setScale(1,
                        RoundingMode.HALF_UP));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }

            // Update the channel
            if (state != null) {
                logger.debug("Update channel {} with state {} ({})", channelId, state.toString(),
                        (value == null) ? "null" : value.getClass().getSimpleName());
                updateState(channelId, state);
            }
        }
    }

    private Object getChannelValue(String fullChannelId, NodeData nodeData) {
        Object value = null;

        if (nodeData != null) {
            String[] fullChannelIdFields = StringUtils.split(fullChannelId, "#");

            String channelId = fullChannelIdFields[1];

            switch (channelId) {
                case CHANNEL_CO2_PPM:
                    value = nodeData.getMeasurements().getCo2Ppm();
                    break;
                case CHANNEL_HUMIDITY:
                    value = nodeData.getMeasurements().getHumidityRH();
                    break;
                case CHANNEL_AQI_CN:
                    value = nodeData.getMeasurements().getPm25AQICN();
                    break;
                case CHANNEL_AQI_US:
                    value = nodeData.getMeasurements().getPm25AQIUS();
                    break;
                case CHANNEL_PM_25:
                    value = nodeData.getMeasurements().getPm25Ugm3();
                    break;
                case CHANNEL_TEMP_CELSIUS:
                    value = nodeData.getMeasurements().getTemperatureC();
                    break;
                case CHANNEL_TEMP_FAHRENHEIT:
                    value = nodeData.getMeasurements().getTemperatureF();
                    break;
                case CHANNEL_BATTERY_LEVEL:
                    value = nodeData.getStatus().getBattery();
                    break;
                case CHANNEL_WIFI_STRENGTH:
                    value = nodeData.getStatus().getWifiStrength();
                    break;
                case CHANNEL_TIMESTAMP:
                    // It seem the Node timestamp is Unix timestamp converted from UTC time plus timezone offset.
                    // Not sure about DST though, but it's best guess at now
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(nodeData.getStatus().getDatetime() * 1000L);
                    TimeZone tzSettings = TimeZone.getTimeZone(nodeData.getSettings().getTimezone());
                    cal.add(Calendar.MILLISECOND, -tzSettings.getRawOffset());
                    if (tzSettings.inDaylightTime(cal.getTime())) {
                        cal.add(Calendar.MILLISECOND, -cal.getTimeZone().getDSTSavings());
                    }
                    value = cal;
                    break;
                case CHANNEL_USED_MEMORY:
                    value = nodeData.getStatus().getUsedMemory();
                    break;
            }
        }

        return value;
    }

}
