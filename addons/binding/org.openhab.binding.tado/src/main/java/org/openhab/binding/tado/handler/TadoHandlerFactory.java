/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.handler;

import static org.openhab.binding.tado.TadoBindingConstants.*;

import java.util.Hashtable;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.tado.internal.discovery.TadoDiscoveryService;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link TadoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_HOME,
            THING_TYPE_ZONE, THING_TYPE_MOBILE_DEVICE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HOME)) {
            TadoHomeHandler tadoHomeHandler = new TadoHomeHandler((Bridge) thing);

            TadoDiscoveryService discoveryService = new TadoDiscoveryService(tadoHomeHandler);
            bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                    new Hashtable<String, Object>());

            return tadoHomeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new TadoZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MOBILE_DEVICE)) {
            return new TadoMobileDeviceHandler(thing);
        }

        return null;
    }
}
