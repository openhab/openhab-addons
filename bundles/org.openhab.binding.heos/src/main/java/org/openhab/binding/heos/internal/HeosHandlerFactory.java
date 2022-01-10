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
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.api.HeosAudioSink;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscovery;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.handler.HeosDynamicStateDescriptionProvider;
import org.openhab.binding.heos.internal.handler.HeosGroupHandler;
import org.openhab.binding.heos.internal.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.handler.HeosThingBaseHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.config.discovery.DiscoveryService;
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
 * The {@link HeosHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Johannes Einig - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.heos")
@NonNullByDefault
public class HeosHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(HeosHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private @NonNullByDefault({}) AudioHTTPServer audioHTTPServer;
    private @NonNullByDefault({}) NetworkAddressService networkAddressService;
    private @NonNullByDefault({}) HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            HeosBridgeHandler bridgeHandler = new HeosBridgeHandler((Bridge) thing,
                    heosDynamicStateDescriptionProvider);
            HeosPlayerDiscovery playerDiscovery = new HeosPlayerDiscovery(bridgeHandler);
            discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), playerDiscovery, new Hashtable<>()));
            logger.debug("Register discovery service for HEOS player and HEOS groups by bridge '{}'",
                    bridgeHandler.getThing().getUID().getId());
            return bridgeHandler;
        }
        if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            HeosPlayerHandler playerHandler = new HeosPlayerHandler(thing, heosDynamicStateDescriptionProvider);
            registerAudioSink(thing, playerHandler);
            return playerHandler;
        }
        if (THING_TYPE_GROUP.equals(thingTypeUID)) {
            HeosGroupHandler groupHandler = new HeosGroupHandler(thing, heosDynamicStateDescriptionProvider);
            registerAudioSink(thing, groupHandler);
            return groupHandler;
        }
        return null;
    }

    private void registerAudioSink(Thing thing, HeosThingBaseHandler thingBaseHandler) {
        HeosAudioSink audioSink = new HeosAudioSink(thingBaseHandler, audioHTTPServer, createCallbackUrl());
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
        audioSinkRegistrations.put(thing.getUID().toString(), reg);
    }

    @Override
    public void unregisterHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(THING_TYPE_BRIDGE)) {
            super.unregisterHandler(thing);
            ServiceRegistration<?> serviceRegistration = discoveryServiceRegs.get(thing.getUID());
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                discoveryServiceRegs.remove(thing.getUID());
                logger.debug("Unregister discovery service for HEOS player and HEOS groups by bridge '{}'",
                        thing.getUID().getId());
            }
        }
        if (THING_TYPE_PLAYER.equals(thing.getThingTypeUID()) || THING_TYPE_GROUP.equals(thing.getThingTypeUID())) {
            super.unregisterHandler(thing);
            ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
            if (reg != null) {
                reg.unregister();
            }
        }
    }

    @Reference
    protected void setAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = audioHTTPServer;
    }

    protected void unsetAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = null;
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(HeosDynamicStateDescriptionProvider provider) {
        this.heosDynamicStateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(HeosDynamicStateDescriptionProvider provider) {
        this.heosDynamicStateDescriptionProvider = null;
    }

    private @Nullable String createCallbackUrl() {
        final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }
        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }
        return "http://" + ipAddress + ":" + port;
    }
}
