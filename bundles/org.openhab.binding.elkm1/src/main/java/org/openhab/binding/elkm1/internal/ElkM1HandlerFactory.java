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

package org.openhab.binding.elkm1.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.elkm1.internal.discovery.ElkM1DiscoveryHandler;
import org.openhab.binding.elkm1.internal.handler.ElkM1AreaHandler;
import org.openhab.binding.elkm1.internal.handler.ElkM1BridgeHandler;
import org.openhab.binding.elkm1.internal.handler.ElkM1ZoneHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElkM1HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Bennett - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.elkm1")
public class ElkM1HandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ElkM1HandlerFactory.class);
    private static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(ElkM1BindingConstants.THING_TYPE_BRIDGE, ElkM1BindingConstants.THING_TYPE_ZONE,
                    ElkM1BindingConstants.THING_TYPE_AREA));
    static {
        SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(SUPPORTED_THING_TYPES_UIDS);
    }
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates the specific handler for this thing.
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ElkM1BindingConstants.THING_TYPE_BRIDGE)) {
            ElkM1BridgeHandler bridge = new ElkM1BridgeHandler((Bridge) thing);
            ElkM1DiscoveryHandler discovery = new ElkM1DiscoveryHandler(bridge);
            discoveryServiceRegs.put(bridge.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discovery, new Hashtable<String, Object>()));
            discovery.activate(null);
            return bridge;
        }
        if (thingTypeUID.equals(ElkM1BindingConstants.THING_TYPE_ZONE)) {
            return new ElkM1ZoneHandler(thing);
        }
        if (thingTypeUID.equals(ElkM1BindingConstants.THING_TYPE_AREA)) {
            return new ElkM1AreaHandler(thing);
        }
        logger.error("Can't Create Handler: {}", thingTypeUID);

        return null;
    }

    /**
     * Remove the handler for this thing.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ElkM1BridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                ElkM1DiscoveryHandler service = (ElkM1DiscoveryHandler) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }

        }
        super.removeHandler(thingHandler);
    }
}
