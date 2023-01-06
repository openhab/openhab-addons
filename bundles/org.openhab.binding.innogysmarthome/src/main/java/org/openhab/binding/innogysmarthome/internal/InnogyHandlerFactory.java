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
package org.openhab.binding.innogysmarthome.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.innogysmarthome.internal.handler.InnogyBridgeHandler;
import org.openhab.binding.innogysmarthome.internal.handler.InnogyDeviceHandler;
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
 * The {@link InnogyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.innogysmarthome")
@NonNullByDefault
public class InnogyHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(InnogyBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    InnogyDeviceHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(InnogyHandlerFactory.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    @Activate
    public InnogyHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory) {
        this.oAuthFactory = oAuthFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (InnogyBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new InnogyBridgeHandler((Bridge) thing, oAuthFactory, httpClient);
        } else if (InnogyDeviceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new InnogyDeviceHandler(thing);
        } else {
            logger.debug("Unsupported thing {}.", thing.getThingTypeUID());
            return null;
        }
    }
}
