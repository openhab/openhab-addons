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
package org.openhab.binding.bydbatterybox.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bydbatterybox.internal.BydBatteryBoxBindingConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bydbatterybox.internal.FakeBattery;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;

/**
 * Tests {@link BydBatteryBoxHandler} against a {@link FakeBattery}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class BydBatteryBoxHandlerTest {

    private static final long FIRST_CYCLE_TIMEOUT_MILLIS = 15_000;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) TranslationProvider i18nProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;
    private @Mock @NonNullByDefault({}) Bundle bundle;

    private @Nullable BydBatteryBoxHandler handler;
    private @Nullable FakeBattery battery;

    @AfterEach
    void tearDown() throws Exception {
        BydBatteryBoxHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.dispose();
        }
        FakeBattery localBattery = battery;
        if (localBattery != null) {
            localBattery.close();
        }
    }

    private BydBatteryBoxHandler createHandler(int port) {
        // only the status cycle: cell details and event logs are covered by the protocol tests
        Configuration configuration = new Configuration(Map.of("hostname", "127.0.0.1", "port",
                BigDecimal.valueOf(port), "refreshInterval", BigDecimal.valueOf(30), "detailInterval", BigDecimal.ZERO,
                "eventLogInterval", BigDecimal.ZERO, "connectionTimeout", BigDecimal.valueOf(2)));
        Thing thing = ThingBuilder.create(THING_TYPE_BATTERY, "test").withConfiguration(configuration).build();
        BydBatteryBoxHandler localHandler = new BydBatteryBoxHandler(thing, i18nProvider, localeProvider, bundle);
        localHandler.setCallback(callback);
        handler = localHandler;
        return localHandler;
    }

    @Test
    void refreshRepublishesTheLastState() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, Fixtures.frame("identity"), Fixtures.frame("status"));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port());
        ChannelUID stateOfCharge = new ChannelUID(localHandler.getThing().getUID(), GROUP_STATUS,
                CHANNEL_STATE_OF_CHARGE);
        QuantityType<?> expected = new QuantityType<>(6, Units.PERCENT);

        localHandler.initialize();
        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        verify(callback, times(1)).stateUpdated(stateOfCharge, expected);

        localHandler.handleCommand(stateOfCharge, RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(stateOfCharge, expected);
    }

    @Test
    void refreshBeforeTheFirstReadingPublishesNothing() {
        BydBatteryBoxHandler localHandler = createHandler(1);
        ChannelUID stateOfCharge = new ChannelUID(localHandler.getThing().getUID(), GROUP_STATUS,
                CHANNEL_STATE_OF_CHARGE);

        localHandler.handleCommand(stateOfCharge, RefreshType.REFRESH);

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void busyBatteryGoesOfflineWithCommunicationError() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, FakeBattery.CLOSE);
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port());

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(), argThat(
                info -> info.getStatus() == ThingStatus.OFFLINE && "@text/offline.busy".equals(info.getDescription())));
    }
}
