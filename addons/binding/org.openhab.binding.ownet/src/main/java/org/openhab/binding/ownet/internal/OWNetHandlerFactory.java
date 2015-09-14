/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ownet.internal;

import static org.openhab.binding.ownet.OWNetBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ownet.handler.OW12Handler;
import org.openhab.binding.ownet.handler.OW26Handler;
import org.openhab.binding.ownet.handler.OW28Handler;
import org.openhab.binding.ownet.handler.OW29Handler;
import org.openhab.binding.ownet.handler.OW3AHandler;
import org.openhab.binding.ownet.handler.OWDeviceHandler;
import org.openhab.binding.ownet.handler.OWNetHandler;

import com.google.common.collect.Sets;

/**
 * The {@link ModbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author vores8 - Initial contribution
 */
public class OWNetHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(OWNetHandler.SUPPORTED_THING_TYPES_UIDS,
            OWDeviceHandler.SUPPORTED_THING_TYPES_UIDS);
    private static final Map<String, ThingHandler> ownetThingHandlers = new HashMap<String, ThingHandler>();
    private static final Map<String, OWNetHandler> ownetTcpHandlers = new HashMap<String, OWNetHandler>();
    private DiscoveryService discoveryService = null;

    protected void setDiscoveryService(DiscoveryService service) {
        this.discoveryService = service;
    }

    protected void unsetDiscoveryService(DiscoveryService service) {
        this.discoveryService = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    // @Override
    // public void registerHandler(Thing thing, ThingHandlerCallback thingHandlerListener) {
    // super.registerHandler(thing, thingHandlerListener);
    // }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_TCP)) {
            OWNetHandler handler = new OWNetHandler((Bridge) thing);
            discoveryService.addParticipant(handler.getUID().getAsString(), handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE12)) {
            OWDeviceHandler handler = new OW12Handler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE26)) {
            OWDeviceHandler handler = new OW26Handler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE28)) {
            OWDeviceHandler handler = new OW28Handler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE29)) {
            OWDeviceHandler handler = new OW29Handler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE3A)) {
            OWDeviceHandler handler = new OW3AHandler(thing);
            return handler;
        }

        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        ThingHandler thingHandler = thing.getHandler();
        ThingTypeUID thingTypeUID = thingHandler.getThing().getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_TCP)) {
            discoveryService.removeParticipant(thingHandler.getThing().getUID().getAsString());
        } else if (thingTypeUID.equals(THING_TYPE28) || thingTypeUID.equals(THING_TYPE12)
                || thingTypeUID.equals(THING_TYPE26) || thingTypeUID.equals(THING_TYPE29)
                || thingTypeUID.equals(THING_TYPE3A)) {
            discoveryService.unDiscover(thingHandler.getThing().getUID());

        }
    }

}
