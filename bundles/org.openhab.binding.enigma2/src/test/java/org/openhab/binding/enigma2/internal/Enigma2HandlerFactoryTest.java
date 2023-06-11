/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.internal;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.enigma2.internal.Enigma2BindingConstants.THING_TYPE_DEVICE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Enigma2HandlerFactoryTest} class is responsible for testing {@link Enigma2HandlerFactory}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class Enigma2HandlerFactoryTest {
    @Nullable
    private Thing thing;
    @Nullable
    private Configuration configuration;

    @Test
    public void testSupportsThingType() {
        assertThat(new Enigma2HandlerFactory().supportsThingType(Enigma2BindingConstants.THING_TYPE_DEVICE), is(true));
    }

    @Test
    public void testSupportsThingTypeFalse() {
        assertThat(new Enigma2HandlerFactory().supportsThingType(new ThingTypeUID("any", "device")), is(false));
    }

    @Test
    public void testCreateHandlerNull() {
        thing = mock(Thing.class);
        assertThat(new Enigma2HandlerFactory().createHandler(requireNonNull(thing)), is(nullValue()));
    }

    @Test
    public void testCreateHandler() {
        thing = mock(Thing.class);
        configuration = mock(Configuration.class);
        when(thing.getConfiguration()).thenReturn(requireNonNull(configuration));
        when(configuration.as(Enigma2Configuration.class)).thenReturn(new Enigma2Configuration());
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_DEVICE);
        assertThat(new Enigma2HandlerFactory().createHandler(requireNonNull(thing)), is(notNullValue()));
    }
}
