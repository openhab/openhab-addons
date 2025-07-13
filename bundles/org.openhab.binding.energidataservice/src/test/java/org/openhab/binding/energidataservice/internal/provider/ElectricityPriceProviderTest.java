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
package org.openhab.binding.energidataservice.internal.provider;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.energidataservice.internal.ApiController;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.dto.DayAheadPriceRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.provider.listener.ElectricityPriceListener;
import org.openhab.binding.energidataservice.internal.provider.subscription.SpotPriceSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.Subscription;
import org.openhab.core.i18n.TimeZoneProvider;
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
    private @NonNullByDefault({}) @Mock ApiController apiController;
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
        provider = new ElectricityPriceProvider(scheduler, apiController, timeZoneProvider);
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

    @Test
    void getPricesDownloadsPricesWhenNotCachedAndHavingNoListeners()
            throws InterruptedException, TimeoutException, ExecutionException, DataServiceException {
        when(apiController.getSpotPrices(any(String.class), any(Currency.class), any(DateQueryParameter.class),
                any(DateQueryParameter.class), ArgumentMatchers.<Map<String, String>> any()))
                .thenReturn(new ElspotpriceRecord[] {});
        when(apiController.getDayAheadPrices(any(String.class), any(Currency.class), any(DateQueryParameter.class),
                any(DateQueryParameter.class), ArgumentMatchers.<Map<String, String>> any()))
                .thenReturn(new DayAheadPriceRecord[] {});
        provider.getPrices(SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        if (Instant.now().isAfter(DAY_AHEAD_TRANSITION_DATE.atStartOfDay(NORD_POOL_TIMEZONE).toInstant())) {
            verify(apiController).getDayAheadPrices(eq("DK1"), eq(Currency.getInstance("DKK")),
                    any(DateQueryParameter.class), any(DateQueryParameter.class), anyMap());
        } else {
            verify(apiController).getSpotPrices(eq("DK1"), eq(Currency.getInstance("DKK")),
                    any(DateQueryParameter.class), any(DateQueryParameter.class), anyMap());
        }
    }

    @Test
    void getPricesDoesNotDownloadPricesWhenNotCachedAndHavingListeners()
            throws InterruptedException, TimeoutException, ExecutionException, DataServiceException {
        when(apiController.getSpotPrices(any(String.class), any(Currency.class), any(DateQueryParameter.class),
                any(DateQueryParameter.class), ArgumentMatchers.<Map<String, String>> any()))
                .thenReturn(new ElspotpriceRecord[] {});
        Subscription subscription = SpotPriceSubscription.of("DK1", Currency.getInstance("DKK"));
        provider.subscribe(listener1, subscription);
        provider.getPrices(SpotPriceSubscription.of("DK1", Currency.getInstance("DKK")));
        verify(apiController, never()).getSpotPrices(eq("DK1"), eq(Currency.getInstance("DKK")),
                any(DateQueryParameter.class), any(DateQueryParameter.class), anyMap());
        verify(apiController, never()).getDayAheadPrices(eq("DK1"), eq(Currency.getInstance("DKK")),
                any(DateQueryParameter.class), any(DateQueryParameter.class), anyMap());
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
