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
package org.openhab.binding.openweathermap.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mockito.Mockito;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;

/**
 * Utility class for working with test objects in unit tests
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class TestObjectsUtil {

    public static Configuration createConfig(boolean returnValid, @Nullable String apiVersion) {
        final Configuration config = new Configuration();
        if (returnValid) {
            config.put(OpenWeatherMapBindingConstants.CONFIG_LOCATION, "51.0435,7.2865");
            config.put(OpenWeatherMapBindingConstants.CONFIG_HISTORY_DAYS, 1);
            if (apiVersion != null) {
                config.put(OpenWeatherMapBindingConstants.CONFIG_API_VERSION, apiVersion);
            }
        }
        return config;
    }

    public static Thing mockThing(Configuration configuration) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(OpenWeatherMapBindingConstants.BINDING_ID, "owm-test-thing"));
        when(thing.getConfiguration()).thenReturn(configuration);
        return thing;
    }

    public static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        when(channel.getKind()).thenReturn(ChannelKind.STATE);

        return channel;
    }

    public static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    public static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }
}
