/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.MAX_TIME_SECONDS;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public abstract class OpenSprinklerBaseHandler extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected BigDecimal nextDurationTime = MAX_TIME_SECONDS;

    @Nullable
    OpenSprinklerHttpBridgeHandler bridgeHandler;

    public OpenSprinklerBaseHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable OpenSprinklerApi getApi() {
        OpenSprinklerHttpBridgeHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return null;
        }
        try {
            return localBridge.getApi();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return null;
        }
    }

    public void updateChannels() {
        this.getThing().getChannels().forEach(channel -> {
            updateChannel(channel.getUID());
        });
        if (getApi() != null) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void handleNextDurationCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof QuantityType<?>)) {
            logger.warn("Ignoring implausible non-QuantityType command for NEXT_DURATION");
            return;
        }
        QuantityType<?> quantity = (QuantityType<?>) command;
        quantity = quantity.toUnit(Units.SECOND);
        if (quantity != null) {
            nextDurationTime = quantity.toBigDecimal();
            updateState(channelUID, quantity);
        }
    }

    protected BigDecimal nextDurationValue() {
        return nextDurationTime;
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No HTTP Bridge thing selected");
            return;
        }
        bridgeHandler = (OpenSprinklerHttpBridgeHandler) bridge.getHandler();
        updateStatus(ThingStatus.ONLINE);
    }

    protected abstract void updateChannel(ChannelUID uid);
}
