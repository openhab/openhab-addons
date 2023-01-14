/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flicbutton.internal.discovery.FlicButtonDiscoveryService;
import org.openhab.binding.flicbutton.internal.discovery.FlicSimpleclientDiscoveryServiceImpl;
import org.openhab.binding.flicbutton.internal.handler.FlicButtonHandler;
import org.openhab.binding.flicbutton.internal.handler.FlicDaemonBridgeHandler;
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

/**
 * The {@link FlicButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Fink - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.flicbutton")
@NonNullByDefault
public class FlicButtonHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(FlicButtonBindingConstants.BRIDGE_THING_TYPES_UIDS.stream(),
                    FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE)) {
            return new FlicButtonHandler(thing);
        } else if (thingTypeUID.equals(FlicButtonBindingConstants.BRIDGE_THING_TYPE)) {
            FlicButtonDiscoveryService discoveryService = new FlicSimpleclientDiscoveryServiceImpl(thing.getUID());
            FlicDaemonBridgeHandler bridgeHandler = new FlicDaemonBridgeHandler((Bridge) thing, discoveryService);
            registerDiscoveryService(discoveryService, thing.getUID());

            return bridgeHandler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof FlicDaemonBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing().getUID());
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void registerDiscoveryService(FlicButtonDiscoveryService discoveryService,
            ThingUID bridgeUID) {
        this.discoveryServiceRegs.put(bridgeUID, getBundleContext().registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDiscoveryService(ThingUID bridgeUID) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID);
        if (serviceReg != null) {
            FlicButtonDiscoveryService service = (FlicButtonDiscoveryService) getBundleContext()
                    .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
            discoveryServiceRegs.remove(bridgeUID);
        }
    }
}
