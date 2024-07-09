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
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_FEATURE_ON_OFF;

import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.Circuit;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an IntelliCenter2 feature. Features are exposed as On/Off, and represent a Circuit.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see Circuit
 */
@SuppressWarnings("UnstableApiUsage")
@NonNullByDefault
public class IntelliCenter2FeatureHandler extends IntelliCenter2ThingHandler<Circuit> {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2FeatureHandler.class);

    public IntelliCenter2FeatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected Circuit queryModel(ICProtocol protocol) {
        final String id = getObjectName();
        final ICRequest request = ICRequest.getParamList(null, Circuit.createRefreshRequest(id));
        final Future<ICResponse> response = protocol.submit(request);
        return new Circuit(getUnchecked(response).getObjectList().get(0));
    }

    @Override
    protected void updateState(Circuit model) {
        updateState(CHANNEL_FEATURE_ON_OFF, OnOffType.from(model.isOn()));
    }

    @Override
    protected Circuit createFromResponse(ResponseObject response) {
        return new Circuit(response);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_FEATURE_ON_OFF:
                if (command instanceof OnOffType) {
                    var request = ICRequest.setParamList(
                            new RequestObject(getObjectName(), Map.of(Attribute.STATUS, command.toString())));
                    getProtocol().submit(request);
                }
                break;
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, Circuit model) {
        switch (channelUID.getId()) {
            case CHANNEL_FEATURE_ON_OFF:
                updateState(channelUID, OnOffType.from(model.isOn()));
                break;
            default:
                break;
        }
    }

    @Override
    @Nullable
    protected String toChannelId(Attribute a) {
        switch (a) {
            case STATUS:
                return CHANNEL_FEATURE_ON_OFF;
            default:
                return null;
        }
    }
}
