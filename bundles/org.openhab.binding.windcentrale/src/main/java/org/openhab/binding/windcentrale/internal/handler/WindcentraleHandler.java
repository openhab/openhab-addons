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
package org.openhab.binding.windcentrale.internal.handler;

import static org.openhab.binding.windcentrale.internal.WindcentraleBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.windcentrale.internal.config.MillConfig;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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

        final MillConfig config = getConfig().as(MillConfig.class);

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
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    private synchronized void updateData() {
        try {
            logger.debug("Update windmill data '{}'", getThing().getUID());

            final MillConfig config = millConfig;
            final String rawMillData = windcentraleCache.getValue();

            if (config == null || rawMillData == null) {
                return;
            }
            logger.trace("Retrieved updated mill data: {}", rawMillData);
            final JsonElement jsonElement = JsonParser.parseString(rawMillData);

            if (!(jsonElement instanceof JsonObject)) {
                throw new JsonParseException("Could not parse windmill json data");
            }
            final JsonObject millData = (JsonObject) jsonElement;

            updateState(CHANNEL_WIND_SPEED, new DecimalType(millData.get(CHANNEL_WIND_SPEED).getAsString()));
            updateState(CHANNEL_WIND_DIRECTION, new StringType(millData.get(CHANNEL_WIND_DIRECTION).getAsString()));
            updateState(CHANNEL_POWER_TOTAL,
                    new QuantityType<>(millData.get(CHANNEL_POWER_TOTAL).getAsBigDecimal(), KILO(Units.WATT)));
            updateState(CHANNEL_POWER_PER_WD,
                    new QuantityType<>(
                            millData.get(CHANNEL_POWER_PER_WD).getAsBigDecimal().multiply(new BigDecimal(config.wd)),
                            Units.WATT));
            updateState(CHANNEL_POWER_RELATIVE,
                    new QuantityType<>(millData.get(CHANNEL_POWER_RELATIVE).getAsBigDecimal(), Units.PERCENT));
            updateState(CHANNEL_ENERGY,
                    new QuantityType<>(millData.get(CHANNEL_ENERGY).getAsBigDecimal(), Units.KILOWATT_HOUR));
            updateState(CHANNEL_ENERGY_FC,
                    new QuantityType<>(millData.get(CHANNEL_ENERGY_FC).getAsBigDecimal(), Units.KILOWATT_HOUR));
            updateState(CHANNEL_RUNTIME,
                    new QuantityType<>(millData.get(HOURS_RUN_THIS_YEAR).getAsBigDecimal(), Units.HOUR));
            updateState(CHANNEL_RUNTIME_PER,
                    new QuantityType<>(millData.get(CHANNEL_RUNTIME_PER).getAsBigDecimal(), Units.PERCENT));
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType(millData.get(CHANNEL_LAST_UPDATE).getAsString()));

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (final RuntimeException e) {
            logger.debug("Failed to process windmill data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.mill-data-error");
        }
    }
}
