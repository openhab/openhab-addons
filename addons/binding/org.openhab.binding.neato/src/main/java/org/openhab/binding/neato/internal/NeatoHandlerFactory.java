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
package org.openhab.binding.neato.internal;

import static org.openhab.binding.neato.internal.NeatoBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.neato.internal.discovery.NeatoAccountDiscoveryService;
import org.openhab.binding.neato.internal.handler.NeatoAccountHandler;
import org.openhab.binding.neato.internal.handler.NeatoHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NeatoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrik Wimnell - Initial contribution
 * @author Jeff Lauterbach - Adding Bridge thing type
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.neato")
public class NeatoHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE_NEATOACCOUNT, THING_TYPE_VACUUMCLEANER).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections
            .singleton(THING_TYPE_VACUUMCLEANER);

    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VACUUMCLEANER)) {
            return new NeatoHandler(thing);
        } else if (thingTypeUID.equals(BRIDGE_TYPE_NEATOACCOUNT)) {
            NeatoAccountHandler handler = new NeatoAccountHandler((Bridge) thing);
            registerAccountDiscoveryService(handler);
            return handler;
        }

        return null;
    }

    @Override
    protected void removeHandler(@NonNull ThingHandler thingHandler) {
        ServiceRegistration<DiscoveryService> serviceRegistration = discoveryServiceRegistrations
                .get(thingHandler.getThing().getUID());

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    private void registerAccountDiscoveryService(NeatoAccountHandler handler) {
        NeatoAccountDiscoveryService discoveryService = new NeatoAccountDiscoveryService(handler);

        ServiceRegistration<DiscoveryService> serviceRegistration = this.bundleContext
                .registerService(DiscoveryService.class, discoveryService, null);

        discoveryServiceRegistrations.put(handler.getThing().getUID(), serviceRegistration);
    }
}
