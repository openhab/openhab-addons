/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dreamscreen.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;

/**
 * Dynamic channel state description provider.
 * Overrides the state description for the controls at runtime.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, DreamScreenDynamicStateDescriptionProvider.class })
public class DreamScreenDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private Map<ChannelUID, StateDescription> descriptions = new ConcurrentHashMap<>();

    public void setChannelDescription(ChannelUID channelUID, StateDescription description) {
        descriptions.put(channelUID, description);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        return descriptions.get(channel.getUID());
    }

    public void removeThingDescriptions(final ThingUID thingUID) {
        descriptions.entrySet().removeIf(e -> thingUID.equals(e.getKey().getThingUID()));
    }
}
