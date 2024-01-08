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
package org.openhab.binding.astro.handler.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.astro.internal.AstroBindingConstants.THING_TYPE_SUN;
import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * OSGi test for the {@link AstroThingHandler}
 * <p>
 * This class tests the commands for the astro thing.
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Reworked to plain unit tests
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
public class AstroCommandTest {

    @Test
    public void testRefreshCommandUpdatesTheStateOfTheChannels() {
        ThingUID thingUID = new ThingUID(THING_TYPE_SUN, TEST_SUN_THING_ID);
        ChannelUID channelUID = new ChannelUID(thingUID, DEFAULT_TEST_CHANNEL_ID);
        Channel channel = ChannelBuilder.create(channelUID, DEFAULT_IMEM_TYPE).build();

        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);

        Thing thing = mock(Thing.class);
        when(thing.getConfiguration()).thenReturn(thingConfiguration);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getChannel(DEFAULT_TEST_CHANNEL_ID)).thenReturn(channel);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        CronScheduler cronScheduler = mock(CronScheduler.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        AstroThingHandler sunHandler = spy(new SunHandler(thing, cronScheduler, timeZoneProvider));

        // Required from the AstroThingHandler to send the status update
        doReturn(true).when(callback).isChannelLinked(eq(channelUID));
        doReturn(new Sun()).when(sunHandler).getPlanet();
        sunHandler.setCallback(callback);

        sunHandler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(callback, times(1)).stateUpdated(eq(channelUID), any(State.class));
    }
}
