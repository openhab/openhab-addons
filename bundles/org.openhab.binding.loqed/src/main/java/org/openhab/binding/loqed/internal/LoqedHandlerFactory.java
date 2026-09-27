/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.loqed.internal;

import static org.openhab.binding.loqed.internal.LoqedBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LoqedHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.loqed", service = ThingHandlerFactory.class)
public class LoqedHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_ACCOUNT, BRIDGE_TYPE_LOCAL,
            THING_TYPE_LOCK);
    private final Logger logger = LoggerFactory.getLogger(LoqedHandlerFactory.class);
    private final HttpClientFactory httpClientFactory;
    private final NetworkAddressService networkAddressService;
    private final LoqedWebhookServlet webhookServlet;

    @Activate
    public LoqedHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference NetworkAddressService networkAddressService, @Reference LoqedWebhookServlet webhookServlet) {
        this.httpClientFactory = httpClientFactory;
        this.networkAddressService = networkAddressService;
        this.webhookServlet = webhookServlet;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new LoqedBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient());
        } else if (BRIDGE_TYPE_LOCAL.equals(thingTypeUID)) {
            String routeId = InstanceUUID.get() + "_" + thing.getUID().toString().replace(':', '_');
            LoqedLocalBridgeHandler handler = new LoqedLocalBridgeHandler((Bridge) thing,
                    httpClientFactory.getCommonHttpClient(), routeId, createCallbackBaseUrl());
            webhookServlet.addHandler(routeId, handler);
            return handler;
        } else if (THING_TYPE_LOCK.equals(thingTypeUID)) {
            return new LoqedLockHandler(thing);
        }

        return null;
    }

    private @Nullable String createCallbackBaseUrl() {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No primary IPv4 network interface could be found for the LOQED webhook");
            return null;
        }

        // HTTP avoids certificate validation problems on local-network bridges.
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Could not determine the openHAB HTTP port for the LOQED webhook");
            return null;
        }
        return "http://" + ipAddress + ":" + port;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof LoqedLocalBridgeHandler localHandler) {
            webhookServlet.removeHandler(localHandler);
        }
        super.removeHandler(thingHandler);
    }
}
