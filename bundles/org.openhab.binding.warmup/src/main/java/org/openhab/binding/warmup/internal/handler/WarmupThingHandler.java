/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WarmupThingHandler} is a super class for Things related to the Bridge consolidating logic.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class WarmupThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WarmupThingHandler.class);
    protected @Nullable MyWarmupAccountHandler bridgeHandler;

    public WarmupThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeHandler = (MyWarmupAccountHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                bridgeHandler.refreshFromCache();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && bridgeHandler != null) {
            bridgeHandler.refreshFromCache();
        }
    }

    /**
     *
     * @param temperature value returned from the API as an Integer * 10. i.e. 215 = 21.5 degrees C
     * @return the temperature as a {@link QuantityType}
     */
    protected State parseTemperature(@Nullable Integer temperature) {
        return temperature != null ? new QuantityType<>(temperature / 10.0, SIUnits.CELSIUS) : UnDefType.UNDEF;
    }

    /**
     *
     * @param date value returned from the API in local time, formatted as yyyy-MM-dd HH:mm:ss
     * @param timezone {@link ZoneId} valid timezone from API
     * @return the DateTime as a {@link DateTimeType}
     */
    protected State parseDate(@Nullable String date, @Nullable String timezone) {
        return date != null && timezone != null ? new DateTimeType(ZonedDateTime
                .of(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), ZoneId.of(timezone)))
                : UnDefType.UNDEF;
    }

    /**
     *
     * @param value a string to convert to {@link StringType}
     * @return the string as a {@link StringType}
     */
    protected State parseString(@Nullable String value) {
        return value != null ? new StringType(value) : UnDefType.UNDEF;
    }

    /**
     *
     * @param value an integer to convert to {@link QuantityType} in minutes
     * @return the number of minutes as a {@link QuantityType}
     */
    protected State parseDuration(@Nullable Integer value) {
        return value != null ? new QuantityType<>(value, SmartHomeUnits.MINUTE) : UnDefType.UNDEF;
    }
}
