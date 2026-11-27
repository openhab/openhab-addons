/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Builds the things and handlers the tests of {@link PhilipsAirHandler} run with.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
final class PhilipsAirHandlerFixture {

    private PhilipsAirHandlerFixture() {
    }

    static Configuration configuration(String host) {
        Configuration configuration = new Configuration();
        configuration.put(PhilipsAirConfiguration.CONFIG_HOST, host);
        return configuration;
    }

    static Thing thing(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            Map<String, String> properties, List<Channel> channels) {
        return ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(configuration).withProperties(properties)
                .withChannels(channels).build();
    }

    static PhilipsAirHandler handler(Thing thing, HttpClient httpClient,
            PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider, ThingHandlerCallback callback) {
        PhilipsAirHandler handler = new PhilipsAirHandler(thing, httpClient, stateDescriptionProvider);
        handler.setCallback(callback);
        return handler;
    }
}
