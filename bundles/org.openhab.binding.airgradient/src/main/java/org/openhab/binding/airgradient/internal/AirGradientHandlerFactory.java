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
package org.openhab.binding.airgradient.internal;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.airgradient.internal.handler.AirGradientAPIHandler;
import org.openhab.binding.airgradient.internal.handler.AirGradientLocalHandler;
import org.openhab.binding.airgradient.internal.handler.AirGradientLocationHandler;
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
 * The {@link AirGradientHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.airgradient", service = ThingHandlerFactory.class)
public class AirGradientHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_LOCATION,
            THING_TYPE_LOCAL);

    private final Logger logger = LoggerFactory.getLogger(AirGradientHandlerFactory.class);
    private final HttpClient httpClient;

    @Activate
    public AirGradientHandlerFactory(final @Reference HttpClientFactory factory) {
        logger.debug("Activating factory for: {}", SUPPORTED_THING_TYPES_UIDS);
        this.httpClient = factory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("We support: {}", SUPPORTED_THING_TYPES_UIDS);
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API.equals(thingTypeUID)) {
            logger.debug("Creating Bridge Handler for {}", thingTypeUID);
            return new AirGradientAPIHandler((Bridge) thing, httpClient);
        }

        if (THING_TYPE_LOCATION.equals(thingTypeUID)) {
            logger.debug("Creating Location Handler for {}", thingTypeUID);
            return new AirGradientLocationHandler(thing);
        }

        if (THING_TYPE_LOCAL.equals(thingTypeUID)) {
            logger.debug("Creating Local Handler for {}", thingTypeUID);
            return new AirGradientLocalHandler(thing, httpClient);
        }

        return null;
    }
}
