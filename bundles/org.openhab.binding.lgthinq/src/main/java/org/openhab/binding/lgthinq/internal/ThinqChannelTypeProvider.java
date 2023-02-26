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
package org.openhab.binding.lgthinq.internal;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.handler.LGThinQAbstractDeviceHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Provider class to provide channel types for user configured channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ThinqChannelTypeProvider implements ChannelTypeProvider, ThingHandlerService {

    private final Map<ChannelTypeUID, ChannelType> map = new HashMap<>();
    private @Nullable ThingHandler handler;

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable final Locale locale) {
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, @Nullable final Locale locale) {
        return map.get(channelTypeUID);
    }

    /**
     * Add a channel type for a user configured channel.
     *
     * @param channelType channelType
     */
    public void addChannelType(final ChannelType channelType) {
        map.put(channelType.getUID(), channelType);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof LGThinQAbstractDeviceHandler) {
            this.handler = handler;
            ((LGThinQAbstractDeviceHandler) handler).setChannelTypeProvider(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
