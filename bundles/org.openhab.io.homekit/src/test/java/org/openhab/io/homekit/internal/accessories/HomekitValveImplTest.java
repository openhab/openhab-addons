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
package org.openhab.io.homekit.internal.accessories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.homekit.internal.HomekitAccessoryType.VALVE;
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.items.GroupItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitOHItemProxy;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.valve.RemainingDurationCharacteristic;
import io.github.hapjava.services.impl.ValveService;

/**
 * Tests notifications for the generated remaining-duration characteristic of a valve with a plain Group.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class HomekitValveImplTest {
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> timer = mock(ScheduledFuture.class);
    private final SwitchItem active = mock(SwitchItem.class);
    private final HomekitAccessoryUpdater updater = mock(HomekitAccessoryUpdater.class);
    private final NumberItem duration = new NumberItem("valve_duration");
    private final GroupItem group = new GroupItem("gValve");

    @BeforeEach
    void setUp() {
        when(active.getName()).thenReturn("valve_active");
        when(active.getState()).thenReturn(OnOffType.OFF);
        duration.setState(new DecimalType(60));
        doAnswer(invocation -> {
            when(timer.isDone()).thenReturn(false);
            return timer;
        }).when(scheduler).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS));
        when(timer.getDelay(TimeUnit.SECONDS)).thenReturn(60L);
        when(timer.cancel(anyBoolean())).thenAnswer(invocation -> {
            when(timer.isDone()).thenReturn(true);
            return true;
        });
    }

    @Test
    void notifiesWithRemainingDurationWhenStarted() throws Exception {
        HomekitValveImpl valve = createValve(false);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        List<Integer> notifications = subscribe(remaining);

        assertNull(group.getBaseItem());
        assertEquals(0, remaining.getValue().get());
        valve.setValveActive(ActiveEnum.ACTIVE).get();

        assertEquals(List.of(60), notifications);
        verify(active).send(OnOffType.ON);
        verifyNoInteractions(updater);
    }

    @Test
    void notifiesWithZeroWhenStoppedEarly() throws Exception {
        HomekitValveImpl valve = createValve(false);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        List<Integer> notifications = subscribe(remaining);
        valve.setValveActive(ActiveEnum.ACTIVE).get();

        valve.setValveActive(ActiveEnum.INACTIVE).get();

        assertEquals(List.of(60, 0), notifications);
        assertEquals(0, remaining.getValue().get());
        verify(timer).cancel(true);
        verify(active).send(OnOffType.OFF);
    }

    @Test
    void notifiesWithZeroWhenTimerExpiresBeforeFutureCompletes() throws Exception {
        HomekitValveImpl valve = createValve(false);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        List<Integer> notifications = subscribe(remaining);
        valve.setValveActive(ActiveEnum.ACTIVE).get();
        ArgumentCaptor<Runnable> expiration = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(expiration.capture(), eq(60L), eq(TimeUnit.SECONDS));
        when(timer.getDelay(TimeUnit.SECONDS)).thenReturn(-1L);

        expiration.getValue().run();

        assertEquals(List.of(60, 0), notifications);
        assertFalse(timer.isDone());
        verify(active).send(OnOffType.OFF);
    }

    @Test
    void countdownReadsDoNotSendNotifications() throws Exception {
        HomekitValveImpl valve = createValve(false);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        List<Integer> notifications = subscribe(remaining);
        valve.setValveActive(ActiveEnum.ACTIVE).get();

        when(timer.getDelay(TimeUnit.SECONDS)).thenReturn(59L, 58L);
        assertEquals(59, remaining.getValue().get());
        assertEquals(58, remaining.getValue().get());

        assertEquals(List.of(60), notifications);
    }

    @Test
    void unsubscribeStopsNotificationsButTimerStillClosesValve() throws Exception {
        HomekitValveImpl valve = createValve(false);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        List<Integer> notifications = subscribe(remaining);
        valve.setValveActive(ActiveEnum.ACTIVE).get();
        ArgumentCaptor<Runnable> expiration = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(expiration.capture(), eq(60L), eq(TimeUnit.SECONDS));

        remaining.unsubscribe();
        when(timer.getDelay(TimeUnit.SECONDS)).thenReturn(0L);
        expiration.getValue().run();
        when(timer.isDone()).thenReturn(true);

        assertEquals(List.of(60), notifications);
        verify(active).send(OnOffType.OFF);

        List<Integer> reconnectedNotifications = subscribe(remaining);
        when(timer.getDelay(TimeUnit.SECONDS)).thenReturn(60L);
        valve.setValveActive(ActiveEnum.ACTIVE).get();
        assertEquals(List.of(60), reconnectedNotifications);
        assertEquals(List.of(60), notifications);
    }

    @Test
    void doesNotNotifyWhenStoppingAnIdleValve() throws Exception {
        HomekitValveImpl valve = createValve(false);
        List<Integer> notifications = subscribe(getRemainingDuration(valve));

        valve.setValveActive(ActiveEnum.INACTIVE).get();
        assertTrue(notifications.isEmpty());

        valve.setValveActive(ActiveEnum.ACTIVE).get();
        valve.setValveActive(ActiveEnum.INACTIVE).get();
        valve.setValveActive(ActiveEnum.INACTIVE).get();
        assertEquals(List.of(60, 0), notifications);
    }

    @Test
    void doesNotNotifyWhenNoTimerCanBeStarted() throws Exception {
        HomekitValveImpl valve = createValve(false);
        List<Integer> notifications = subscribe(getRemainingDuration(valve));
        duration.setState(DecimalType.ZERO);

        valve.setValveActive(ActiveEnum.ACTIVE).get();

        assertTrue(notifications.isEmpty());
        verifyNoInteractions(scheduler);
    }

    @Test
    void preservesItemBackedRemainingDurationWhenConfigured() throws Exception {
        HomekitValveImpl valve = createValve(true);
        RemainingDurationCharacteristic remaining = getRemainingDuration(valve);
        HomekitCharacteristicChangeCallback callback = mock(HomekitCharacteristicChangeCallback.class);
        remaining.subscribe(callback);

        valve.setValveActive(ActiveEnum.ACTIVE).get();

        assertEquals(42, remaining.getValue().get());
        verify(updater).subscribeToUpdates(any(NumberItem.class), eq(REMAINING_DURATION.getTag()), any());
        verifyNoInteractions(callback);
    }

    private HomekitValveImpl createValve(boolean itemBackedRemainingDuration) throws Exception {
        HomekitTaggedItem root = new HomekitTaggedItem(new HomekitOHItemProxy(group), VALVE,
                Map.of("homekitTimer", true, "ValveType", "Irrigation"));
        HomekitOHItemProxy activeProxy = new HomekitOHItemProxy(active);
        List<HomekitTaggedItem> characteristics = new ArrayList<>(
                List.of(new HomekitTaggedItem(activeProxy, VALVE, ACTIVE_STATUS, null),
                        new HomekitTaggedItem(activeProxy, VALVE, INUSE_STATUS, null),
                        new HomekitTaggedItem(new HomekitOHItemProxy(duration), VALVE, DURATION, null)));
        if (itemBackedRemainingDuration) {
            NumberItem remaining = new NumberItem("valve_remaining_duration");
            remaining.setState(new DecimalType(42));
            characteristics
                    .add(new HomekitTaggedItem(new HomekitOHItemProxy(remaining), VALVE, REMAINING_DURATION, null));
        }
        HomekitValveImpl valve = new HomekitValveImpl(root, characteristics, List.of(), updater, new HomekitSettings(),
                scheduler);
        valve.init();
        return valve;
    }

    private RemainingDurationCharacteristic getRemainingDuration(HomekitValveImpl valve) {
        return valve.getServices().stream().filter(ValveService.class::isInstance)
                .flatMap(service -> service.getCharacteristics().stream())
                .filter(RemainingDurationCharacteristic.class::isInstance)
                .map(RemainingDurationCharacteristic.class::cast).findFirst().orElseThrow();
    }

    private List<Integer> subscribe(RemainingDurationCharacteristic remaining) {
        List<Integer> notifications = new ArrayList<>();
        remaining.subscribe(() -> notifications.add(remaining.getValue().join()));
        return notifications;
    }
}
