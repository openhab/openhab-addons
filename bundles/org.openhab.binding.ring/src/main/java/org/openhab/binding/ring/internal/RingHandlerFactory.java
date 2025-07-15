/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal;

import static org.openhab.binding.ring.RingBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.ring.internal.handler.AccountHandler;
import org.openhab.binding.ring.internal.handler.ChimeHandler;
import org.openhab.binding.ring.internal.handler.DoorbellHandler;
import org.openhab.binding.ring.internal.handler.OtherDeviceHandler;
import org.openhab.binding.ring.internal.handler.StickupcamHandler;
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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Wim Vissers - Initial contribution
 * @author Chris Milbert - Stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@Component(service = { ThingHandlerFactory.class,
        RingHandlerFactory.class }, immediate = true, configurationPid = "binding.ring")
@NonNullByDefault
public class RingHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(RingHandlerFactory.class);

    private final NetworkAddressService networkAddressService;

    private final HttpClient httpClient;
    private final RingVideoServlet servlet;
    private int httpPort;

    public final Gson gson = new Gson();

    @Activate
    public RingHandlerFactory(@Reference NetworkAddressService networkAddressService,
            @Reference RingVideoServlet servlet, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) throws Exception {
        super.activate(componentContext);
        httpPort = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
        if (httpPort == -1) {
            httpPort = 8080;
        }
        this.servlet = servlet;
        this.networkAddressService = networkAddressService;

        logger.debug("Using OH HTTP port {}", httpPort);

        httpClient = httpClientFactory.createHttpClient("ring", new SslContextFactory.Client());
        httpClient.start();
    }

    @Deactivate
    public void deactivate() throws Exception {
        httpClient.stop();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.info("createHandler thingType: {}", thingTypeUID);
        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            if (thing instanceof Bridge bridge) {
                return new AccountHandler(bridge, networkAddressService, servlet, httpPort, httpClient);
            } else {
                logger.warn("Account Bridge configured as legacy Thing");
                return null;
            }
        } else if (thingTypeUID.equals(THING_TYPE_DOORBELL)) {
            return new DoorbellHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CHIME)) {
            return new ChimeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_STICKUPCAM)) {
            return new StickupcamHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OTHERDEVICE)) {
            return new OtherDeviceHandler(thing);
        }
        return null;
    }
}
