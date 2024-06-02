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
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.devices.rf.smartcontact.TapoSmartContactHandler;
import org.openhab.binding.tapocontrol.internal.devices.rf.wheatersensor.TapoWheaterSensorHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoUniversalDeviceHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.bulb.TapoBulbHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.hub.TapoHubHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.lightstrip.TapoLightStripHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.socket.TapoSocketHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.socket.TapoSocketStripHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link TapoControlHandlerFactory} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Wild - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tapocontrol")
@NonNullByDefault
public class TapoControlHandlerFactory extends BaseThingHandlerFactory {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation()
            .create();
    private final Logger logger = LoggerFactory.getLogger(TapoControlHandlerFactory.class);
    private final Set<TapoBridgeHandler> accountHandlers = new HashSet<>();
    private final HttpClient httpClient;

    @Activate
    public TapoControlHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        // create new httpClient
        httpClient = httpClientFactory.createHttpClient(BINDING_ID, new SslContextFactory.Client());
        httpClient.setFollowRedirects(false);
        httpClient.setMaxConnectionsPerDestination(HTTP_MAX_CONNECTIONS);
        httpClient.setMaxRequestsQueuedPerDestination(HTTP_MAX_QUEUED_REQUESTS);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("cannot start httpClient");
        }
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("unable to stop httpClient");
        }
    }

    /**
     * Provides the supported thing types
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (thingTypeUID.equals(UNIVERSAL_THING_TYPE)) {
            return true;
        }
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Create handler of things.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_BRIDGE_UIDS.contains(thingTypeUID)) {
            TapoBridgeHandler bridgeHandler = new TapoBridgeHandler((Bridge) thing, httpClient);
            accountHandlers.add(bridgeHandler);
            return bridgeHandler;
        } else if (SUPPORTED_HUB_UIDS.contains(thingTypeUID)) {
            return new TapoHubHandler(thing);
        } else if (SUPPORTED_SOCKET_UIDS.contains(thingTypeUID)) {
            return new TapoSocketHandler(thing);
        } else if (SUPPORTED_SOCKET_STRIP_UIDS.contains(thingTypeUID)) {
            return new TapoSocketStripHandler(thing);
        } else if (SUPPORTED_WHITE_BULB_UIDS.contains(thingTypeUID)) {
            return new TapoBulbHandler(thing);
        } else if (SUPPORTED_COLOR_BULB_UIDS.contains(thingTypeUID)) {
            return new TapoBulbHandler(thing);
        } else if (SUPPORTED_LIGHT_STRIP_UIDS.contains(thingTypeUID)) {
            return new TapoLightStripHandler(thing);
        } else if (SUPPORTED_SMART_CONTACTS.contains(thingTypeUID)) {
            return new TapoSmartContactHandler(thing);
        } else if (SUPPORTED_WHEATHER_SENSORS.contains(thingTypeUID)) {
            return new TapoWheaterSensorHandler(thing);
        } else if (thingTypeUID.equals(UNIVERSAL_THING_TYPE)) {
            return new TapoUniversalDeviceHandler(thing);
        }
        return null;
    }
}
