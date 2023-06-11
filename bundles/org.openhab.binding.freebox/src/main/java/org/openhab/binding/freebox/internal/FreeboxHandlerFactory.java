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
package org.openhab.binding.freebox.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.handler.FreeboxHandler;
import org.openhab.binding.freebox.internal.handler.FreeboxThingHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link FreeboxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - several thing types and handlers + discovery service
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.freebox")
public class FreeboxHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(FreeboxBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS.stream(),
                    FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(FreeboxHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final TimeZoneProvider timeZoneProvider;

    // url (scheme+server+port) to use for playing notification sounds
    private @Nullable String callbackUrl;

    @Activate
    public FreeboxHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.timeZoneProvider = timeZoneProvider;
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
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (thingTypeUID.equals(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null && thingUID != null) {
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
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            return new FreeboxHandler((Bridge) thing);
        } else if (FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            FreeboxThingHandler handler = new FreeboxThingHandler(thing, timeZoneProvider);
            if (FreeboxBindingConstants.FREEBOX_THING_TYPE_AIRPLAY.equals(thingTypeUID)) {
                registerAudioSink(handler);
            }
            return handler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof FreeboxThingHandler) {
            unregisterAudioSink(thingHandler.getThing());
        }
    }

    private synchronized void registerAudioSink(FreeboxThingHandler thingHandler) {
        String callbackUrl = createCallbackUrl();
        FreeboxAirPlayAudioSink audioSink = new FreeboxAirPlayAudioSink(thingHandler, audioHTTPServer, callbackUrl);
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
        audioSinkRegistrations.put(thingHandler.getThing().getUID(), reg);
    }

    private synchronized void unregisterAudioSink(Thing thing) {
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.remove(thing.getUID());
        if (reg != null) {
            reg.unregister();
        }
    }

    private @Nullable String createCallbackUrl() {
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
}
