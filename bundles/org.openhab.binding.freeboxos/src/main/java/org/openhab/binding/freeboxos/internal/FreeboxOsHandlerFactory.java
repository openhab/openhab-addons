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
package org.openhab.binding.freeboxos.internal;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Category;
import org.openhab.binding.freeboxos.internal.handler.ActivePlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.AlarmHandler;
import org.openhab.binding.freeboxos.internal.handler.BasicShutterHandler;
import org.openhab.binding.freeboxos.internal.handler.CallHandler;
import org.openhab.binding.freeboxos.internal.handler.CameraHandler;
import org.openhab.binding.freeboxos.internal.handler.DectHandler;
import org.openhab.binding.freeboxos.internal.handler.FreeboxOsHandler;
import org.openhab.binding.freeboxos.internal.handler.FreeplugHandler;
import org.openhab.binding.freeboxos.internal.handler.FxsHandler;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.handler.KeyfobHandler;
import org.openhab.binding.freeboxos.internal.handler.PlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.RepeaterHandler;
import org.openhab.binding.freeboxos.internal.handler.RevolutionHandler;
import org.openhab.binding.freeboxos.internal.handler.ServerHandler;
import org.openhab.binding.freeboxos.internal.handler.ShutterHandler;
import org.openhab.binding.freeboxos.internal.handler.VmHandler;
import org.openhab.binding.freeboxos.internal.handler.WifiStationHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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

/**
 * The {@link FreeboxOsHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.freeboxos")
public class FreeboxOsHandlerFactory extends BaseThingHandlerFactory {
    private static final String TIMEOUT = "timeout";
    private static final String CALLBACK_URL = "callBackUrl";

    private final Logger logger = LoggerFactory.getLogger(FreeboxOsHandlerFactory.class);

    private final NetworkAddressService networkAddressService;
    private final AudioHTTPServer audioHTTPServer;
    private final HttpClient httpClient;
    private final ApiHandler apiHandler;
    private String callbackURL = "";

    @Activate
    public FreeboxOsHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference HttpClientFactory httpClientFactory, final @Reference TimeZoneProvider timeZoneProvider,
            ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);

        this.audioHTTPServer = audioHTTPServer;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.networkAddressService = networkAddressService;
        this.apiHandler = new ApiHandler(httpClient, timeZoneProvider);

        configChanged(config);
    }

    @Modified
    public void configChanged(Map<String, Object> config) {
        String timeout = (String) config.getOrDefault(TIMEOUT, "8");
        apiHandler.setTimeout(TimeUnit.SECONDS.toMillis(Long.parseLong(timeout)));

        callbackURL = (String) config.getOrDefault(CALLBACK_URL, "");
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (callbackURL.isEmpty() && port != -1) {
            String openHabIp = Objects.requireNonNull(networkAddressService.getPrimaryIpv4HostAddress());
            // we do not use SSL as it can cause certificate validation issues.
            callbackURL = "http://%s:%d".formatted(openHabIp, port);
        }
        if (callbackURL.isEmpty()) {
            logger.warn("Unable to build a correct call back URL to stream media contents");
            return;
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_API.equals(thingTypeUID)) {
            return new FreeboxOsHandler((Bridge) thing, new FreeboxOsSession(apiHandler), callbackURL, bundleContext,
                    audioHTTPServer);
        } else if (THING_TYPE_FREEPLUG.equals(thingTypeUID)) {
            return new FreeplugHandler(thing);
        } else if (THING_TYPE_FXS.equals(thingTypeUID)) {
            return new FxsHandler(thing);
        } else if (THING_TYPE_DECT.equals(thingTypeUID)) {
            return new DectHandler(thing);
        } else if (THING_TYPE_CALL.equals(thingTypeUID)) {
            return new CallHandler(thing);
        } else if (THING_TYPE_REVOLUTION.equals(thingTypeUID)) {
            return new RevolutionHandler(thing);
        } else if (THING_TYPE_DELTA.equals(thingTypeUID)) {
            return new ServerHandler(thing);
        } else if (THING_TYPE_HOST.equals(thingTypeUID)) {
            return new HostHandler(thing);
        } else if (THING_TYPE_WIFI_HOST.equals(thingTypeUID)) {
            return new WifiStationHandler(thing);
        } else if (THING_TYPE_REPEATER.equals(thingTypeUID)) {
            return new RepeaterHandler(thing);
        } else if (THING_TYPE_VM.equals(thingTypeUID)) {
            return new VmHandler(thing);
        } else if (THING_TYPE_ACTIVE_PLAYER.equals(thingTypeUID)) {
            return new ActivePlayerHandler(thing);
        } else if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            return new PlayerHandler(thing);
        } else if (Category.BASIC_SHUTTER.getThingTypeUID().equals(thingTypeUID)) {
            return new BasicShutterHandler(thing);
        } else if (Category.SHUTTER.getThingTypeUID().equals(thingTypeUID)) {
            return new ShutterHandler(thing);
        } else if (Category.ALARM.getThingTypeUID().equals(thingTypeUID)) {
            return new AlarmHandler(thing);
        } else if (Category.KFB.getThingTypeUID().equals(thingTypeUID)) {
            return new KeyfobHandler(thing);
        } else if (Category.CAMERA.getThingTypeUID().equals(thingTypeUID)) {
            return new CameraHandler(thing);
        }

        return null;
    }
}
