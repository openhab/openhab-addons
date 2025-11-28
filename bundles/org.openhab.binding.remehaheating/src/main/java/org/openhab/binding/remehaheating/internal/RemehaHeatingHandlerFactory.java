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
package org.openhab.binding.remehaheating.internal;

import static org.openhab.binding.remehaheating.internal.RemehaHeatingBindingConstants.THING_TYPE_BOILER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RemehaHeatingHandlerFactory} is responsible for creating things and thing handlers.
 * 
 * This factory creates handlers for supported Remeha heating system things.
 * It implements the OSGi component pattern and is automatically registered
 * as a ThingHandlerFactory service.
 * 
 * Currently supports:
 * - Remeha boiler things (THING_TYPE_BOILER)
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.remehaheating", service = ThingHandlerFactory.class)
public class RemehaHeatingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BOILER);
    private final HttpClientFactory httpClientFactory;

    @Activate
    public RemehaHeatingHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Checks if this factory supports the given thing type.
     * 
     * @param thingTypeUID The thing type UID to check
     * @return true if the thing type is supported, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a thing handler for the given thing.
     * 
     * @param thing The thing for which to create a handler
     * @return A new handler instance or null if the thing type is not supported
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BOILER.equals(thingTypeUID)) {
            HttpClient httpClient = httpClientFactory.createHttpClient("remehaheating");
            httpClient.setRequestBufferSize(16384);
            httpClient.setResponseBufferSize(16384);
            try {
                httpClient.start();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start HTTP client", e);
            }
            return new RemehaHeatingHandler(thing, httpClient);
        }

        return null;
    }
}
