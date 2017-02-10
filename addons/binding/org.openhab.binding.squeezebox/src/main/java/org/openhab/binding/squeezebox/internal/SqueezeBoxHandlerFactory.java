/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
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
import org.openhab.binding.squeezebox.discovery.SqueezeBoxPlayerDiscoveryParticipant;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerEventListener;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerHandler;
import org.openhab.binding.squeezebox.handler.SqueezeBoxServerHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link SqueezeBoxHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Cancel request player job when handler removed
 */
public class SqueezeBoxHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            SqueezeBoxServerHandler.SUPPORTED_THING_TYPES_UIDS, SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private AudioHTTPServer audioHTTPServer;

    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SQUEEZEBOXSERVER_THING_TYPE)) {
            logger.trace("creating handler for bridge thing {}", thing);
            SqueezeBoxServerHandler bridge = new SqueezeBoxServerHandler((Bridge) thing);
            registerSqueezeBoxPlayerDiscoveryService(bridge);
            return bridge;
        }

        if (thingTypeUID.equals(SQUEEZEBOXPLAYER_THING_TYPE)) {
            logger.trace("creating handler for player thing {}", thing);
            SqueezeBoxPlayerHandler playerHandler = new SqueezeBoxPlayerHandler(thing);

            // Register the player as an audio sink
            logger.trace("Registering an audio sink for player thing {}", thing.getUID());
            SqueezeBoxAudioSink audioSink = new SqueezeBoxAudioSink(playerHandler, audioHTTPServer);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);

            return playerHandler;
        }

        return null;
    }

    /**
     * Adds SqueezeBoxServerHandlers to the discovery service to find SqueezeBox
     * Players
     *
     * @param squeezeBoxServerHandler
     */
    private synchronized void registerSqueezeBoxPlayerDiscoveryService(
            SqueezeBoxServerHandler squeezeBoxServerHandler) {
        logger.trace("registering player discovery service");

        SqueezeBoxPlayerDiscoveryParticipant discoveryService = new SqueezeBoxPlayerDiscoveryParticipant(
                squeezeBoxServerHandler);

        // Register the PlayerListener with the SqueezeBoxServerHandler
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(discoveryService);

        // Register the service, then add the service to the ServiceRegistration map
        discoveryServiceRegs.put(squeezeBoxServerHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {

        if (thingHandler instanceof SqueezeBoxServerHandler) {
            logger.trace("removing handler for bridge thing {}", thingHandler.getThing());

            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                logger.trace("unregistering player discovery service");

                // Get the discovery service object and use it to cancel the RequestPlayerJob
                SqueezeBoxPlayerDiscoveryParticipant discoveryService = (SqueezeBoxPlayerDiscoveryParticipant) bundleContext
                        .getService(serviceReg.getReference());
                discoveryService.cancelRequestPlayerJob();

                // Unregister the PlayerListener from the SqueezeBoxServerHandler
                ((SqueezeBoxServerHandler) thingHandler).unregisterSqueezeBoxPlayerListener(
                        (SqueezeBoxPlayerEventListener) bundleContext.getService(serviceReg.getReference()));

                // Unregister the PlayerListener service
                serviceReg.unregister();

                // Remove the service from the ServiceRegistration map
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }

        if (thingHandler instanceof SqueezeBoxPlayerHandler) {
            SqueezeBoxServerHandler bridge = ((SqueezeBoxPlayerHandler) thingHandler).getSqueezeBoxServerHandler();
            if (bridge != null) {
                // Unregister the player's audio sink
                logger.trace("Unregistering the audio sync service for player thing {}",
                        thingHandler.getThing().getUID());
                ServiceRegistration<AudioSink> reg = audioSinkRegistrations
                        .get(thingHandler.getThing().getUID().toString());
                if (reg != null) {
                    reg.unregister();
                }

                logger.trace("removing handler for player thing {}", thingHandler.getThing());
                bridge.removePlayerCache(((SqueezeBoxPlayerHandler) thingHandler).getMac());
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
