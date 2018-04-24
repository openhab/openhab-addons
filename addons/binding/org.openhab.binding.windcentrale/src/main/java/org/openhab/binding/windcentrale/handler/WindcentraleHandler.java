/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.KILO;
import static org.openhab.binding.windcentrale.WindcentraleBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.windcentrale.internal.config.MillConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WindcentraleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class WindcentraleHandler extends BaseThingHandler {

    private static final String HOURS_RUN_THIS_YEAR = "hoursRunThisYear";
    private static final String URL_FORMAT = "https://zep-api.windcentrale.nl/production/%d/live?ignoreLoadingBar=true";
    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);

    private final Logger logger = LoggerFactory.getLogger(WindcentraleHandler.class);
    private final JsonParser parser = new JsonParser();

    private @Nullable MillConfig millConfig;
    private @Nullable String millUrl;
    private @Nullable ScheduledFuture<?> pollingJob;

    private final ExpiringCache<@Nullable String> windcentraleCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
        try {
            return millUrl != null ? HttpUtil.executeUrl("GET", millUrl, 5000) : null;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    });

    public WindcentraleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.debug("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Windcentrale handler '{}'", getThing().getUID());

        MillConfig config = getConfig().as(MillConfig.class);

        millConfig = config;
        millUrl = String.format(URL_FORMAT, config.millId);
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, config.refreshInterval, TimeUnit.SECONDS);

        logger.debug("Polling job scheduled to run every {} sec. for '{}'", config.refreshInterval,
                getThing().getUID());

        updateProperty(Thing.PROPERTY_VENDOR, "Windcentrale");
        updateProperty(Thing.PROPERTY_MODEL_ID, "Windmolen");
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Integer.toString(config.millId));
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

        MillConfig config = millConfig;
        String getMillData = windcentraleCache.getValue();

        if (config == null || getMillData == null) {
            return;
        }

        try {
            JsonObject millData = (JsonObject) parser.parse(getMillData);
            logger.trace("Retrieved updated mill data: {}", millData);

            updateState(CHANNEL_WIND_SPEED, new DecimalType(millData.get(CHANNEL_WIND_SPEED).getAsString()));
            updateState(CHANNEL_WIND_DIRECTION, new StringType(millData.get(CHANNEL_WIND_DIRECTION).getAsString()));
            updateState(CHANNEL_POWER_TOTAL,
                    new QuantityType<>(millData.get(CHANNEL_POWER_TOTAL).getAsBigDecimal(), KILO(SmartHomeUnits.WATT)));
            updateState(CHANNEL_POWER_PER_WD,
                    new QuantityType<>(
                            millData.get(CHANNEL_POWER_PER_WD).getAsBigDecimal().multiply(new BigDecimal(config.wd)),
                            SmartHomeUnits.WATT));
            updateState(CHANNEL_POWER_RELATIVE,
                    new QuantityType<>(millData.get(CHANNEL_POWER_RELATIVE).getAsBigDecimal(), SmartHomeUnits.PERCENT));
            updateState(CHANNEL_ENERGY,
                    new QuantityType<>(millData.get(CHANNEL_ENERGY).getAsBigDecimal(), SmartHomeUnits.KILOWATT_HOUR));
            updateState(CHANNEL_ENERGY_FC, new QuantityType<>(millData.get(CHANNEL_ENERGY_FC).getAsBigDecimal(),
                    SmartHomeUnits.KILOWATT_HOUR));
            updateState(CHANNEL_RUNTIME,
                    new QuantityType<>(millData.get(HOURS_RUN_THIS_YEAR).getAsBigDecimal(), SmartHomeUnits.HOUR));
            updateState(CHANNEL_RUNTIME_PER,
                    new QuantityType<>(millData.get(CHANNEL_RUNTIME_PER).getAsBigDecimal(), SmartHomeUnits.PERCENT));
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType(millData.get(CHANNEL_LAST_UPDATE).getAsString()));

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to process windmill data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Failed to process mill data");
        }
    }
}
