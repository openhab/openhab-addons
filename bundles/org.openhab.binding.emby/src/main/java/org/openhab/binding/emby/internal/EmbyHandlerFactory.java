/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.handler.EmbyBridgeHandler;
import org.openhab.binding.emby.internal.handler.EmbyDeviceHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
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
@Component(configurationPid = "binding.emby", service = ThingHandlerFactory.class)
public class EmbyHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(EmbyHandlerFactory.class);

    private NetworkAddressService networkAddressService;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_EMBY_CONTROLLER, THING_TYPE_EMBY_DEVICE).collect(Collectors.toSet()));
    private String callbackUrl = null;
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_EMBY_DEVICE)) {
            return new EmbyDeviceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_EMBY_CONTROLLER)) {
            EmbyBridgeHandler bridgeHandler = new EmbyBridgeHandler((Bridge) thing, createCallbackUrl(),
                    createCallbackPort());
            registerEmbyClientDiscoveryService(bridgeHandler);
            return bridgeHandler;
        }
        return null;
    }

    private String createCallbackUrl() {
        if (callbackUrl != null) {
            return callbackUrl;
        } else {
            final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }
            return ipAddress;
        }
    }

    private String createCallbackPort() {
        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }
        return Integer.toString(port);
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
    }

    private synchronized void registerEmbyClientDiscoveryService(EmbyBridgeHandler bridgeHandler) {
        EmbyClientDiscoveryService discoveryService = new EmbyClientDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
