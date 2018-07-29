/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.HeosBindingConstants.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.handler.HeosGroupHandler;
import org.openhab.binding.heos.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.api.HeosAudioSink;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscovery;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosHandlerFactory extends BaseThingHandlerFactory {
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(HeosHandlerFactory.class);
    private HeosSystem heos = new HeosSystem();
    private HeosFacade api = heos.getAPI();

    private AudioHTTPServer audioHTTPServer;
    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private String callbackUrl;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
    };

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            HeosBridgeHandler bridgeHandler = new HeosBridgeHandler((Bridge) thing, heos, api);
            HeosPlayerDiscovery playerDiscovery = new HeosPlayerDiscovery(bridgeHandler);
            playerDiscovery.addDiscoveryListener(bridgeHandler);
            discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), playerDiscovery, new Hashtable<String, Object>()));
            logger.info("Register discovery service for HEOS player and HEOS groups by bridge '{}'",
                    bridgeHandler.getThing().getUID().getId());
            return bridgeHandler;
        }
        if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            HeosPlayerHandler playerHandler = new HeosPlayerHandler(thing, heos, api);
            // register the speaker as an audio sink
            HeosAudioSink audioSink = new HeosAudioSink(playerHandler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            return playerHandler;
        }
        if (THING_TYPE_GROUP.equals(thingTypeUID)) {
            HeosGroupHandler groupHandler = new HeosGroupHandler(thing, heos, api);
            // register the group as an audio sink
            HeosAudioSink audioSink = new HeosAudioSink(groupHandler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            return groupHandler;
        }
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(THING_TYPE_BRIDGE)) {
            super.unregisterHandler(thing);
            ServiceRegistration<?> serviceRegistration = this.discoveryServiceRegs.get(thing.getUID());
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                discoveryServiceRegs.remove(thing.getUID());
                logger.info("Unregister discovery service for HEOS player and HEOS groups by bridge '{}'",
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

    protected void setAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = audioHTTPServer;
    }

    protected void unsetAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = null;
    }
}
