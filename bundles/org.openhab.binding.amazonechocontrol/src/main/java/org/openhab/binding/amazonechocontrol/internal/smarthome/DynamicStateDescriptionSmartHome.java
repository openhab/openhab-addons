/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * Dynamic channel state description provider
 * Overrides the state description for the colors of the smart bulbs
 *
 * @author Lukas Knoeller
 *
 */

@Component(service = { DynamicStateDescriptionProvider.class, DynamicStateDescriptionSmartHome.class })
@NonNullByDefault
public class DynamicStateDescriptionSmartHome implements DynamicStateDescriptionProvider {

    private @Nullable ThingRegistry thingRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable SmartHomeDeviceHandler findHandler(Channel channel) {
        ThingRegistry thingRegistry = this.thingRegistry;
        if (thingRegistry == null) {
            return null;
        }
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }
        ThingHandler handler = thing.getHandler();
        if (!(handler instanceof SmartHomeDeviceHandler)) {
            return null;
        }
        SmartHomeDeviceHandler smartHomeHandler = (SmartHomeDeviceHandler) handler;
        return smartHomeHandler;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {

        if (!BINDING_ID.equals(channel.getChannelTypeUID().getBindingId())) {
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
