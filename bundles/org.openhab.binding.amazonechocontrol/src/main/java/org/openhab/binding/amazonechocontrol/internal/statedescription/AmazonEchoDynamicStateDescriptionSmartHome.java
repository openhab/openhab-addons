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
package org.openhab.binding.amazonechocontrol.internal.statedescription;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.smarthome.SmartHomeDeviceHandler;
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

@Component(service = { DynamicStateDescriptionProvider.class, AmazonEchoDynamicStateDescriptionSmartHome.class })
@NonNullByDefault
public class AmazonEchoDynamicStateDescriptionSmartHome implements DynamicStateDescriptionProvider {

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
        ThingUID accountThingId = thing.getBridgeUID();
        Thing accountThing = thingRegistry.get(accountThingId);
        if (accountThing == null) {
            return null;
        }
        AccountHandler accountHandler = (AccountHandler) accountThing.getHandler();
        if (accountHandler == null) {
            return null;
        }
        Connection connection = accountHandler.findConnection();
        if (connection == null || !connection.getIsLoggedIn()) {
            return null;
        }
        return (SmartHomeDeviceHandler) thing.getHandler();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
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
