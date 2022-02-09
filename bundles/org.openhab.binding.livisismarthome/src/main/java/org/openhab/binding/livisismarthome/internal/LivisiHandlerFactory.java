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
package org.openhab.binding.livisismarthome.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeHandler;
import org.openhab.binding.livisismarthome.internal.handler.LivisiDeviceHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link LivisiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.livisismarthome")
@NonNullByDefault
public class LivisiHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(LivisiBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    LivisiDeviceHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(LivisiHandlerFactory.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    @Activate
    public LivisiHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory) {
        this.oAuthFactory = oAuthFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (LivisiBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new LivisiBridgeHandler((Bridge) thing, oAuthFactory, httpClient);
        } else if (LivisiDeviceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new LivisiDeviceHandler(thing);
        } else {
            logger.debug("Unsupported thing {}.", thing.getThingTypeUID());
            return null;
        }
    }
}
