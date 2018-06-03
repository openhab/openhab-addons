/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal;

import static org.openhab.binding.kodi.KodiBindingConstants.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.kodi.handler.KodiHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
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
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.kodi")
public class KodiHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(KodiHandlerFactory.class);

    private AudioHTTPServer audioHTTPServer;
    private NetworkAddressService networkAddressService;

    // url (scheme+server+port) to use for playing notification sounds
    private String callbackUrl = null;

    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private KodiDynamicStateDescriptionProvider stateDescriptionProvider;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_KODI)) {
            KodiHandler handler = new KodiHandler(thing, stateDescriptionProvider);

            // register the Kodi as an audio sink
            KodiAudioSink audioSink = new KodiAudioSink(handler, audioHTTPServer, createCallbackUrl());
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);

            return handler;
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
    protected void setDynamicStateDescriptionProvider(KodiDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(KodiDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = null;
    }
}
