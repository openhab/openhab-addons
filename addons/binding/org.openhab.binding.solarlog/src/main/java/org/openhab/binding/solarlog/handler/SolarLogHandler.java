/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.handler;

import java.math.BigDecimal;
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
import org.openhab.binding.solarlog.SolarLogBindingConstants;
import org.openhab.binding.solarlog.internal.HttpUtils;
import org.openhab.binding.solarlog.internal.SolarLogChannel;
import org.openhab.binding.solarlog.internal.SolarLogSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link SolarLogHandler} is responsible for handling commands, which are
 * sent to one of the channels. It does the "heavy lifting" of connecting to the
 * SolarLog, getting the data, parsing it and updating the channels in OpenHab.
 *
 * @author Johann Richard - Initial contribution
 */
public class SolarLogHandler extends BaseThingHandler {

    private SolarLogSourceConfig config;
    private Logger logger = LoggerFactory.getLogger(SolarLogHandler.class);

    public SolarLogHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarLogSourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Communication error with the device");
                    logger.debug("Error refreshing source {}", getThing().getUID(), e);
                }
            }

        }, 0, config.refreshInterval < 15 ? 15 : config.refreshInterval, TimeUnit.SECONDS); // Minimum interval is 15 s
    }

    private void refresh() throws Exception {
        // Get the JSON - somehow
        JsonElement solarLogDataElement = HttpUtils.getSolarLogData(config.url);
        JsonObject solarLogData = solarLogDataElement.getAsJsonObject();

        // Check whether the data is well-formed
        if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_ROOT)) {
            solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_ROOT);
            if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES)) {
                solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES);

                for (SolarLogChannel channelConfig : SolarLogChannel.values()) {
                    if (solarLogData.has(channelConfig.getIndex())) {
                        String value = solarLogData.get(channelConfig.getIndex()).getAsString();
                        Channel channel = getThing().getChannel(channelConfig.getId());
                        State state = getState(value, channelConfig.getType());

                        updateState(channel.getUID(), state);
                    } else {
                        logger.debug("Error refreshing source {}", getThing().getUID(), channelConfig.getId());
                    }
                }
            }
        }

    }

    private State getState(String value, String type) {
        if (type == "Number") {
            try {
                logger.trace("Parsing number {}", value);
                return new DecimalType(new BigDecimal(value));
            } catch (NumberFormatException e) {
                logger.trace("Parsing number failed. Returning string");
                return new StringType(value);
            }
        }

        if (type == "DateTime") {
            try {
                logger.trace("Parsing date " + value);
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
                logger.trace("Parsing date failed. Returning string", e);
                return new StringType(value);
            }
        }
        return new StringType(value);

    }
}
