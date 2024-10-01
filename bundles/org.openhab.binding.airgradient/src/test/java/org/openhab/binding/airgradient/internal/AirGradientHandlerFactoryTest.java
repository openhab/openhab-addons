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
package org.openhab.binding.airgradient.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Jørgen Austvik - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class AirGradientHandlerFactoryTest {

    @Test
    public void testSupportsThingTypes() {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        AirGradientHandlerFactory sut = new AirGradientHandlerFactory(httpClientFactoryMock);

        assertThat(sut.supportsThingType(AirGradientBindingConstants.THING_TYPE_API), is(true));
        assertThat(sut.supportsThingType(AirGradientBindingConstants.THING_TYPE_LOCATION), is(true));
        assertThat(sut.supportsThingType(new ThingTypeUID("unknown", "thingtype")), is(false));
    }

    @Test
    public void testCanCreateAPIHandler() {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        AirGradientHandlerFactory sut = new AirGradientHandlerFactory(httpClientFactoryMock);
        Bridge bridgeMock = Mockito.mock(Bridge.class);
        Mockito.when(bridgeMock.getThingTypeUID()).thenReturn(AirGradientBindingConstants.THING_TYPE_API);

        assertThat(sut.createHandler(bridgeMock), is(notNullValue()));
    }

    @Test
    public void testCanCreateLocationHandler() {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        AirGradientHandlerFactory sut = new AirGradientHandlerFactory(httpClientFactoryMock);
        Thing thingMock = Mockito.mock(Thing.class);
        Mockito.when(thingMock.getThingTypeUID()).thenReturn(AirGradientBindingConstants.THING_TYPE_LOCATION);

        assertThat(sut.createHandler(thingMock), is(notNullValue()));
    }

    @Test
    public void testCanCreateUnknownHandler() {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        AirGradientHandlerFactory sut = new AirGradientHandlerFactory(httpClientFactoryMock);
        Thing thingMock = Mockito.mock(Thing.class);
        Mockito.when(thingMock.getThingTypeUID()).thenReturn(new ThingTypeUID("unknown", "thingtype"));

        assertThat(sut.createHandler(thingMock), is(nullValue()));
    }
}
