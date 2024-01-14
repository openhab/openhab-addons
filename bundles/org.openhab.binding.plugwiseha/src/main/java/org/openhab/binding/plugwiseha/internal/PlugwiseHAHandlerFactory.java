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
package org.openhab.binding.plugwiseha.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHAApplianceHandler;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHABridgeHandler;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHAZoneHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PlugwiseHAHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 * 
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plugwiseha")
public class PlugwiseHAHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    // Constructor

    @Activate
    public PlugwiseHAHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    // Public methods

    /**
     * Returns whether the handler is able to create a thing or register a thing
     * handler for the given type.
     *
     * @param thingTypeUID the thing type UID
     * @return true, if the handler supports the thing type, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)
                || PlugwiseHAZoneHandler.supportsThingType(thingTypeUID))
                || PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID);
    }

    /**
     * Creates a thing for given arguments.
     *
     * @param thingTypeUID thing type uid (not null)
     * @param configuration configuration
     * @param thingUID thing uid, which can be null
     * @param bridgeUID bridge uid, which can be null
     * @return created thing
     */
    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (PlugwiseHAZoneHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else if (PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the plugwiseha binding.");
    }

    // Protected and private methods

    /**
     * Creates a {@link ThingHandler} for the given thing.
     *
     * @param thing the thing
     * @return thing the created handler
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)) {
            return new PlugwiseHABridgeHandler((Bridge) thing, this.httpClient);
        } else if (PlugwiseHAZoneHandler.supportsThingType(thingTypeUID)) {
            return new PlugwiseHAZoneHandler(thing);
        } else if (PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID)) {
            return new PlugwiseHAApplianceHandler(thing);
        }
        return null;
    }
}
