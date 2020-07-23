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
package org.openhab.binding.freebox.internal;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.time.ZoneId;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.freebox.internal.handler.DeltaHandler;
import org.openhab.binding.freebox.internal.handler.HostHandler;
import org.openhab.binding.freebox.internal.handler.PhoneHandler;
import org.openhab.binding.freebox.internal.handler.PlayerHandler;
import org.openhab.binding.freebox.internal.handler.RevolutionHandler;
import org.openhab.binding.freebox.internal.handler.VirtualMachineHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private final Logger logger = LoggerFactory.getLogger(FreeboxHandlerFactory.class);
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final ZoneId zoneId;
    private @Nullable String callbackUrl;
    private final Map<ThingUID, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    @Activate
    public FreeboxHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference TimeZoneProvider timeZoneProvider, ComponentContext componentContext) {
        super.activate(componentContext);
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.zoneId = timeZoneProvider.getTimeZone();
        setCallbackUrl(componentContext.getProperties().get(CALLBACK_URL));
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        logger.debug("Updated binding configuration to {}", config);
        setCallbackUrl(config.get(CALLBACK_URL));
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(FREEBOX_BRIDGE_TYPE_REVOLUTION)) {
            return new RevolutionHandler((Bridge) thing, gson);
        } else if (thingTypeUID.equals(FREEBOX_BRIDGE_TYPE_DELTA)) {
            return new DeltaHandler((Bridge) thing, gson);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_PLAYER)) {
            PlayerHandler handler = new PlayerHandler(thing, zoneId, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), handler, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID(), reg);
            return handler;
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_HOST)) {
            return new HostHandler(thing, zoneId);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_PHONE)) {
            return new PhoneHandler(thing, zoneId);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_VM)) {
            return new VirtualMachineHandler(thing, zoneId);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PlayerHandler) {
            ServiceRegistration<AudioSink> reg = audioSinkRegistrations.remove(thingHandler.getThing().getUID());
            reg.unregister();
        }
    }

    private void setCallbackUrl(@Nullable Object url) {
        if (url != null) {
            callbackUrl = (String) url;
        } else {
            String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port != -1 && ipAddress != null) {
                // we do not use SSL as it can cause certificate validation issues.
                callbackUrl = String.format("http://%s:%d", ipAddress, port);
            } else {
                logger.warn("No network interface could be found or cannot find port of the http service.");
            }
        }
    }

}
