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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RdsHandlerFactory} creates things and thing handlers
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(configurationPid = "binding.siemensrds", service = ThingHandlerFactory.class)
public class RdsHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_CLOUD, THING_TYPE_RDS)));

    private final Map<ThingUID, ServiceRegistration<?>> discos = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if ((thingTypeUID.equals(THING_TYPE_CLOUD)) && (thing instanceof Bridge)) {
            RdsCloudHandler handler = new RdsCloudHandler((Bridge) thing);
            createDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(THING_TYPE_RDS))
            return new RdsHandler(thing);

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler handler) {
        if (handler instanceof RdsCloudHandler) {
            destroyDiscoveryService((RdsCloudHandler) handler);
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
                new Hashtable<String, Object>());

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
        ServiceRegistration<?> serviceReg = discos.remove(handler.getThing().getUID());

        if (serviceReg != null) {
            // retrieve the respective discovery service
            RdsDiscoveryService disco = (RdsDiscoveryService) bundleContext.getService(serviceReg.getReference());

            // unregister the service
            serviceReg.unregister();

            // deactivate the service
            if (disco != null)
                disco.deactivate();
        }
    }

}
