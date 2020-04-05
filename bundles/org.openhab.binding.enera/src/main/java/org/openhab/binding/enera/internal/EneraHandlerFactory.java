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
package org.openhab.binding.enera.internal;

import static org.openhab.binding.enera.internal.EneraBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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
import org.openhab.binding.enera.internal.discovery.EneraDeviceDiscoveryService;
import org.openhab.binding.enera.internal.handler.EneraAccountHandler;
import org.openhab.binding.enera.internal.handler.EneraDeviceHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link EneraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Rahner - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.enera", service = ThingHandlerFactory.class)
public class EneraHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_DEVICE, THING_TYPE_ACCOUNT));;

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            EneraAccountHandler handler = new EneraAccountHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            EneraDeviceHandler handler = new EneraDeviceHandler((Thing) thing);
            return handler;
        }

        return null;
    }

    private synchronized void registerDeviceDiscoveryService(EneraAccountHandler handler) {
        EneraDeviceDiscoveryService discoveryService = new EneraDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
