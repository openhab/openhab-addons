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
package org.openhab.binding.nuki.internal;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.nuki.internal.dataexchange.NukiApiServlet;
import org.openhab.binding.nuki.internal.handler.NukiBridgeHandler;
import org.openhab.binding.nuki.internal.handler.NukiSmartLockHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Katter - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nuki")
@NonNullByDefault
public class NukiHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NukiHandlerFactory.class);

    private final HttpService httpService;
    private final HttpClient httpClient;
    private final NetworkAddressService networkAddressService;
    private @Nullable String callbackUrl;
    private @Nullable NukiApiServlet nukiApiServlet;

    @Activate
    public NukiHandlerFactory(@Reference HttpService httpService, @Reference final HttpClientFactory httpClientFactory,
            @Reference NetworkAddressService networkAddressService) {
        this.httpService = httpService;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.networkAddressService = networkAddressService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NukiBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("NukiHandlerFactory:createHandler({})", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NukiBindingConstants.THING_TYPE_BRIDGE_UIDS.contains(thingTypeUID)) {
            callbackUrl = createCallbackUrl();
            NukiBridgeHandler nukiBridgeHandler = new NukiBridgeHandler((Bridge) thing, httpClient, callbackUrl);
            if (!nukiBridgeHandler.isInitializable()) {
                return null;
            }
            if (nukiApiServlet == null) {
                nukiApiServlet = new NukiApiServlet(httpService);
            }
            nukiApiServlet.add(nukiBridgeHandler);
            return nukiBridgeHandler;
        } else if (NukiBindingConstants.THING_TYPE_SMARTLOCK_UIDS.contains(thingTypeUID)) {
            return new NukiSmartLockHandler(thing);
        }
        logger.trace("No valid Handler found for Thing[{}]!", thingTypeUID);
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        logger.trace("NukiHandlerFactory:unregisterHandler({})", thing);
        if (thing.getHandler() instanceof NukiBridgeHandler && nukiApiServlet != null) {
            nukiApiServlet.remove((NukiBridgeHandler) thing.getHandler());
            if (nukiApiServlet.countNukiBridgeHandlers() == 0) {
                nukiApiServlet = null;
            }
        }
    }

    private @Nullable String createCallbackUrl() {
        logger.trace("createCallbackUrl()");
        if (callbackUrl != null) {
            return callbackUrl;
        }
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
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(ipAddress + ":" + port);
        String callbackUrl = String.format(NukiBindingConstants.CALLBACK_URL, parameters.toArray());
        logger.trace("callbackUrl[{}]", callbackUrl);
        return callbackUrl;
    }
}
