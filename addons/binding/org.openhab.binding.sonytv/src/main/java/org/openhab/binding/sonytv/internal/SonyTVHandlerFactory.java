/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.sonytv.SonyTVBindingConstants;
import org.openhab.binding.sonytv.handler.SonyTVHandler;

/**
 * The {@link SonyTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class SonyTVHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SonyTVBindingConstants.THING_TYPE_BRAVIA);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(SonyTVBindingConstants.THING_TYPE_BRAVIA)) {
            return new SonyTVHandler(thing, discoveryServiceRegistry);
        }
        return null;
    }

    protected void setDiscoveryServiceRegistry(final DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(final DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

}