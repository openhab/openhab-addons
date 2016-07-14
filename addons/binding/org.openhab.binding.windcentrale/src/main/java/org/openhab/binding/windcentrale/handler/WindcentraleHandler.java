/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale.handler;

import static org.openhab.binding.windcentrale.WindcentraleBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link WindcentraleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class WindcentraleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(WindcentraleHandler.class);

    private static final String BASE_URL = "https://zep-api.windcentrale.nl/production/";
    private URL millUrl;
    private BigDecimal wd = BigDecimal.ONE;
    private ScheduledFuture<?> pollingJob;

    public WindcentraleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Windcentrale handler '{}'", getThing().getUID());

        Object param;

        param = getConfig().get(PROPERTY_MILL_ID);
        int millId;
        if (param instanceof BigDecimal) {
            millId = ((BigDecimal) param).intValue();
        } else {
            millId = 1;
        }

        param = getConfig().get(PROPERTY_QTY_WINDDELEN);
        if (param instanceof BigDecimal) {
            wd = (BigDecimal) param;
        } else {
            wd = BigDecimal.ONE;
        }

        int pollingPeriod = 30;
        param = getConfig().get(PROPERTY_REFRESH_INTERVAL);
        if (param instanceof BigDecimal) {
            pollingPeriod = ((BigDecimal) param).intValue();
        }

        updateProperty(Thing.PROPERTY_VENDOR, "Windcentrale");
        updateProperty(Thing.PROPERTY_MODEL_ID, "Windmolen");
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Integer.toString(millId));

        String urlString = BASE_URL + millId + "/live?ignoreLoadingBar=true";
        try {
            millUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Constructed url '" + urlString + "' is not valid: " + e.getLocalizedMessage());
            return;
        }

        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Windcentrale handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private synchronized void updateData() {
        logger.debug("Update windmill data '{}'", getThing().getUID());

        try {
            String getMillData = getMillData();
            JsonParser parser = new JsonParser();
            JsonObject millData = (JsonObject) parser.parse(getMillData);

            logger.trace("Retrieved updated mill data: {}", millData);

            updateState(CHANNEL_WIND_SPEED, new DecimalType(millData.get(CHANNEL_WIND_SPEED).getAsString()));
            updateState(CHANNEL_WIND_DIRECTION, new StringType(millData.get(CHANNEL_WIND_DIRECTION).getAsString()));
            updateState(CHANNEL_POWER_TOTAL, new DecimalType(millData.get(CHANNEL_POWER_TOTAL).getAsBigDecimal()));
            updateState(CHANNEL_POWER_PER_WD,
                    new DecimalType(millData.get(CHANNEL_POWER_PER_WD).getAsBigDecimal().multiply(wd)));
            updateState(CHANNEL_POWER_RELATIVE,
                    new DecimalType(millData.get(CHANNEL_POWER_RELATIVE).getAsBigDecimal()));
            updateState(CHANNEL_ENERGY, new DecimalType(millData.get(CHANNEL_ENERGY).getAsBigDecimal()));
            updateState(CHANNEL_ENERGY_FC, new DecimalType(millData.get(CHANNEL_ENERGY_FC).getAsBigDecimal()));
            updateState(CHANNEL_RUNTIME, new DecimalType(millData.get(CHANNEL_RUNTIME).getAsBigDecimal()));
            updateState(CHANNEL_RUNTIME_PER, new DecimalType(millData.get(CHANNEL_RUNTIME_PER).getAsBigDecimal()));
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType(millData.get(CHANNEL_LAST_UPDATE).getAsString()));

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Exception e) {
            logger.debug("Failed to update windmill data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private String getMillData() throws IOException {
        try (InputStream connection = millUrl.openStream()) {
            return IOUtils.toString(connection);
        }
    }
}
