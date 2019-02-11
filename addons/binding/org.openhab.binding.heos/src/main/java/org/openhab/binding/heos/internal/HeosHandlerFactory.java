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
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.handler.HeosGroupHandler;
import org.openhab.binding.heos.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.api.HeosAudioSink;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscovery;
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
public class HeosHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(HeosHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private HeosSystem heos = new HeosSystem();
    private HeosFacade api = heos.getAPI();

    private AudioHTTPServer audioHTTPServer;
    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private NetworkAddressService networkAddressService;

    private String callbackUrl;

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
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            HeosBridgeHandler bridgeHandler = new HeosBridgeHandler((Bridge) thing, heos, api);
            HeosPlayerDiscovery playerDiscovery = new HeosPlayerDiscovery(bridgeHandler);
            discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), playerDiscovery, new Hashtable<>()));
            logger.debug("Register discovery service for HEOS player and HEOS groups by bridge '{}'",
                    bridgeHandler.getThing().getUID().getId());
            return bridgeHandler;
        }
        if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            HeosPlayerHandler playerHandler = new HeosPlayerHandler(thing, heos, api);
            // register the speaker as an audio sink
            HeosAudioSink audioSink = new HeosAudioSink(playerHandler, audioHTTPServer, createCallbackUrl());
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            return playerHandler;
        }
        if (THING_TYPE_GROUP.equals(thingTypeUID)) {
            HeosGroupHandler groupHandler = new HeosGroupHandler(thing, heos, api);
            // register the group as an audio sink
            HeosAudioSink audioSink = new HeosAudioSink(groupHandler, audioHTTPServer, createCallbackUrl());
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            return groupHandler;
        }
        return null;
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

    private String createCallbackUrl() {
        if (callbackUrl != null) {
            return callbackUrl;
        } else {
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
}
