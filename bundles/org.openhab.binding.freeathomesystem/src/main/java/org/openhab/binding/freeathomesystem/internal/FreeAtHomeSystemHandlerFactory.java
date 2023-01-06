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
package org.openhab.binding.freeathomesystem.internal;

import static org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeDeviceHandler;
import org.openhab.binding.freeathomesystem.internal.type.FreeAtHomeChannelGroupTypeProvider;
import org.openhab.binding.freeathomesystem.internal.type.FreeAtHomeChannelTypeProvider;
import org.openhab.binding.freeathomesystem.internal.type.FreeAtHomeThingTypeProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeSystemHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.freeathomesystem", service = ThingHandlerFactory.class)
public class FreeAtHomeSystemHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSystemHandlerFactory.class);

    private @NonNullByDefault({}) HttpClient httpClient;
    private final FreeAtHomeThingTypeProvider thingTypeProvider;
    private final FreeAtHomeChannelTypeProvider channelTypeProvider;
    private final FreeAtHomeChannelGroupTypeProvider channelGroupsTypeProvider;

    private void generteThingTypes() {
        String label = "free-at-home-device";
        String description = String.format("Generic free@home device");

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(BRIDGE_TYPE_UID.toString());
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, FREEATHOMEDEVICE_TYPE_ID);

        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, "Busch&Jaeger - ABB");
        properties.put(Thing.PROPERTY_MODEL_ID, "free@home Device");

        URI configDescriptionURI;
        try {
            configDescriptionURI = new URI("thing-type:freeathomesystem:free-at-home-device");

            ThingType thingType = ThingTypeBuilder.instance(thingTypeUID, label)
                    .withSupportedBridgeTypeUIDs(supportedBridgeTypeUids).withDescription(description)
                    .withProperties(properties).withConfigDescriptionURI(configDescriptionURI).build();

            this.thingTypeProvider.addThingType(thingType);
        } catch (URISyntaxException e) {
            logger.debug("Exception during creating config description URI");
        }

        return;
    }

    @Activate
    public FreeAtHomeSystemHandlerFactory(@Reference FreeAtHomeThingTypeProvider thingTypeProvider,
            @Reference FreeAtHomeChannelTypeProvider channelTypeProvider,
            @Reference FreeAtHomeChannelGroupTypeProvider channelGroupsTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
        this.channelTypeProvider = channelTypeProvider;
        this.channelGroupsTypeProvider = channelGroupsTypeProvider;

        generteThingTypes();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("Create SysAP bridge");
            return new FreeAtHomeBridgeHandler((Bridge) thing, httpClient);
        } else if (FREEATHOMEDEVICE_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("Create free@home device");
            return new FreeAtHomeDeviceHandler(thing, channelTypeProvider, channelGroupsTypeProvider);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.createHttpClient("FreeAtHome");
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
