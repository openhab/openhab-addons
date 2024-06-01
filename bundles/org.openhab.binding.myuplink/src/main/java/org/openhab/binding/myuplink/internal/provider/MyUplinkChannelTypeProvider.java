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
package org.openhab.binding.myuplink.internal.provider;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Provides generated channel-types to the framework
 *
 * @author Alexander Friese - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, MyUplinkChannelTypeProvider.class }, immediate = true)
@NonNullByDefault
public class MyUplinkChannelTypeProvider implements ChannelTypeProvider {

    private final Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    /**
     * Add a channel-type
     *
     * @param type
     */
    public void add(ChannelType type) {
        channelTypes.put(type.getUID(), type);
    }

    /**
     * Remove a channel-type
     *
     * @param uid
     */
    public void remove(ChannelTypeUID uid) {
        channelTypes.remove(uid);
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
    }

    @Deactivate
    protected void deactivate() {
        channelTypes.clear();
    }
}
