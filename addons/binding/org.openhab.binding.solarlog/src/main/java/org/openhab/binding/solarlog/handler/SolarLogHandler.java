/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.solarlog.SolarLogBindingConstants;
import org.openhab.binding.solarlog.internal.SolarLogChannel;
import org.openhab.binding.solarlog.internal.SolarLogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SolarLogHandler} is responsible for handling commands, which are
 * sent to one of the channels. It does the "heavy lifting" of connecting to the
 * Solar-Log, getting the data, parsing it and updating the channels.
 *
 * @author Johann Richard - Initial contribution
 */
public class SolarLogHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SolarLogHandler.class);
    private SolarLogConfig config;

    private final int timeout = 5000;

    public SolarLogHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Solar-Log");
        config = getConfigAs(SolarLogConfig.class);
        scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Running refresh cycle");
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
                // Very rudimentary Exception differentiation
            } catch (IOException e) {
                logger.debug("Error reading response from Solar-Log: {}", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error with the device. Please retry later.");
            } catch (JsonSyntaxException je) {
                logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je);
            } catch (Exception e) {
                logger.warn("Error refreshing source {}: {}", getThing().getUID(), e);
            }
        }, 0, config.refreshInterval < 15 ? 15 : config.refreshInterval, TimeUnit.SECONDS); // Minimum interval is 15 s
    }

    private void refresh() throws Exception {
        // Get the JSON - somehow
        logger.trace("Starting refresh handler");
        String httpMethod = "POST";
        String url = config.url + "/getjp";
        String content = "{\"801\":{\"170\":null}}";
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        logger.debug("Attempting to load data from {} with parameter {}", url, content);
        String response = HttpUtil.executeUrl(httpMethod, url, stream, null, timeout);
        JsonElement solarLogDataElement = new JsonParser().parse(response);
        JsonObject solarLogData = solarLogDataElement.getAsJsonObject();

        // Check whether the data is well-formed
        if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_ROOT)) {
            solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_ROOT);
            logger.trace("Found root node in Solar-Log data. Attempting to read data");
            if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES)) {
                solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES);

                for (SolarLogChannel channelConfig : SolarLogChannel.values()) {
                    if (solarLogData.has(channelConfig.getIndex())) {
                        String value = solarLogData.get(channelConfig.getIndex()).getAsString();
                        Channel channel = getThing().getChannel(channelConfig.getId());
                        State state = getState(value, channelConfig);
                        if (channel != null) {
                            logger.trace("Update channel state: {}", state);
                            updateState(channel.getUID(), state);
                        }
                    } else {
                        logger.debug("Error refreshing source {}", getThing().getUID(), channelConfig.getId());
                    }
                }
            }
        } else {
            logger.warn("Data retrieval failed, no data returned {}", response);
        }
    }

    private State getState(String value, SolarLogChannel type) {
        switch (type) {
            // Only DateTime channel
            case CHANNEL_LASTUPDATETIME:
                try {
                    logger.trace("Parsing date {}", value);
                    try {
                        Date date = new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(value);
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");// dd/MM/yyyy
                        String strDate = sdfDate.format(date);

                        logger.trace("Parsing date successful. Returning date. {}", new DateTimeType(strDate));
                        return new DateTimeType(strDate);
                    } catch (ParseException fpe) {
                        logger.trace("Parsing date failed. Returning string.", fpe);
                        return new StringType(value);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Parsing date failed: {}. Returning nothing", e);
                    return UnDefType.UNDEF;
                }
                // All other channels should be numbers
            default:
                try {
                    logger.trace("Parsing number {}", value);
                    return new DecimalType(new BigDecimal(value));
                } catch (NumberFormatException e) {
                    // Log a warning and return UNDEF
                    logger.warn("Parsing number failed: {}. Returning nothing", e);
                    return UnDefType.UNDEF;
                }
        }
    }
}
