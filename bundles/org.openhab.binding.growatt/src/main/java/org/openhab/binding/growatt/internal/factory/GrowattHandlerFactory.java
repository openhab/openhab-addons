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
package org.openhab.binding.growatt.internal.factory;

import static org.openhab.binding.growatt.internal.GrowattBindingConstants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.discovery.GrowattDiscoveryService;
import org.openhab.binding.growatt.internal.handler.GrowattBridgeHandler;
import org.openhab.binding.growatt.internal.handler.GrowattInverterHandler;
import org.openhab.binding.growatt.internal.servlet.GrottHttpServlet;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrowattHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.growatt", service = ThingHandlerFactory.class)
public class GrowattHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_INVERTER);

    private final Logger logger = LoggerFactory.getLogger(GrowattHandlerFactory.class);

    private final GrottHttpServlet httpServlet = new GrottHttpServlet();
    private final GrowattDiscoveryService discoveryService = new GrowattDiscoveryService();
    private final Set<ThingUID> bridges = Collections.synchronizedSet(new HashSet<>());

    private @Nullable ServiceRegistration<?> discoveryServiceRegistration;

    @Activate
    public GrowattHandlerFactory(@Reference HttpService httpService) {
        try {
            httpService.registerServlet(GrottHttpServlet.PATH, httpServlet, null, null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("GrowattHandlerFactory() failed to register servlet", e);
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            discoveryRegister();
            bridges.add(thing.getUID());
            return new GrowattBridgeHandler((Bridge) thing, httpServlet, discoveryService);
        }

        if (THING_TYPE_INVERTER.equals(thingTypeUID)) {
            return new GrowattInverterHandler(thing);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        discoveryUnregister();
        super.deactivate(componentContext);
    }

    private void discoveryRegister() {
        ServiceRegistration<?> temp = discoveryServiceRegistration;
        if (temp == null) {
            temp = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
            discoveryServiceRegistration = temp;
        }
    }

    private void discoveryUnregister() {
        ServiceRegistration<?> temp = discoveryServiceRegistration;
        if (temp != null) {
            temp.unregister();
        }
        discoveryServiceRegistration = null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof GrowattBridgeHandler) {
            bridges.remove(thingHandler.getThing().getUID());
            if (bridges.isEmpty()) {
                discoveryUnregister();
            }
        }
        super.removeHandler(thingHandler);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
}
