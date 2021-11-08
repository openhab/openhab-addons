/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.device.TapoSmartBulb;
import org.openhab.binding.tapocontrol.internal.device.TapoSmartPlug;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TapoControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Wild - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tapocontrol")
@NonNullByDefault
public class TapoControlThingHandler extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(TapoControlThingHandler.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Set<TapoBridgeHandler> accountHandlers = new HashSet<>();
    private final HttpClient httpClient;

    @Activate // @Reference TapoCredentials credentials,
    // public TapoControlThingHandler(Map<String, Object> properties, @Reference final HttpClientFactory
    // httpClientFactory) {
    public TapoControlThingHandler(@Reference final HttpClientFactory httpClientFactory) {
        logger.debug("thinghandler created");
        this.httpClient = httpClientFactory.getCommonHttpClient();
        httpClient.setFollowRedirects(false);
    }

    /**
     * Provides the supported thing types
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Create handler of things.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_BRIDGE_UIDS.contains(thingTypeUID)) {
            logger.trace("returns new TapoBridge '{}'", thingTypeUID.toString());
            TapoBridgeHandler bridgeHandler = new TapoBridgeHandler((Bridge) thing, httpClient);
            accountHandlers.add(bridgeHandler);
            registerDisoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (SUPPORTED_SMART_PLUG_UIDS.contains(thingTypeUID)) {
            logger.trace("returns new TapoSmartPLUG '{}'", thingTypeUID.toString());
            return new TapoSmartPlug(thing, httpClient);
        } else if (SUPPORTED_WHITE_BULB_UIDS.contains(thingTypeUID)) {
            logger.trace("returns new TapoSmartBULB '{}'", thingTypeUID.toString());
            return new TapoSmartBulb(thing, httpClient);
        } else if (SUPPORTED_COLOR_BULB_UIDS.contains(thingTypeUID)) {
            logger.trace("returns new TapoSmartBULB '{}'", thingTypeUID.toString());
            return new TapoSmartBulb(thing, httpClient);
        } else {
            logger.error("ThingHandler not found for {}", thingTypeUID.toString());
        }
        return null;
    }

    /**
     * REMOVE HANDLER
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TapoBridgeHandler) {
            unregisterDisoveryService(thingHandler);
            accountHandlers.remove(thingHandler);
        }
    }

    /**
     * Register Disovery Service
     */
    protected synchronized void registerDisoveryService(TapoBridgeHandler accountHandler) {
        logger.trace("registering discovery service");
        try {
            TapoDiscoveryService discoveryService = new TapoDiscoveryService(accountHandler);
            this.discoveryServiceRegs.put(accountHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
            discoveryService.activate();
            discoveryService.startDelayedScan();
        } catch (Exception e) {
            logger.error("error creating discoveryService", e);
        }
    }

    /**
     * Unregister Discovery Service
     */
    protected void unregisterDisoveryService(ThingHandler thingHandler) {
        logger.trace("unregistering discovery service");

        ServiceRegistration<?> discoveryServiceRegistration = this.discoveryServiceRegs
                .remove(thingHandler.getThing().getUID());

        if (discoveryServiceRegistration != null) {
            // remove discovery service, if bridge handler is removed
            try {
                TapoDiscoveryService discoveryService = (TapoDiscoveryService) bundleContext
                        .getService(discoveryServiceRegistration.getReference());
                discoveryServiceRegistration.unregister();
                discoveryService.abortScan();
            } catch (Exception e) {
                logger.error("error removing discoveryService", e);
            }

        }
    }
}
