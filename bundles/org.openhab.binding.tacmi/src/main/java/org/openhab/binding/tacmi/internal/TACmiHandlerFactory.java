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
package org.openhab.binding.tacmi.internal;

import static org.openhab.binding.tacmi.internal.TACmiBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tacmi.internal.coe.TACmiCoEBridgeHandler;
import org.openhab.binding.tacmi.internal.coe.TACmiHandler;
import org.openhab.binding.tacmi.internal.schema.TACmiSchemaHandler;
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

/**
 * The {@link TACmiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tacmi", service = ThingHandlerFactory.class)
public class TACmiHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_CMI, THING_TYPE_COE_BRIDGE, THING_TYPE_CMI_SCHEMA).collect(Collectors.toSet()));

    private final HttpClient httpClient;
    private final TACmiChannelTypeProvider channelTypeProvider;

    @Activate
    public TACmiHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TACmiChannelTypeProvider channelTypeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CMI.equals(thingTypeUID)) {
            return new TACmiHandler(thing);
        } else if (THING_TYPE_COE_BRIDGE.equals(thingTypeUID)) {
            return new TACmiCoEBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_CMI_SCHEMA.equals(thingTypeUID)) {
            return new TACmiSchemaHandler(thing, httpClient, channelTypeProvider);
        }

        return null;
    }
}
