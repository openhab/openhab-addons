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
package org.openhab.binding.networkupstools.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Provider class to provide channel types for user configured channels.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class NUTChannelTypeProvider implements ChannelTypeProvider, ThingHandlerService {

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
    public void setThingHandler(@Nullable final ThingHandler handler) {
        if (handler instanceof NUTHandler nutHandler) {
            this.handler = handler;
            nutHandler.setChannelTypeProvider(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
