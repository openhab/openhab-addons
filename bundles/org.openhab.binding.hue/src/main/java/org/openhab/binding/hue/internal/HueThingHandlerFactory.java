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
package org.openhab.binding.hue.internal;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueLightHandler;
import org.openhab.binding.hue.internal.handler.sensors.DimmerSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.LightLevelHandler;
import org.openhab.binding.hue.internal.handler.sensors.PresenceHandler;
import org.openhab.binding.hue.internal.handler.sensors.TapSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.TemperatureHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hue")
public class HueThingHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            Stream.of(HueBridgeHandler.SUPPORTED_THING_TYPES.stream(), HueLightHandler.SUPPORTED_THING_TYPES.stream(),
                    DimmerSwitchHandler.SUPPORTED_THING_TYPES.stream(), TapSwitchHandler.SUPPORTED_THING_TYPES.stream(),
                    PresenceHandler.SUPPORTED_THING_TYPES.stream(), TemperatureHandler.SUPPORTED_THING_TYPES.stream(),
                    LightLevelHandler.SUPPORTED_THING_TYPES.stream()).flatMap(i -> i).collect(Collectors.toSet()));

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueLightUID = getLightUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueLightUID, bridgeUID);
        } else if (DimmerSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || TapSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || PresenceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || TemperatureHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || LightLevelHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueSensorUID = getSensorUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueSensorUID, bridgeUID);
        }

        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private ThingUID getLightUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            return getThingUID(thingTypeUID, configuration.get(LIGHT_ID).toString(), bridgeUID);
        }
    }

    private ThingUID getSensorUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            return getThingUID(thingTypeUID, configuration.get(SENSOR_ID).toString(), bridgeUID);
        }
    }

    private ThingUID getThingUID(ThingTypeUID thingTypeUID, String id, @Nullable ThingUID bridgeUID) {
        if (bridgeUID != null) {
            return new ThingUID(thingTypeUID, id, bridgeUID.getId());
        } else {
            return new ThingUID(thingTypeUID, id);
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            HueBridgeHandler handler = new HueBridgeHandler((Bridge) thing);
            registerLightDiscoveryService(handler);
            return handler;
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueLightHandler(thing);
        } else if (DimmerSwitchHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new DimmerSwitchHandler(thing);
        } else if (TapSwitchHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new TapSwitchHandler(thing);
        } else if (PresenceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new PresenceHandler(thing);
        } else if (TemperatureHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new TemperatureHandler(thing);
        } else if (LightLevelHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new LightLevelHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerLightDiscoveryService(HueBridgeHandler bridgeHandler) {
        HueLightDiscoveryService discoveryService = new HueLightDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HueBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                HueLightDiscoveryService service = (HueLightDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }
}
