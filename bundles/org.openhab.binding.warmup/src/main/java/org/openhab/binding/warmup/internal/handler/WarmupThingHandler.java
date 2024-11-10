/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link WarmupThingHandler} is a super class for Things related to the Bridge consolidating logic.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class WarmupThingHandler extends BaseThingHandler {

    public WarmupThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();

        if (command instanceof RefreshType && bridgeHandler != null) {
            bridgeHandler.refreshFromCache();
        }
    }

    protected void refreshFromServer() {
        final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler != null) {
            bridgeHandler.refreshFromServer();
        }
    }

    protected @Nullable MyWarmupAccountHandler getBridgeHandler() {
        final Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        } else {
            return (MyWarmupAccountHandler) bridge.getHandler();
        }
    }

    /**
     *
     * @param temperature value returned from the API as a String * 10. i.e. "215" = 21.5 degrees C
     * @return the temperature as a {@link QuantityType}
     */
    protected State parseTemperature(@Nullable String temperature) {
        try {
            return temperature != null ? parseTemperature(Integer.parseInt(temperature)) : UnDefType.UNDEF;
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
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
     * @param temperature {@link QuantityType} a temperature
     * @return the temperature as an int in degrees C * 10. i.e. 21.5 degrees C = 215
     */
    protected int formatTemperature(QuantityType<Temperature> temperature) {
        return (int) (temperature.toUnit(SIUnits.CELSIUS).doubleValue() * 10);
    }

    /**
     *
     * @param enery value returned from the API as a string "10.5" = 10.5 kWh
     * @return the energy as a {@link QuantityType}
     */
    protected State parseEnergy(@Nullable String energy) {
        try {
            return energy != null ? new QuantityType<>(Float.parseFloat(energy), Units.KILOWATT_HOUR) : UnDefType.UNDEF;
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }
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
        return value != null ? new QuantityType<>(value, Units.MINUTE) : UnDefType.UNDEF;
    }
}
