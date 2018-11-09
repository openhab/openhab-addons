/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.deconz.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HandlerFactory} is responsible for creating things and thing
 * handlers. It also contains the sensor Think discovery service.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.deconz")
@NonNullByDefault
public class HandlerFactory extends BaseThingHandlerFactory {
    ThingDiscoveryService thingDiscoveryService = new ThingDiscoveryService();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_TYPE, THING_TYPE_PRESENCE_SENSOR, THING_TYPE_DAYLIGHT_SENSOR, THING_TYPE_POWER_SENSOR)
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE.equals(thingTypeUID)) {
            return new DeconzBridgeHandler(thingDiscoveryService, (Bridge) thing);
        } else if (THING_TYPE_PRESENCE_SENSOR.equals(thingTypeUID)) {
            return new SensorThingHandler(thing);
        } else if (THING_TYPE_DAYLIGHT_SENSOR.equals(thingTypeUID)) {
            return new SensorThingHandler(thing);
        } else if (THING_TYPE_POWER_SENSOR.equals(thingTypeUID)) {
            return new SensorThingHandler(thing);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        thingDiscoveryService.start(bundleContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        thingDiscoveryService.stop();
        super.deactivate(componentContext);
    }
}
