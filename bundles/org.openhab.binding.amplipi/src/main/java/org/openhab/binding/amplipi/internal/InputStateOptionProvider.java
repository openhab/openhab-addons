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
package org.openhab.binding.amplipi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.model.Stream;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;

/**
 * This class provides the list of valid inputs for the input channel of a source.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class InputStateOptionProvider extends BaseDynamicStateDescriptionProvider implements ThingHandlerService {

    private @Nullable AmpliPiHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AmpliPiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("local", "RCA"));
        if (typeUID != null && AmpliPiBindingConstants.CHANNEL_INPUT.equals(typeUID.getId()) && handler != null) {
            List<Stream> streams = handler.getStreams();
            for (Stream stream : streams) {
                options.add(new StateOption("stream=" + stream.getId(), getLabel(stream)));
            }
            setStateOptions(channel.getUID(), options);
        }
        return super.getStateDescription(channel, original, locale);
    }

    private @Nullable String getLabel(Stream stream) {
        if (stream.getType().equals("internetradio")) {
            return stream.getName();
        } else {
            return stream.getType().substring(0, 1).toUpperCase() + stream.getType().substring(1);
        }
    }
}
