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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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

    protected @Nullable MyWarmupAccountHandler bridgeHandler;

    public WarmupThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeHandler = (MyWarmupAccountHandler) bridge.getHandler();
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && bridgeHandler != null) {
            bridgeHandler.refreshFromCache();
        }
    }

    protected void refreshFromServer() {
        if (bridgeHandler != null) {
            bridgeHandler.refreshFromServer();
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
