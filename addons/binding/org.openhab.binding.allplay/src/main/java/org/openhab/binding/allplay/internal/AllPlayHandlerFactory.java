/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal;

import static org.openhab.binding.allplay.AllPlayBindingConstants.SPEAKER_THING_TYPE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
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
import org.openhab.binding.allplay.AllPlayBindingConstants;
import org.openhab.binding.allplay.handler.AllPlayHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.AllPlay;
import de.kaizencode.tchaikovsky.exception.AllPlayException;

/**
 * The {@link AllPlayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dominic Lerbs - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.allplay")
public class AllPlayHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AllPlayHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SPEAKER_THING_TYPE);
    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private AllPlay allPlay;
    private AllPlayBindingProperties bindingProperties;

    private AudioHTTPServer audioHTTPServer;
    private NetworkAddressService networkAddressService;
    private String callbackUrl;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(AllPlayBindingConstants.SPEAKER_THING_TYPE)) {
            logger.debug("Creating AllPlayHandler for thing {}", thing.getUID());

            AllPlayHandler handler = new AllPlayHandler(thing, allPlay, bindingProperties);
            registerAudioSink(thing, handler);

            return handler;
        }
        return null;
    }

    private void registerAudioSink(Thing thing, AllPlayHandler handler) {
        AllPlayAudioSink audioSink = new AllPlayAudioSink(handler, audioHTTPServer, callbackUrl);
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
        audioSinkRegistrations.put(thing.getUID().toString(), reg);
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        unregisterAudioSink(thing);
    }

    private void unregisterAudioSink(Thing thing) {
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
        if (reg != null) {
            reg.unregister();
        }
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("Activating AllPlayHandlerFactory");
        allPlay = new AllPlay("openHAB2");
        try {
            logger.debug("Connecting to AllPlay");
            allPlay.connect();
        } catch (AllPlayException e) {
            logger.error("Cannot initialize AllPlay", e);
        }
        Dictionary<String, Object> properties = componentContext.getProperties();
        bindingProperties = new AllPlayBindingProperties(properties);
        callbackUrl = assembleCallbackUrl();
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating AllPlayHandlerFactory");
        allPlay.disconnect();
        allPlay = null;
        super.deactivate(componentContext);
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

    private String assembleCallbackUrl() {
        String callbackUrl = bindingProperties.getCallbackUrl();
        if (callbackUrl == null) {
            String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
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
            callbackUrl = "http://" + ipAddress + ":" + port;
        }
        return callbackUrl;
    }

}
