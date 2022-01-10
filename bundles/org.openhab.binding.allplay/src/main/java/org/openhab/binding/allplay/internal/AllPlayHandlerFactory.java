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
package org.openhab.binding.allplay.internal;

import static org.openhab.binding.allplay.internal.AllPlayBindingConstants.SPEAKER_THING_TYPE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.allplay.internal.handler.AllPlayHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
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

import de.kaizencode.tchaikovsky.AllPlay;
import de.kaizencode.tchaikovsky.exception.AllPlayException;

/**
 * The {@link AllPlayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dominic Lerbs - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.allplay")
public class AllPlayHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AllPlayHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SPEAKER_THING_TYPE);
    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private AllPlay allPlay;
    private AllPlayBindingProperties bindingProperties;

    // Bindings should not use the ThingRegistry! See https://github.com/openhab/openhab-addons/pull/6080 and
    // https://github.com/eclipse/smarthome/issues/5182
    private final ThingRegistry thingRegistry;
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private String callbackUrl;

    @Activate
    public AllPlayHandlerFactory(final @Reference ThingRegistry thingRegistry,
            final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService) {
        this.thingRegistry = thingRegistry;
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(SPEAKER_THING_TYPE)) {
            logger.debug("Creating AllPlayHandler for thing {}", thing.getUID());

            AllPlayHandler handler = new AllPlayHandler(thingRegistry, thing, allPlay, bindingProperties);
            registerAudioSink(thing, handler);

            return handler;
        }
        return null;
    }

    private void registerAudioSink(Thing thing, AllPlayHandler handler) {
        AllPlayAudioSink audioSink = new AllPlayAudioSink(handler, audioHTTPServer, callbackUrl);
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
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
