/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.handler;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.swagger.codegen.api.client.HomeApi;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * Common base class for home-based thing-handler.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHomeThingHandler extends BaseThingHandler {

    public BaseHomeThingHandler(Thing thing) {
        super(thing);
    }

    public @Nullable Long getHomeId() {
        TadoHomeHandler handler = getHomeHandler();
        return handler.getHomeId();
    }

    protected TadoHomeHandler getHomeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new IllegalStateException("Bridge not initialized");
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof TadoHomeHandler)) {
            throw new IllegalStateException("Handler not initialized");
        }
        return (TadoHomeHandler) handler;
    }

    protected HomeApi getApi() {
        TadoHomeHandler handler = getHomeHandler();
        return handler.getApi();
    }

    protected void onSuccessfulOperation() {
        // update without error -> we're back online
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void updateAllAPIChannels(HomeApi api) {
        updateAPIChannels(api.getAPIRateLimit(), TadoBindingConstants.CHANNEL_API_RATE_LIMIT, Units.ONE);
        updateAPIChannels(api.getAPIRateDuration(), TadoBindingConstants.CHANNEL_API_RATE_DURATION, Units.SECOND);
        updateAPIChannels(api.getAPIRateRemaining(), TadoBindingConstants.CHANNEL_API_RATE_REMAINING, Units.ONE);
        updateAPIChannels(api.getAPIRateReset(), TadoBindingConstants.CHANNEL_API_RATE_RESET, Units.SECOND);
    }

    private void updateAPIChannels(@Nullable Integer value, String channelName, Unit unit) {
        if (value != null) {
            if (TadoBindingConstants.CHANNEL_API_RATE_LIMIT.equals(channelName)
                    || TadoBindingConstants.CHANNEL_API_RATE_REMAINING.equals(channelName)) {
                updateState(channelName, new DecimalType(value));
            } else {
                updateState(channelName, new QuantityType<>(value, unit));
            }
        }
    }
}
