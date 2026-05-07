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
package org.openhab.binding.solax.internal.local.handlers;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.TimeZone;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link AbstractInverterHandlerTest} is the abstract base for handler-level tests that verify the full flow from
 * raw JSON API data through parsing to channel state updates on {@link SolaxLocalAccessInverterHandler}.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public abstract class AbstractInverterHandlerTest {

    protected static final ThingUID THING_UID = new ThingUID(SolaxBindingConstants.THING_TYPE_LOCAL_CONNECT_INVERTER,
            "test");

    @Mock
    @NonNullByDefault({})
    protected ThingHandlerCallback callbackMock;

    @Mock
    @NonNullByDefault({})
    protected Thing thingMock;

    @Mock
    @NonNullByDefault({})
    protected TranslationProvider i18nProvider;

    @Mock
    @NonNullByDefault({})
    protected TimeZoneProvider timeZoneProvider;

    @NonNullByDefault({})
    protected TestableInverterHandler handler;

    /**
     * Subclass of {@link SolaxLocalAccessInverterHandler} that exposes the protected {@code updateFromData} method for
     * direct invocation in tests, bypassing the HTTP retrieval layer.
     */
    protected static class TestableInverterHandler extends SolaxLocalAccessInverterHandler {
        public TestableInverterHandler(Thing thing, TranslationProvider i18nProvider,
                TimeZoneProvider timeZoneProvider) {
            super(thing, i18nProvider, timeZoneProvider);
        }

        @Override
        public void updateFromData(@NonNull String rawJsonData) {
            super.updateFromData(rawJsonData);
        }
    }

    @BeforeEach
    public void setUp() {
        when(thingMock.getUID()).thenReturn(THING_UID);
        when(thingMock.getChannels()).thenReturn(List.of());
        when(timeZoneProvider.getTimeZone()).thenReturn(TimeZone.getDefault().toZoneId());

        handler = new TestableInverterHandler(thingMock, i18nProvider, timeZoneProvider);
        handler.setCallback(callbackMock);
    }

    @Test
    public void testChannelUpdates() {
        handler.updateFromData(getRawData());
        assertChannels();
    }

    protected abstract String getRawData();

    protected abstract void assertChannels();

    protected <T extends Quantity<T>> void assertQuantityChannel(String channelId, double expectedValue, Unit<T> unit) {
        verify(callbackMock).stateUpdated(eq(new ChannelUID(THING_UID, channelId)),
                eq(new QuantityType<>(expectedValue, unit)));
    }

    protected void assertStringChannel(String channelId, String expectedValue) {
        verify(callbackMock).stateUpdated(eq(new ChannelUID(THING_UID, channelId)), eq(new StringType(expectedValue)));
    }
}
