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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.BINDING_ID;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * Dynamic channel state description provider
 * Overrides the state description for the colors of the smart bulbs
 *
 * @author Lukas Knoeller - Initial contribution
 *
 */

@Component(service = { DynamicStateDescriptionProvider.class, DynamicStateDescriptionSmartHome.class })
@NonNullByDefault
public class DynamicStateDescriptionSmartHome implements DynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public DynamicStateDescriptionSmartHome(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable SmartHomeDeviceHandler findHandler(Channel channel) {
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }
        ThingHandler handler = thing.getHandler();
        if (handler instanceof SmartHomeDeviceHandler smartHomeHandler) {
            return smartHomeHandler;
        }
        return null;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null || !BINDING_ID.equals(channelTypeUID.getBindingId())) {
            return null;
        }
        if (originalStateDescription == null) {
            return null;
        }
        SmartHomeDeviceHandler handler = findHandler(channel);
        if (handler != null) {
            return handler.findStateDescription(channel, originalStateDescription, locale);
        }
        return null;
    }
}
