/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.astro.handler.test;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.THING_TYPE_SUN;
import static org.eclipse.smarthome.binding.astro.test.cases.AstroBindingTestsData.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.handler.SunHandler;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

/**
 * OSGi test for the {@link AstroThingHandler}
 * <p>
 * This class tests the commands for the astro thing.
 *
 * @author Petar Valchev - Initial implementation
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
        AstroThingHandler sunHandler = spy(new SunHandler(thing, cronScheduler));

        // Required from the AstroThingHandler to send the status update
        doReturn(true).when(callback).isChannelLinked(eq(channelUID));
        doReturn(new Sun()).when(sunHandler).getPlanet();
        sunHandler.setCallback(callback);

        sunHandler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(callback, times(1)).stateUpdated(eq(channelUID), any(State.class));
    }
}
