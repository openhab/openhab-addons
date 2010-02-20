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
package org.openhab.binding.upnpcontrol.internal;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpRendererHandler;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpServerHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.upnpcontrol")
@NonNullByDefault
public class UpnpControlHandlerFactory extends BaseThingHandlerFactory implements UpnpAudioSinkReg {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConcurrentMap<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, UpnpServerHandler> upnpServers = new ConcurrentHashMap<>();

    private final UpnpIOService upnpIOService;
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;
    private final UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider;

    private String callbackUrl = "";

    @Activate
    public UpnpControlHandlerFactory(final @Reference UpnpIOService upnpIOService,
            final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference UpnpDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            final @Reference UpnpDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider) {
        this.upnpIOService = upnpIOService;
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.upnpStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.upnpCommandDescriptionProvider = dynamicCommandDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_RENDERER)) {
            return addRenderer(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SERVER)) {
            return addServer(thing);
        }
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String key = thing.getUID().toString();

        if (thingTypeUID.equals(THING_TYPE_RENDERER)) {
            removeRenderer(key);
        } else if (thingTypeUID.equals(THING_TYPE_SERVER)) {
            removeServer(key);
        }
        super.unregisterHandler(thing);
    }

    private UpnpServerHandler addServer(Thing thing) {
        UpnpServerHandler handler = new UpnpServerHandler(thing, upnpIOService, upnpRenderers,
                upnpStateDescriptionProvider, upnpCommandDescriptionProvider);
        String key = thing.getUID().toString();
        upnpServers.put(key, handler);
        logger.debug("Media server handler created for {}", thing.getLabel());
        return handler;
    }

    private UpnpRendererHandler addRenderer(Thing thing) {
        callbackUrl = createCallbackUrl();
        UpnpRendererHandler handler = new UpnpRendererHandler(thing, upnpIOService, this);
        String key = thing.getUID().toString();
        upnpRenderers.put(key, handler);
        upnpServers.forEach((thingId, value) -> value.addRendererOption(key));
        logger.debug("Media renderer handler created for {}", thing.getLabel());

        return handler;
    }

    private void removeServer(String key) {
        logger.debug("Removing media server handler for {}", upnpServers.get(key).getThing().getLabel());
        upnpServers.remove(key);
    }

    private void removeRenderer(String key) {
        logger.debug("Removing media renderer handler for {}", upnpRenderers.get(key).getThing().getLabel());
        if (audioSinkRegistrations.containsKey(key)) {
            logger.debug("Removing audio sink registration for {}", upnpRenderers.get(key).getThing().getLabel());
            ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(key);
            reg.unregister();
            audioSinkRegistrations.remove(key);
        }
        upnpServers.forEach((thingId, value) -> value.removeRendererOption(key));
        upnpRenderers.remove(key);
    }

    @Override
    public void registerAudioSink(UpnpRendererHandler handler) {
        if (!(callbackUrl.isEmpty())) {
            UpnpAudioSink audioSink = new UpnpAudioSink(handler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            Thing thing = handler.getThing();
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            logger.debug("Audio sink added for media renderer {}", thing.getLabel());
        }
    }

    private String createCallbackUrl() {
        if (!callbackUrl.isEmpty()) {
            return callbackUrl;
        }
        NetworkAddressService nwaService = networkAddressService;
        String ipAddress = nwaService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return "";
        }
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return "";
        }
        return "http://" + ipAddress + ":" + port;
    }
}
