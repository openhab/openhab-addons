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
import org.openhab.binding.ws980wifi.internal.discovery.ws980wifi;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.units.indriya.unit.Units;

/**
 * The {@link ws980wifiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joerg Dokupil - Initial contribution
 */
@NonNullByDefault
public class ws980wifiHandler extends BaseThingHandler {

    private final Logger log = LoggerFactory.getLogger(ws980wifiHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable ws980wifiConfiguration config;
    private String host = "";
    private String port = "";

    public ws980wifiHandler(Thing thing) {
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
            config = getConfigAs(ws980wifiConfiguration.class);
            host = config.getHost();
            port = config.getPort();

            pollingJob = scheduler.scheduleWithFixedDelay(this::updateWeatherData, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
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
        ws980wifi wsObject = new ws980wifi(host, port);
        log.debug("wsObject for refresh created with {}, {}", wsObject.getHost(), wsObject.getPort());

        if (wsObject.refreshValues()) {
            updateState("tempInside", new QuantityType<>(wsObject.tempInside, SIUnits.CELSIUS));
            updateState("tempOutside", new QuantityType<>(wsObject.tempOutside, SIUnits.CELSIUS));
            updateState("tempDewPoint", new QuantityType<>(wsObject.tempDewPoint, SIUnits.CELSIUS));
            updateState("tempWindChill", new QuantityType<>(wsObject.tempWindChill, SIUnits.CELSIUS));
            updateState("heatIndex", new QuantityType<>(wsObject.heatIndex, SIUnits.CELSIUS));
            updateState("humidityInside", new QuantityType<>(wsObject.humidityInside, Units.PERCENT));
            updateState("humidityOutside", new QuantityType<>(wsObject.humidityOutside, Units.PERCENT));
            updateState("pressureAbsolut", new QuantityType<>(wsObject.pressureAbsolut, SIUnits.PASCAL));
            updateState("pressureRelative", new QuantityType<>(wsObject.pressureRelative, SIUnits.PASCAL));
            updateState("windDirection",
                    new QuantityType<>(wsObject.windDirection, org.openhab.core.library.unit.Units.DEGREE_ANGLE));
            updateState("windSpeed", new QuantityType<>(wsObject.windSpeed, Units.KILOMETRE_PER_HOUR));
            updateState("windSpeedGust", new QuantityType<>(wsObject.windSpeedGust, Units.KILOMETRE_PER_HOUR));
            updateState("rainLastHour",
                    new QuantityType<>(wsObject.rainLastHour, org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("rainLastDay",
                    new QuantityType<>(wsObject.rainLastDay, org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("rainLastWeek",
                    new QuantityType<>(wsObject.rainLastWeek, org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("rainLastMonth", new QuantityType<>(wsObject.rainLastMonth,
                    org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("rainLastYear",
                    new QuantityType<>(wsObject.rainLastYear, org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("rainTotal",
                    new QuantityType<>(wsObject.rainTotal, org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR));
            updateState("lightLevel", new QuantityType<>(wsObject.lightLevel, org.openhab.core.library.unit.Units.LUX));
            updateState("uvRaw", new QuantityType<>(wsObject.uvRaw, org.openhab.core.library.unit.Units.IRRADIANCE));
            updateState("uvIndex",
                    new QuantityType<>(wsObject.uvIndex, org.openhab.core.library.unit.Units.IRRADIANCE));
            log.debug("refreshValues successfully done");
        } else {
            log.debug("refreshValues stops with Error");
        }
    }
}
