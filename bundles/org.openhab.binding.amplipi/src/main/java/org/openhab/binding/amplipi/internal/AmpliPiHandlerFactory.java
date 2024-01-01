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
package org.openhab.binding.amplipi.internal;

import static org.openhab.binding.amplipi.internal.AmpliPiBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
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
 * The {@link AmpliPiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.amplipi", service = ThingHandlerFactory.class)
public class AmpliPiHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AmpliPiHandlerFactory.class);

    private HttpClient httpClient;
    private AudioHTTPServer audioHttpServer;
    private final NetworkAddressService networkAddressService;

    @Activate
    public AmpliPiHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference AudioHTTPServer audioHttpServer, @Reference NetworkAddressService networkAddressService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.audioHttpServer = audioHttpServer;
        this.networkAddressService = networkAddressService;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CONTROLLER, THING_TYPE_ZONE,
            THING_TYPE_GROUP);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            String callbackUrl = createCallbackUrl();
            return new AmpliPiHandler(thing, httpClient, audioHttpServer, callbackUrl);
        }
        if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            return new AmpliPiZoneHandler(thing, httpClient);
        }
        if (THING_TYPE_GROUP.equals(thingTypeUID)) {
            return new AmpliPiGroupHandler(thing, httpClient);
        }

        return null;
    }

    private @Nullable String createCallbackUrl() {
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

        return "http://" + ipAddress + ":" + port;
    }
}
