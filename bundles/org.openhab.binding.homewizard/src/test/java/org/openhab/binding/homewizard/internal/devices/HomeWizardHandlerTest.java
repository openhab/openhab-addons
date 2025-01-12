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
package org.openhab.binding.homewizard.internal.devices;

import static org.mockito.Mockito.when;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Tests for the HomeWizard Handler
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardHandlerTest {

    protected static final Configuration CONFIG = createConfig();

    protected static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("ipAddress", "192.168.1.1");
        return config;
    }

    protected static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    protected static Channel mockChannel(final ThingUID thingId, final String groupId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, groupId + "#" + channelId));
        return channel;
    }

    protected static State getState(final int input) {
        return new DecimalType(input);
    }

    protected static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    protected static State getState(final double input) {
        return new DecimalType(input);
    }

    protected static State getState(final boolean input) {
        return OnOffType.from(input);
    }

    protected static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    protected static ChannelUID getEnergyChannelUid(Thing thing, String channelId) {
        return new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY + "#" + channelId);
    }
}
