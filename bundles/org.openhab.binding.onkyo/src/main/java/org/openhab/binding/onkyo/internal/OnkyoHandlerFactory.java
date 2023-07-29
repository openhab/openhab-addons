/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onkyo.internal;

import static org.openhab.binding.onkyo.internal.OnkyoBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.onkyo.internal.handler.OnkyoHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnkyoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Frank - Initial contribution
 * @author Stewart Cossey - added dynamic state descriptor provider functions
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.onkyo")
public class OnkyoHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OnkyoHandlerFactory.class);

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private UpnpIOService upnpIOService;
    private AudioHTTPServer audioHTTPServer;
    private NetworkAddressService networkAddressService;
    private OnkyoStateDescriptionProvider stateDescriptionProvider;

    // url (scheme+server+port) to use for playing notification sounds
    private String callbackUrl;

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
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            String callbackUrl = createCallbackUrl();
            OnkyoHandler handler = new OnkyoHandler(thing, upnpIOService, audioHTTPServer, callbackUrl,
                    stateDescriptionProvider);
            if (callbackUrl != null) {
                @SuppressWarnings("unchecked")
                ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                        .registerService(AudioSink.class.getName(), handler, new Hashtable<>());
                audioSinkRegistrations.put(thing.getUID().toString(), reg);
            }
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
    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
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
    protected void setDynamicStateDescriptionProvider(OnkyoStateDescriptionProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(OnkyoStateDescriptionProvider provider) {
        this.stateDescriptionProvider = null;
    }
}
