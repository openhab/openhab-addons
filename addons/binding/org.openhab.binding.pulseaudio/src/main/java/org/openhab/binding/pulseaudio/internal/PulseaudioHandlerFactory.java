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
package org.openhab.binding.pulseaudio.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.pulseaudio.internal.discovery.PulseaudioDeviceDiscoveryService;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioBridgeHandler;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PulseaudioHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pulseaudio")
public class PulseaudioHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(PulseaudioHandlerFactory.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.concat(PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.stream(),
                    PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.stream()).collect(Collectors.toSet()));

    private final Map<ThingHandler, ServiceRegistration<?>> discoveryServiceReg = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        }
        if (PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID deviceUID = getPulseaudioDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    private void registerDeviceDiscoveryService(PulseaudioBridgeHandler paBridgeHandler) {
        PulseaudioDeviceDiscoveryService discoveryService = new PulseaudioDeviceDiscoveryService(paBridgeHandler);
        discoveryService.activate();
        this.discoveryServiceReg.put(paBridgeHandler,
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private ThingUID getPulseaudioDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID == null) {
            String name = (String) configuration.get(PulseaudioBindingConstants.DEVICE_PARAMETER_NAME);
            return new ThingUID(thingTypeUID, name, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (this.discoveryServiceReg.containsKey(thingHandler)) {
            PulseaudioDeviceDiscoveryService service = (PulseaudioDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceReg.get(thingHandler).getReference());
            service.deactivate();
            discoveryServiceReg.get(thingHandler).unregister();
            discoveryServiceReg.remove(thingHandler);
        }
        super.removeHandler(thingHandler);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            PulseaudioBridgeHandler handler = new PulseaudioBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PulseaudioHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        modified(componentContext);
    }

    protected synchronized void modified(ComponentContext componentContext) {
        Dictionary<String, ?> properties = componentContext.getProperties();
        logger.info("pulseaudio configuration update received ({})", properties);
        if (properties == null) {
            return;
        }
        Enumeration<String> e = properties.keys();
        while (e.hasMoreElements()) {
            String k = e.nextElement();
            if (PulseaudioBindingConstants.TYPE_FILTERS.containsKey(k)) {
                PulseaudioBindingConstants.TYPE_FILTERS.put(k, (boolean) properties.get(k));
            }
            logger.debug("update received {}: {}", k, properties.get(k));
        }
    }
}
