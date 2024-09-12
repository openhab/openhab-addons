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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link RdsHandlerFactory} creates things and thing handlers
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.siemensrds", service = ThingHandlerFactory.class)
public class RdsHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CLOUD, THING_TYPE_RDS);

    private final Map<ThingUID, ServiceRegistration<?>> discos = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if ((thingTypeUID.equals(THING_TYPE_CLOUD)) && (thing instanceof Bridge bridge)) {
            RdsCloudHandler handler = new RdsCloudHandler(bridge);
            createDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(THING_TYPE_RDS)) {
            return new RdsHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler handler) {
        if (handler instanceof RdsCloudHandler cloudHandler) {
            destroyDiscoveryService(cloudHandler);
        }
    }

    /*
     * create a discovery service so that a newly created cloud account will find
     * the things that it supports
     */
    private synchronized void createDiscoveryService(RdsCloudHandler handler) {
        // create a new discovery service
        RdsDiscoveryService ds = new RdsDiscoveryService(handler);

        // register the discovery service
        ServiceRegistration<?> serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), ds,
                new Hashtable<>());

        /*
         * store service registration in a list so we can destroy it when the respective
         * hub is destroyed
         */
        discos.put(handler.getThing().getUID(), serviceReg);

        // finally activate the discovery service
        ds.activate();
    }

    /*
     * destroy the discovery service
     */
    private synchronized void destroyDiscoveryService(RdsCloudHandler handler) {
        // fetch the respective thing's service registration from our list
        @Nullable
        ServiceRegistration<?> serviceReg = discos.remove(handler.getThing().getUID());

        // retrieve the respective discovery service
        if (serviceReg != null) {
            RdsDiscoveryService disco = (RdsDiscoveryService) bundleContext.getService(serviceReg.getReference());

            // unregister the service
            serviceReg.unregister();

            // deactivate the service
            if (disco != null) {
                disco.deactivate();
            }
        }
    }
}
