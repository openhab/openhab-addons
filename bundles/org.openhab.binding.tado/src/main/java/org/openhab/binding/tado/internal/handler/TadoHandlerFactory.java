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
package org.openhab.binding.tado.internal.handler;

import static org.openhab.binding.tado.internal.TadoBindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.discovery.TadoDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TadoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tado", service = ThingHandlerFactory.class)
public class TadoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_HOME, THING_TYPE_ZONE, THING_TYPE_MOBILE_DEVICE)));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final TadoStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public TadoHandlerFactory(final @Reference TadoStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HOME)) {
            TadoHomeHandler tadoHomeHandler = new TadoHomeHandler((Bridge) thing);
            registerTadoDiscoveryService(tadoHomeHandler);
            return tadoHomeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new TadoZoneHandler(thing, stateDescriptionProvider);
        } else if (thingTypeUID.equals(THING_TYPE_MOBILE_DEVICE)) {
            return new TadoMobileDeviceHandler(thing);
        }

        return null;
    }

    private synchronized void registerTadoDiscoveryService(TadoHomeHandler tadoHomeHandler) {
        TadoDiscoveryService discoveryService = new TadoDiscoveryService(tadoHomeHandler);
        ServiceRegistration<?> serviceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
        discoveryService.activate();
        this.discoveryServiceRegs.put(tadoHomeHandler.getThing().getUID(), serviceRegistration);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TadoHomeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                TadoDiscoveryService service = (TadoDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }
}
