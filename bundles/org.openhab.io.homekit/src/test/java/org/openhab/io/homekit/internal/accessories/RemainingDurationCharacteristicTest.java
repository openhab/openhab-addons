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
import static org.openhab.io.homekit.internal.HomekitCharacteristicType.REMAINING_DURATION;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.valve.RemainingDurationCharacteristic;

/**
 * Tests notifications and reads for Item-backed remaining-duration characteristics.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class RemainingDurationCharacteristicTest {
    @Test
    void subscribesToUpdatesAndNotifiesForCorrectionsAndCompletion() throws Exception {
        NumberItem item = mock(NumberItem.class);
        when(item.getState()).thenReturn(DecimalType.ZERO);
        HomekitTaggedItem taggedItem = mock(HomekitTaggedItem.class);
        when(taggedItem.getItem()).thenReturn(item);
        when(taggedItem.getCharacteristicType()).thenReturn(REMAINING_DURATION);
        HomekitAccessoryUpdater updater = mock(HomekitAccessoryUpdater.class);
        HomekitCharacteristicChangeCallback callback = mock(HomekitCharacteristicChangeCallback.class);

        var characteristic = (RemainingDurationCharacteristic) HomekitCharacteristicFactory
                .createCharacteristic(taggedItem, updater);
        characteristic.subscribe(callback);

        ArgumentCaptor<Consumer<State>> updates = ArgumentCaptor.captor();
        verify(updater).subscribeToUpdates(eq(item), eq(REMAINING_DURATION.getTag()), updates.capture());
        Consumer<State> listener = updates.getValue();

        listener.accept(UnDefType.UNDEF);
        verifyNoInteractions(callback);

        when(item.getState()).thenReturn(new DecimalType(85));
        listener.accept(new DecimalType(85));
        verify(callback).changed();
        assertEquals(85, characteristic.getValue().get());

        when(item.getState()).thenReturn(DecimalType.ZERO);
        listener.accept(DecimalType.ZERO);
        verify(callback, times(2)).changed();
        assertEquals(0, characteristic.getValue().get());

        listener.accept(DecimalType.ZERO);
        verifyNoMoreInteractions(callback);

        characteristic.unsubscribe();
        verify(updater).unsubscribe(item, REMAINING_DURATION.getTag());
    }
}
