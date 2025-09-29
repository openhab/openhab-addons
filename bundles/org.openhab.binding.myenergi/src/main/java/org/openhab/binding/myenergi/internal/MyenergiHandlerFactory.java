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
package org.openhab.binding.myenergi.internal;

import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.handler.MyenergiBridgeHandler;
import org.openhab.binding.myenergi.internal.handler.MyenergiEddiHandler;
import org.openhab.binding.myenergi.internal.handler.MyenergiHarviHandler;
import org.openhab.binding.myenergi.internal.handler.MyenergiZappiHandler;
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
 * The {@link MyenergiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rene Scherer - Initial contribution
 * @author Stephen Cook - Eddi Support
 */
@NonNullByDefault
@Component(configurationPid = "binding.myenergi", service = ThingHandlerFactory.class)
public class MyenergiHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MyenergiHandlerFactory.class);

    private MyenergiApiClient apiClient = new MyenergiApiClient();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, MyenergiBindingConstants.SUPPORTED_THING_TYPES_UIDS)
            .flatMap(Collection::stream).collect(Collectors.toSet());

    @Activate
    public MyenergiHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        apiClient.setHttpClientFactory(httpClientFactory);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler - create handler for {}", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new MyenergiBridgeHandler((Bridge) thing, apiClient);
        } else if (THING_TYPE_ZAPPI.equals(thingTypeUID)) {
            return new MyenergiZappiHandler(thing);
        } else if (THING_TYPE_HARVI.equals(thingTypeUID)) {
            return new MyenergiHarviHandler(thing);
        } else if (THING_TYPE_EDDI.equals(thingTypeUID)) {
            return new MyenergiEddiHandler(thing);
        }
        return null;
    }
}
