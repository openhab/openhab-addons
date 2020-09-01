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
package org.openhab.binding.haywardomnilogic.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.haywardomnilogic.internal.discovery.HaywardDiscoveryHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBackyardHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBowHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBridgeHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardChlorinatorHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardColorLogicHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardFilterHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardHeaterHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardRelayHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardSensorHandler;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardVirtualHeaterHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HaywardHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.haywardomnilogic", service = ThingHandlerFactory.class)
public class HaywardHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(HaywardHandlerFactory.class);
    private static Set<ThingTypeUID> supportedThingTypes = new HashSet<ThingTypeUID>(
            Arrays.asList(HaywardBindingConstants.THING_TYPE_BACKYARD, HaywardBindingConstants.THING_TYPE_BOW,
                    HaywardBindingConstants.THING_TYPE_BRIDGE, HaywardBindingConstants.THING_TYPE_CHLORINATOR,
                    HaywardBindingConstants.THING_TYPE_COLORLOGIC, HaywardBindingConstants.THING_TYPE_FILTER,
                    HaywardBindingConstants.THING_TYPE_HEATER, HaywardBindingConstants.THING_TYPE_RELAY,
                    HaywardBindingConstants.THING_TYPE_SENSOR, HaywardBindingConstants.THING_TYPE_VIRTUALHEATER));

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private HttpClient httpClient;

    @Activate
    public HaywardHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return supportedThingTypes.contains(thingTypeUID);
    }

    /**
     * Creates the specific handler for this thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_BRIDGE)) {
            HaywardBridgeHandler bridge = new HaywardBridgeHandler((Bridge) thing, httpClient);
            HaywardDiscoveryHandler discovery = new HaywardDiscoveryHandler(bridge);
            discoveryServiceRegs.put(bridge.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discovery, new Hashtable<String, Object>()));
            discovery.activate(null);
            return bridge;
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_BACKYARD)) {
            return new HaywardBackyardHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_BOW)) {
            return new HaywardBowHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_CHLORINATOR)) {
            return new HaywardChlorinatorHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_COLORLOGIC)) {
            return new HaywardColorLogicHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_FILTER)) {
            return new HaywardFilterHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_HEATER)) {
            return new HaywardHeaterHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_RELAY)) {
            return new HaywardRelayHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_SENSOR)) {
            return new HaywardSensorHandler(thing);
        }
        if (thingTypeUID.equals(HaywardBindingConstants.THING_TYPE_VIRTUALHEATER)) {
            return new HaywardVirtualHeaterHandler(thing);
        }
        logger.error("Can't Create Handler: {}", thingTypeUID);
        return null;
    }

    /**
     * Remove the handler for this thing.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HaywardBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            // remove discovery service, if bridge handler is removed
            HaywardDiscoveryHandler service = (HaywardDiscoveryHandler) bundleContext
                    .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
                serviceReg.unregister();
            }
            discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        }
        super.removeHandler(thingHandler);
    }
}
