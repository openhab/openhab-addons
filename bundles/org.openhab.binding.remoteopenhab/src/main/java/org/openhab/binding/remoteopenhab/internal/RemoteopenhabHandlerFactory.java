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
package org.openhab.binding.remoteopenhab.internal;

import static org.openhab.binding.remoteopenhab.internal.RemoteopenhabBindingConstants.*;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.remoteopenhab.internal.handler.RemoteopenhabBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link RemoteopenhabHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.remoteopenhab")
public class RemoteopenhabHandlerFactory extends BaseThingHandlerFactory {

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final RemoteopenhabChannelTypeProvider channelTypeProvider;
    private final RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider;
    private final Gson jsonParser;

    @Activate
    public RemoteopenhabHandlerFactory(final @Reference ClientBuilder clientBuilder,
            final @Reference SseEventSourceFactory eventSourceFactory,
            final @Reference RemoteopenhabChannelTypeProvider channelTypeProvider,
            final @Reference RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider) {
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        jsonParser = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    }

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for the specific thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return BRIDGE_TYPE_SERVER.equals(thingTypeUID)
                ? new RemoteopenhabBridgeHandler((Bridge) thing, clientBuilder, eventSourceFactory, channelTypeProvider,
                        stateDescriptionProvider, jsonParser)
                : null;
    }
}
