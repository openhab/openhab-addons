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
package org.openhab.binding.ws980wifi.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ws980wifi.internal.discovery.WS980WiFi;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WS980WiFiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joerg Dokupil - Initial contribution
 */
@NonNullByDefault
public class WS980WiFiHandler extends BaseThingHandler {

    private final Logger log = LoggerFactory.getLogger(WS980WiFiHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable WS980WiFiConfiguration config;
    private String host = "";
    private String port = "";

    public WS980WiFiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // sensor things cannot send any messages, hence they are not allowed to handle any command
        // The only possible command would be "Refresh"
    }

    @Override
    public void initialize() {
        Thing thing = getThing();
        if (thing == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Thing is NULL");
        } else {
            config = getConfigAs(WS980WiFiConfiguration.class);
            host = config.getHost();
            port = config.getPort();
            updateStatus(ThingStatus.UNKNOWN);
            pollingJob = scheduler.scheduleWithFixedDelay(this::updateWeatherData, 0, config.getRefreshInterval(),
                    TimeUnit.SECONDS);
            log.debug("ws980wifi Handler is initialized");
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
            log.debug("ws980wifi Handler is disposed");
        }
    }

    private void updateWeatherData() {
        log.debug("updateWeatherData started by pollingJob");
        WS980WiFi wsObject = new WS980WiFi(host, port);
        log.debug("wsObject for refresh created with {}, {}", wsObject.getHost(), wsObject.getPort());

        if (wsObject.refreshValues()) {
            updateStatus(ThingStatus.ONLINE);
            updateState(WS980WiFiBindingConstants.CHANNEL_TEMPERATURE_INSIDE,
                    new QuantityType<>(wsObject.tempInside, SIUnits.CELSIUS));
            updateState(WS980WiFiBindingConstants.CHANNEL_TEMPERATURE_OUTSIDE,
                    new QuantityType<>(wsObject.tempOutside, SIUnits.CELSIUS));
            updateState(WS980WiFiBindingConstants.CHANNEL_TEMPERATURE_DEWPOINT,
                    new QuantityType<>(wsObject.tempDewPoint, SIUnits.CELSIUS));
            updateState(WS980WiFiBindingConstants.CHANNEL_TEMPERATURE_WINDCHILL,
                    new QuantityType<>(wsObject.tempWindChill, SIUnits.CELSIUS));
            updateState(WS980WiFiBindingConstants.CHANNEL_TEMPERATURE_HEATINDEX,
                    new QuantityType<>(wsObject.heatIndex, SIUnits.CELSIUS));
            updateState(WS980WiFiBindingConstants.CHANNEL_HUMIDITY_INSIDE,
                    new QuantityType<>(wsObject.humidityInside, Units.PERCENT));
            updateState(WS980WiFiBindingConstants.CHANNEL_HUMIDITY_OUTSIDE,
                    new QuantityType<>(wsObject.humidityOutside, Units.PERCENT));
            updateState(WS980WiFiBindingConstants.CHANNEL_PRESSURE_ABSOLUT,
                    new QuantityType<>(wsObject.pressureAbsolut, SIUnits.PASCAL));
            updateState(WS980WiFiBindingConstants.CHANNEL_PRESSURE_RELATIVE,
                    new QuantityType<>(wsObject.pressureRelative, SIUnits.PASCAL));
            updateState(WS980WiFiBindingConstants.CHANNEL_WIND_DIRECTION,
                    new QuantityType<>(wsObject.windDirection, Units.DEGREE_ANGLE));
            updateState(WS980WiFiBindingConstants.CHANNEL_WINDSPEED,
                    new QuantityType<>(wsObject.windSpeed, SIUnits.KILOMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_WINDSPEED_GUST,
                    new QuantityType<>(wsObject.windSpeedGust, SIUnits.KILOMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_LAST_HOUR,
                    new QuantityType<>(wsObject.rainLastHour, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_LAST_DAY,
                    new QuantityType<>(wsObject.rainLastDay, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_LAST_WEEK,
                    new QuantityType<>(wsObject.rainLastWeek, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_LAST_MONTH,
                    new QuantityType<>(wsObject.rainLastMonth, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_LAST_YEAR,
                    new QuantityType<>(wsObject.rainLastYear, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_RAIN_TOTAL,
                    new QuantityType<>(wsObject.rainTotal, Units.MILLIMETRE_PER_HOUR));
            updateState(WS980WiFiBindingConstants.CHANNEL_LIGTH_LEVEL,
                    new QuantityType<>(wsObject.lightLevel, Units.LUX));
            updateState(WS980WiFiBindingConstants.CHANNEL_UV_RAW, new QuantityType<>(wsObject.uvRaw, Units.IRRADIANCE));
            updateState(WS980WiFiBindingConstants.CHANNEL_UV_INDEX,
                    new QuantityType<>(wsObject.uvIndex, Units.IRRADIANCE));
            log.debug("refreshValues successfully done");
        } else {
            log.debug("refreshValues stops with Error");
            updateStatus(ThingStatus.OFFLINE);
            getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
        }
    }
}
