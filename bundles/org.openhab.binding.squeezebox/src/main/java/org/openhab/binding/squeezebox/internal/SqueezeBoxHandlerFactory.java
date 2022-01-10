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
package org.openhab.binding.squeezebox.internal;

import static org.openhab.binding.squeezebox.internal.SqueezeBoxBindingConstants.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.squeezebox.internal.discovery.SqueezeBoxPlayerDiscoveryParticipant;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerEventListener;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerHandler;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxServerHandler;
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
 * The {@link SqueezeBoxHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Cancel request player job when handler removed
 * @author Mark Hilbush - Add callbackUrl
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.squeezebox")
public class SqueezeBoxHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SqueezeBoxServerHandler.SUPPORTED_THING_TYPES_UIDS.stream(),
                    SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final SqueezeBoxStateDescriptionOptionsProvider stateDescriptionProvider;

    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // Callback url (scheme+server+port) to use for playing notification sounds
    private String callbackUrl = null;

    @Activate
    public SqueezeBoxHandlerFactory(@Reference AudioHTTPServer audioHTTPServer,
            @Reference NetworkAddressService networkAddressService,
            @Reference SqueezeBoxStateDescriptionOptionsProvider stateDescriptionProvider) {
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
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
            SqueezeBoxPlayerHandler playerHandler = new SqueezeBoxPlayerHandler(thing, createCallbackUrl(),
                    stateDescriptionProvider);

            // Register the player as an audio sink
            logger.trace("Registering an audio sink for player thing {}", thing.getUID());
            SqueezeBoxAudioSink audioSink = new SqueezeBoxAudioSink(playerHandler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
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
        discoveryServiceRegs.put(squeezeBoxServerHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
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

    private String createCallbackUrl() {
        final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }

        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }

        return "http://" + ipAddress + ":" + port;
    }
}
