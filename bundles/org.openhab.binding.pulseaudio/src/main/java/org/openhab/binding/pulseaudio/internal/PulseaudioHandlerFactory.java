/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.pulseaudio.internal.discovery.PulseaudioDeviceDiscoveryService;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioBridgeHandler;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
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

    private PulseAudioBindingConfiguration configuration = new PulseAudioBindingConfiguration();

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
        ServiceRegistration<?> serviceRegistration = this.discoveryServiceReg.get(thingHandler);
        if (serviceRegistration != null) {
            PulseaudioDeviceDiscoveryService service = (PulseaudioDeviceDiscoveryService) bundleContext
                    .getService(serviceRegistration.getReference());
            service.deactivate();
            serviceRegistration.unregister();
        }
        discoveryServiceReg.remove(thingHandler);
        super.removeHandler(thingHandler);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            PulseaudioBridgeHandler handler = new PulseaudioBridgeHandler((Bridge) thing, configuration);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PulseaudioHandler(thing, bundleContext);
        }

        return null;
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected synchronized void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configuration.update(new Configuration(config).as(PulseAudioBindingConfiguration.class));
        logger.debug("pulseaudio configuration update received ({})", config);
    }
}
