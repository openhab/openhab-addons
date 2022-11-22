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
package org.openhab.binding.freeathomesystem.internal.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@Component(service = { FreeAtHomeChannelGroupTypeProvider.class, ChannelGroupTypeProvider.class })
public class FreeAtHomeChannelGroupTypeProviderImpl implements FreeAtHomeChannelGroupTypeProvider {

    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypeByUID = new HashMap<>();

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        Collection<ChannelGroupType> result = new ArrayList<>();
        for (ChannelGroupTypeUID uid : channelGroupTypeByUID.keySet()) {
            result.add(channelGroupTypeByUID.get(uid));
        }
        return result;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return channelGroupTypeByUID.get(channelGroupTypeUID);
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        // method stub
    }
}
