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
package org.openhab.binding.samsungtv.internal;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.SAMSUNG_TV_THING_TYPE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jupnp.UpnpService;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Added Component annotation
 * @author Nick Waterton - Added HttpClient for IP Control
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.samsungtv")
public class SamsungTvHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(SAMSUNG_TV_THING_TYPE);

    private @NonNullByDefault({}) UpnpIOService upnpIOService;
    private @NonNullByDefault({}) UpnpService upnpService;

    @Reference
    private @NonNullByDefault({}) WebSocketFactory webSocketFactory;

    private final Logger logger = LoggerFactory.getLogger(SamsungTvHandlerFactory.class);
    private final HttpClient ipControlHttpClient;

    @Activate
    public SamsungTvHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        // the TV's IP Control certificate is self signed
        ipControlHttpClient = httpClientFactory.createHttpClient("samsungtv-ipc", new SslContextFactory.Client(true));
        try {
            ipControlHttpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Could not start IP Control HTTP client", e);
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        try {
            ipControlHttpClient.stop();
        } catch (Exception e) {
            logger.debug("Failed to stop IP Control HTTP client: {}", e.getMessage());
        }
        super.deactivate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SAMSUNG_TV_THING_TYPE)) {
            return new SamsungTvHandler(thing, upnpIOService, upnpService, webSocketFactory, ipControlHttpClient);
        }

        return null;
    }

    @Reference
    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    @Reference
    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }
}
