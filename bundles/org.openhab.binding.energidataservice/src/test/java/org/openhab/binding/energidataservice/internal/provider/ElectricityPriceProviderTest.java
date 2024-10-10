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
package org.openhab.binding.energidataservice.internal.provider;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.provider.listener.ElectricityPriceListener;
import org.openhab.binding.energidataservice.internal.provider.subscription.SpotPriceSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.Subscription;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.scheduler.SchedulerRunnable;

/**
 * Tests for {@link ElectricityPriceProvider}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ElectricityPriceProviderTest {

    private @NonNullByDefault({}) @Mock Scheduler scheduler;
    private @NonNullByDefault({}) @Mock HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) @Mock TimeZoneProvider timeZoneProvider;
    private @NonNullByDefault({}) @Mock MockedListener listener1;
    private @NonNullByDefault({}) @Mock MockedListener listener2;
    private @NonNullByDefault({}) ElectricityPriceProvider provider;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ScheduledCompletableFuture<@Nullable Void> futureMock = (ScheduledCompletableFuture<@Nullable Void>) mock(
                ScheduledCompletableFuture.class);
        when(scheduler.at(any(SchedulerRunnable.class), any(Instant.class))).thenReturn(futureMock);
        provider = new ElectricityPriceProvider(scheduler, httpClientFactory, timeZoneProvider);
    }

    @AfterEach
    void teardown() {
        provider.unsubscribe(listener1);
        provider.unsubscribe(listener2);
    }

    @Test
    void subscribeDuplicateRegistrationThrowsIllegalArgumentException() {
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));

        assertThrows(IllegalArgumentException.class, () -> {
            provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        });
    }

    @Test
    void subscribeFirstSubscriptionSchedulesRefreshJob() {
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        verify(scheduler, times(1)).at(any(SchedulerRunnable.class), any(Instant.class));
    }

    @Test
    void subscribeSecondSubscriptionReschedulesRefreshJob() {
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("EUR")));
        verify(scheduler, times(2)).at(any(SchedulerRunnable.class), any(Instant.class));
    }

    @Test
    void subscribeSecondSubscriptionFromOtherListenerReschedulesRefreshJob() {
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        provider.subscribe(listener2, SpotPriceSubscription.of("DK1", Currency.getInstance("EUR")));
        verify(scheduler, times(2)).at(any(SchedulerRunnable.class), any(Instant.class));
    }

    @Test
    void subscribeSecondSameSubscriptionFromOtherListenerDoesNotScheduleRefreshJob() {
        provider.subscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        provider.subscribe(listener2, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        verify(scheduler, times(1)).at(any(SchedulerRunnable.class), any(Instant.class));
    }

    @Test
    void subscribeAfterUnsubscribeSchedulesRefreshJobAgain() {
        Subscription subscription = SpotPriceSubscription.of("DK1", Currency.getInstance("DKK"));
        provider.subscribe(listener1, subscription);
        provider.unsubscribe(listener1, subscription);
        provider.subscribe(listener1, subscription);
        verify(scheduler, times(2)).at(any(SchedulerRunnable.class), any(Instant.class));
    }

    @Test
    void unsubscribeUnknownSubscriptionThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            provider.unsubscribe(listener1, SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        });
    }

    private class MockedListener implements ElectricityPriceListener {
        @Override
        public void onDayAheadAvailable() {
        }

        @Override
        public void onCurrentSpotPrice(@Nullable BigDecimal price, Currency currency) {
        }

        @Override
        public void onSpotPrices(Map<Instant, BigDecimal> spotPrices, Currency currency) {
        }

        @Override
        public void onCurrentTariff(DatahubTariff datahubTariff, @Nullable BigDecimal tariff) {
        }

        @Override
        public void onTariffs(DatahubTariff datahubTariff, Map<Instant, BigDecimal> tariffs) {
        }

        @Override
        public void onPropertiesUpdated(Map<String, String> properties) {
        }

        @Override
        public void onCommunicationError(@Nullable String description) {
        }
    }
}
