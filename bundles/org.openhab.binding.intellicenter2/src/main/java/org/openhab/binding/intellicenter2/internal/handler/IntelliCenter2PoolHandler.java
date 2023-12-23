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
package org.openhab.binding.intellicenter2.internal.handler;

import static com.google.common.util.concurrent.Futures.getUnchecked;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_CURRENT_TEMPERATURE;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_HEATER_STATUS;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_TARGET_TEMPERATURE;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.Body;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an IntelliCenter body or pool. This represents a body of water.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see Body
 */
@SuppressWarnings("UnstableApiUsage")
@NonNullByDefault
public class IntelliCenter2PoolHandler extends IntelliCenter2ThingHandler<Body> {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2PoolHandler.class);

    public IntelliCenter2PoolHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateState(Body pool) {
        updateState(CHANNEL_HEATER_STATUS, OnOffType.from(pool.isHeating()));
        updateState(CHANNEL_CURRENT_TEMPERATURE, new DecimalType(pool.getCurrentTemperature()));
        updateState(CHANNEL_TARGET_TEMPERATURE, new DecimalType(pool.getTargetTemperature()));
    }

    @Override
    protected Body queryModel(ICProtocol protocol) {
        final String id = getObjectName();
        final ICRequest request = ICRequest.getParamList(null, Body.createRefreshRequest(id));
        final Future<ICResponse> response = protocol.submit(request);
        return new Body(getUnchecked(response).getObjectList().get(0));
    }

    @Override
    protected Body createFromResponse(ResponseObject response) {
        return new Body(response);
    }

    @Override
    protected void updateState(ChannelUID channelUID, Body pool) {
        switch (channelUID.getId()) {
            case CHANNEL_CURRENT_TEMPERATURE:
                updateState(channelUID, new DecimalType(pool.getCurrentTemperature()));
                break;
            case CHANNEL_TARGET_TEMPERATURE:
                updateState(channelUID, new DecimalType(pool.getTargetTemperature()));
                break;
            case CHANNEL_HEATER_STATUS:
                updateState(channelUID, OnOffType.from(pool.isHeating()));
                break;
            default:
                logger.error("Unable to update state for {}", channelUID);
        }
    }

    @Override
    @Nullable
    protected String toChannelId(Attribute a) {
        switch (a) {
            case LSTTMP:
                return CHANNEL_CURRENT_TEMPERATURE;
            case LOTMP:
                return CHANNEL_TARGET_TEMPERATURE;
            case HTMODE:
                return CHANNEL_HEATER_STATUS;
            default:
                return null;
        }
    }
}
