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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.handler.DeltaHandler;
import org.openhab.binding.freebox.internal.handler.HostHandler;
import org.openhab.binding.freebox.internal.handler.PhoneHandler;
import org.openhab.binding.freebox.internal.handler.PlayerHandler;
import org.openhab.binding.freebox.internal.handler.RevolutionHandler;
import org.openhab.binding.freebox.internal.handler.VirtualMachineHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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

    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final ZoneId zoneId;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable String callbackUrl;

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
            return new PlayerHandler(thing, zoneId, audioHTTPServer, callbackUrl, bundleContext);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_HOST)) {
            return new HostHandler(thing, zoneId);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_PHONE)) {
            return new PhoneHandler(thing, zoneId);
        } else if (thingTypeUID.equals(FREEBOX_THING_TYPE_VM)) {
            return new VirtualMachineHandler(thing, zoneId);
        }
        return null;
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
