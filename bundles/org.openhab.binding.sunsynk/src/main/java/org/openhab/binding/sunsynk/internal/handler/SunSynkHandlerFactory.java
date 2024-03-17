/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.handler;

import static org.openhab.binding.sunsynk.internal.SunSynkBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.discovery.SunSynkAccountDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSynkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Lee Charlton - Initial contribution
 */
// @NonNullByDefault
@Component(configurationPid = "binding.sunsynk", service = ThingHandlerFactory.class)

public class SunSynkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SunSynkHandlerFactory.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.unmodifiableSet(
            Stream.of(BRIDGE_TYPE_ACCOUNT, THING_TYPE_INVERTER, THING_TYPE_PLANT).collect(Collectors.toSet()));

    // private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
    // Set.of(BRIDGE_TYPE_ACCOUNT,THING_TYPE_PLANT,THING_TYPE_INVERTER);

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Set.of(THING_TYPE_PLANT, THING_TYPE_INVERTER);

    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    public boolean supportsThingType(@NonNull ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(@NonNull Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PLANT)) {
            logger.debug("SunSynkHandlerFactory created Plant Handler ");
            return new SunSynkPlantHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_INVERTER)) {
            logger.debug("SunSynkHandlerFactory created Inverter Handler ");
            return new SunSynkInverterHandler(thing);
        } else if (thingTypeUID.equals(BRIDGE_TYPE_ACCOUNT)) {
            SunSynkAccountHandler handler = new SunSynkAccountHandler((Bridge) thing);
            registerAccountDiscoveryService(handler);
            logger.debug("SunSynkHandlerFactory created Account Handler ");
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

    private void registerAccountDiscoveryService(SunSynkAccountHandler handler) {
        SunSynkAccountDiscoveryService discoveryService = new SunSynkAccountDiscoveryService(handler);

        ServiceRegistration<DiscoveryService> serviceRegistration = this.bundleContext
                .registerService(DiscoveryService.class, discoveryService, null);

        discoveryServiceRegistrations.put(handler.getThing().getUID(), serviceRegistration);
    }
}
