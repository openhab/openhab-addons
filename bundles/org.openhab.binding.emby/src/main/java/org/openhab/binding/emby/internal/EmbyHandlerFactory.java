/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_CONTROLLER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_DEVICE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.handler.EmbyBridgeHandler;
import org.openhab.binding.emby.internal.handler.EmbyDeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.emby", service = ThingHandlerFactory.class)
public class EmbyHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(EmbyHandlerFactory.class);

    private NetworkAddressService networkAddressService;
    private WebSocketFactory webSocketClientFactory;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_EMBY_CONTROLLER, THING_TYPE_EMBY_DEVICE).collect(Collectors.toSet()));
    private @Nullable String callbackUrl = null;
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public EmbyHandlerFactory(final @Reference WebSocketFactory webSocketClientFactory,
            final @Reference NetworkAddressService networkAddressService, final ComponentContext componentContext) {
        super.activate(componentContext);
        this.webSocketClientFactory = webSocketClientFactory;
        this.networkAddressService = networkAddressService;
        Dictionary<String, Object> properties = componentContext.getProperties();
        this.callbackUrl = (String) properties.get("callbackUrl");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_EMBY_DEVICE)) {
            logger.debug("Creating EMBY Device Handler for {}.", thing.getLabel());
            return new EmbyDeviceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_EMBY_CONTROLLER)) {
            logger.debug("Creating EMBY Bridge Handler for {}.", thing.getLabel());
            EmbyBridgeHandler bridgeHandler = new EmbyBridgeHandler((Bridge) thing, createCallbackUrl(),
                    createCallbackPort(), webSocketClientFactory.getCommonWebSocketClient());
            registerEmbyClientDiscoveryService(bridgeHandler);
            return bridgeHandler;
        }
        return null;
    }

    private @Nullable String createCallbackUrl() {
        if (callbackUrl != null) {
            logger.debug("The callback url was set to {}", callbackUrl);
            return callbackUrl;
        } else {
            final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }
            logger.debug("Callback URL not set; obtained IP address {} from network address service", ipAddress);
            return ipAddress;
        }
    }

    private @Nullable String createCallbackPort() {
        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the HTTP service.");
            return null;
        }
        return Integer.toString(port);
    }

    private synchronized void registerEmbyClientDiscoveryService(EmbyBridgeHandler bridgeHandler) {
        EmbyClientDiscoveryService discoveryService = new EmbyClientDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
