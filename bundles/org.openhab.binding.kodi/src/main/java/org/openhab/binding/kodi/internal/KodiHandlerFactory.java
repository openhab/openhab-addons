/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.kodi.internal;

import static org.openhab.binding.kodi.internal.KodiBindingConstants.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.kodi.internal.handler.KodiHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
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
 * The {@link KodiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Improvements on channels for opening PVR TV or Radio streams
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.kodi")
public class KodiHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(KodiHandlerFactory.class);

    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final KodiDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final KodiDynamicStateDescriptionProvider stateDescriptionProvider;
    private final WebSocketClient webSocketClient;

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // url (scheme+server+port) to use for playing notification sounds
    private @Nullable String callbackUrl;

    @Activate
    public KodiHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference KodiDynamicCommandDescriptionProvider commandDescriptionProvider,
            final @Reference KodiDynamicStateDescriptionProvider stateDescriptionProvider,
            final @Reference WebSocketFactory webSocketFactory) {
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_KODI)) {
            String callbackUrl = createCallbackUrl();
            KodiHandler handler = new KodiHandler(thing, commandDescriptionProvider, stateDescriptionProvider,
                    webSocketClient, callbackUrl);

            // register the Kodi as an audio sink
            KodiAudioSink audioSink = new KodiAudioSink(handler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);

            return handler;
        }

        return null;
    }

    private @Nullable String createCallbackUrl() {
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

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
        if (reg != null) {
            reg.unregister();
        }
    }
}
