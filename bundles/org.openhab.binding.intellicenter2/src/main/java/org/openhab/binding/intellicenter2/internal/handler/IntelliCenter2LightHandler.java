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
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_LIGHT_COLOR;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_LIGHT_POWER;

import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.IntelliBrite;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an IntelliCenter2 IntelliBrite light.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see IntelliBrite
 */
@SuppressWarnings("UnstableApiUsage")
@NonNullByDefault
public class IntelliCenter2LightHandler extends IntelliCenter2ThingHandler<IntelliBrite> {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2LightHandler.class);

    public IntelliCenter2LightHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected IntelliBrite queryModel(ICProtocol protocol) {
        final String id = getObjectName();
        final ICRequest request = ICRequest.getParamList(null, IntelliBrite.createRefreshRequest(id));
        final Future<ICResponse> response = protocol.submit(request);
        return new IntelliBrite(getUnchecked(response).getObjectList().get(0));
    }

    @Override
    protected void updateState(IntelliBrite model) {
        updateState(CHANNEL_LIGHT_POWER, OnOffType.from(model.isOn()));
        updateState(CHANNEL_LIGHT_COLOR, toHSB(model.getColor()));
    }

    @Override
    protected IntelliBrite createFromResponse(ResponseObject response) {
        return new IntelliBrite(response);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        super.handleCommand(channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_LIGHT_POWER:
                if (command instanceof OnOffType) {
                    var request = ICRequest.setParamList(
                            new RequestObject(getObjectName(), Map.of(Attribute.STATUS, command.toString())));
                    getProtocol().submit(request);
                }
                break;
            case CHANNEL_LIGHT_COLOR:
                if (command instanceof HSBType) {
                    // ACT is the value to set, but USE is returned
                    var params = Map.of(Attribute.ACT, toColor(((HSBType) command)).intellicenterCode);
                    var request = ICRequest.setParamList(new RequestObject(getObjectName(), params));
                    getProtocol().submit(request);
                }
                break;
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, IntelliBrite model) {
        switch (channelUID.getId()) {
            case CHANNEL_LIGHT_POWER:
                updateState(channelUID, OnOffType.from(model.isOn()));
                break;
            case CHANNEL_LIGHT_COLOR:
                updateState(channelUID, toHSB(model.getColor()));
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
                return CHANNEL_LIGHT_POWER;
            case ACT:
            case USE:
                return CHANNEL_LIGHT_COLOR;
            case LIMIT:
                // explicit ignore; we subscribe to LIMIT changes, but we don't really care
                return null;
            default:
                return null;
        }
    }

    private static HSBType toHSB(IntelliBrite.Color color) {
        return new HSBType(new DecimalType(color.hue), new PercentType(color.saturation), new PercentType(100));
    }

    private static IntelliBrite.Color toColor(HSBType hsb) {
        int hue = hsb.getHue().intValue();
        int saturation = hsb.getSaturation().intValue();
        return IntelliBrite.Color.from(hue, saturation);
    }
}
