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
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.plugwise.internal.handler.PlugwiseRelayDeviceHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseScanHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseSenseHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseStickHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseSwitchHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PlugwiseHandlerFactory} is responsible for creating Plugwise things and thing handlers.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plugwise")
public class PlugwiseHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    private final SerialPortManager serialPortManager;

    @Activate
    public PlugwiseHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_STICK)) {
            PlugwiseStickHandler handler = new PlugwiseStickHandler((Bridge) thing, serialPortManager);
            registerDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CIRCLE) || thingTypeUID.equals(THING_TYPE_CIRCLE_PLUS)
                || thingTypeUID.equals(THING_TYPE_STEALTH)) {
            return new PlugwiseRelayDeviceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SCAN)) {
            return new PlugwiseScanHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SENSE)) {
            return new PlugwiseSenseHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new PlugwiseSwitchHandler(thing);
        }

        return null;
    }

    private void registerDiscoveryService(PlugwiseStickHandler handler) {
        PlugwiseThingDiscoveryService discoveryService = new PlugwiseThingDiscoveryService(handler);
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> registration = this.discoveryServiceRegistrations.get(thingHandler.getThing().getUID());
        if (registration != null) {
            PlugwiseThingDiscoveryService discoveryService = (PlugwiseThingDiscoveryService) bundleContext
                    .getService(registration.getReference());
            discoveryService.deactivate();
            registration.unregister();
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        }
    }
}
