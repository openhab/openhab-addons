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

import java.util.TimeZone;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.exceptions.SolaxUpdateException;
import org.openhab.binding.solax.internal.handlers.SolaxLocalAccessChargerHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link AbstractChargerHandlerTest} is the abstract base for handler-level tests that verify the full flow from
 * raw JSON API data through parsing to channel state updates on {@link SolaxLocalAccessChargerHandler}.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public abstract class AbstractChargerHandlerTest {

    protected static final ThingUID THING_UID = new ThingUID(SolaxBindingConstants.THING_TYPE_LOCAL_CONNECT_CHARGER,
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
    protected TestableChargerHandler handler;

    /**
     * Subclass of {@link SolaxLocalAccessChargerHandler} that exposes the protected {@code updateFromData} method for
     * direct invocation in tests, bypassing the HTTP retrieval layer.
     */
    protected static class TestableChargerHandler extends SolaxLocalAccessChargerHandler {
        public TestableChargerHandler(Thing thing, TranslationProvider i18nProvider,
                TimeZoneProvider timeZoneProvider) {
            super(thing, i18nProvider, timeZoneProvider);
        }

        @Override
        public void updateFromData(String rawJsonData) throws SolaxUpdateException {
            super.updateFromData(rawJsonData);
        }
    }

    @BeforeEach
    public void setUp() {
        when(thingMock.getUID()).thenReturn(THING_UID);
        when(timeZoneProvider.getTimeZone()).thenReturn(TimeZone.getDefault().toZoneId());

        handler = new TestableChargerHandler(thingMock, i18nProvider, timeZoneProvider);
        handler.setCallback(callbackMock);
    }

    @Test
    public void testChannelUpdates() throws SolaxUpdateException {
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
