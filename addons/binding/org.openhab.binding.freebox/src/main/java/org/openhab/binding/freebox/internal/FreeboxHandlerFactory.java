/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.core.Configuration;
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
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.handler.FreeboxHandler;
import org.openhab.binding.freebox.handler.FreeboxThingHandler;
import org.openhab.binding.freebox.internal.discovery.FreeboxDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link FreeboxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - several thing types and handlers + discovery service
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.freebox", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class FreeboxHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(FreeboxHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private AudioHTTPServer audioHTTPServer;
    private NetworkAddressService networkAddressService;

    private Map<ThingUID, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // url (scheme+server+port) to use for playing notification sounds
    private String callbackUrl;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            FreeboxBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS, FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS);

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
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (thingTypeUID.equals(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the Freebox binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            FreeboxHandler handler = new FreeboxHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            FreeboxThingHandler handler = new FreeboxThingHandler(thing);
            if (FreeboxBindingConstants.FREEBOX_THING_TYPE_AIRPLAY.equals(thingTypeUID)) {
                registerAudioSink(handler);
            }
            return handler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof FreeboxHandler) {
            unregisterDiscoveryService(thingHandler.getThing());
        } else if (thingHandler instanceof FreeboxThingHandler) {
            unregisterAudioSink(thingHandler.getThing());
        }
        super.removeHandler(thingHandler);
    }

    private void registerDiscoveryService(FreeboxHandler bridgeHandler) {
        FreeboxDiscoveryService discoveryService = new FreeboxDiscoveryService(bridgeHandler);
        discoveryService.activate();
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private void unregisterDiscoveryService(Thing thing) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(thing.getUID());
        if (serviceReg != null) {
            // remove discovery service, if bridge handler is removed
            FreeboxDiscoveryService service = (FreeboxDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            service.deactivate();
            serviceReg.unregister();
            discoveryServiceRegs.remove(thing.getUID());
        }
    }

    private void registerAudioSink(FreeboxThingHandler thingHandler) {
        String callbackUrl = createCallbackUrl();
        FreeboxAirPlayAudioSink audioSink = new FreeboxAirPlayAudioSink(thingHandler, audioHTTPServer, callbackUrl);
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
        audioSinkRegistrations.put(thingHandler.getThing().getUID(), reg);
    }

    private void unregisterAudioSink(Thing thing) {
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID());
        if (reg != null) {
            reg.unregister();
        }
    }

    private String createCallbackUrl() {
        if (callbackUrl != null) {
            return callbackUrl;
        } else {
            String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }

            // we do not use SSL as it can cause certificate validation issues.
            int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port == -1) {
                logger.warn("Cannot find port of the http service.");
                return null;
            }

            return "http://" + ipAddress + ":" + port;
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

}
