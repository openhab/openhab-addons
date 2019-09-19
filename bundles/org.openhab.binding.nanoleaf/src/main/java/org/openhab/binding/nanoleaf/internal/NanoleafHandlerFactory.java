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
package org.openhab.binding.nanoleaf.internal;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.nanoleaf.internal.discovery.NanoleafPanelsDiscoveryService;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafControllerHandler;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafPanelHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NanoleafHandlerFactory} is responsible for creating the controller (bridge)
 * and panel (thing) handlers.
 *
 * @author Martin Raepple - Initial contribution
 */

@Component(configurationPid = "binding.nanoleaf", service = ThingHandlerFactory.class)
public class NanoleafHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NanoleafHandlerFactory.class);

    private HttpClient httpClient;

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_LIGHT_PANEL, THING_TYPE_CONTROLLER).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            NanoleafControllerHandler handler = new NanoleafControllerHandler((Bridge) thing, httpClient);
            registerDiscoveryService(handler);
            logger.debug("Nanoleaf controller handler created.");
            return handler;
        } else if (THING_TYPE_LIGHT_PANEL.equals(thingTypeUID)) {
            NanoleafPanelHandler handler = new NanoleafPanelHandler(thing, httpClient);
            logger.debug("Nanoleaf panel handler created.");
            return handler;
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NanoleafControllerHandler) {
            unregisterDiscoveryService(thingHandler.getThing());
            logger.debug("Nanoleaf controller handler removed.");
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    private synchronized void registerDiscoveryService(NanoleafControllerHandler bridgeHandler) {
        NanoleafPanelsDiscoveryService discoveryService = new NanoleafPanelsDiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
        logger.debug("Discovery service for panels registered.");
    }

    private synchronized void unregisterDiscoveryService(Thing thing) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thing.getUID());
        if (serviceReg != null) {
            serviceReg.unregister();
            logger.debug("Discovery service for panels removed.");
        }
    }
}
