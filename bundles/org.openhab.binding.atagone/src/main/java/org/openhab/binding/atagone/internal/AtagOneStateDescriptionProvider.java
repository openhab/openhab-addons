/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atagone.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;

/**
 * Supplies the DHW target-temperature bounds actually reported by the device
 * ({@code configuration.dhw_min_set}/{@code dhw_max_set}), which vary by installation (a combi boiler's
 * range differs from a system boiler with a 3-port valve kit) and so cannot be hardcoded in
 * {@code thing-types.xml}.
 *
 * @author Florian Lettner - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, AtagOneStateDescriptionProvider.class })
@NonNullByDefault
public class AtagOneStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescription> stateDescriptions = new ConcurrentHashMap<>();

    public void setDescription(ChannelUID channelUID, StateDescription description) {
        stateDescriptions.put(channelUID, description);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        return stateDescriptions.get(channel.getUID());
    }
}
