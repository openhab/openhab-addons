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
package org.openhab.binding.tacmi.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Provides all ChannelTypes for the schema binding...
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
@Component(service = { TACmiChannelTypeProvider.class, ChannelTypeProvider.class })
public class TACmiChannelTypeProvider implements ChannelTypeProvider {

    private final Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(channelTypesByUID.values());
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    public @Nullable ChannelType getInternalChannelType(ChannelTypeUID channelTypeUID) {
        return channelTypesByUID.get(channelTypeUID);
    }

    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }
}
