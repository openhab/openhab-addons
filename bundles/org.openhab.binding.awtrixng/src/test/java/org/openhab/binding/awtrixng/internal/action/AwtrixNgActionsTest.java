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
package org.openhab.binding.awtrixng.internal.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.awtrixng.internal.Helper;
import org.openhab.binding.awtrixng.internal.dto.NotificationPayload;
import org.openhab.binding.awtrixng.internal.dto.SoundSpec;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgBridgeHandler;
import org.openhab.core.thing.binding.ThingActions;

import com.google.gson.JsonParser;

/**
 * Test cases for the {@link AwtrixNgActions}.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class AwtrixNgActionsTest {

    private @Mock AwtrixNgBridgeHandler handler;
    private @Mock ThingActions otherActions;

    private AwtrixNgActions actions;

    @BeforeEach
    void setUp() {
        actions = new AwtrixNgActions();
        actions.setThingHandler(handler);
    }

    private SoundSpec playedSound() {
        ArgumentCaptor<SoundSpec> sound = ArgumentCaptor.forClass(SoundSpec.class);
        verify(handler).play(sound.capture());
        return sound.getValue();
    }

    private NotificationPayload shownNotification() {
        ArgumentCaptor<NotificationPayload> notification = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(handler).showNotification(notification.capture());
        return notification.getValue();
    }

    @Test
    void indicatorActions() {
        AwtrixNgActions.blinkIndicator(actions, 1, "#FF0000", 500);
        AwtrixNgActions.fadeIndicator(actions, 2, "#00FF00", 1000);
        AwtrixNgActions.activateIndicator(actions, 3, "#0000FF");
        AwtrixNgActions.deactivateIndicator(actions, 1);

        verify(handler).setIndicator(1, "#FF0000", 500, null);
        verify(handler).setIndicator(2, "#00FF00", null, 1000);
        verify(handler).setIndicator(3, "#0000FF", null, null);
        verify(handler).resetIndicator(1);
    }

    @Test
    void moodlightActions() {
        AwtrixNgActions.setMoodlight(actions, "#3366FF", 120);
        AwtrixNgActions.setMoodlight(actions, "#3366FF", null);
        AwtrixNgActions.clearMoodlight(actions);

        verify(handler).setMoodlight("#3366FF", 120);
        verify(handler).setMoodlight("#3366FF", null);
        verify(handler).clearMoodlight();
    }

    @Test
    void deviceActions() {
        AwtrixNgActions.reboot(actions);
        AwtrixNgActions.sleep(actions, 60000);

        verify(handler).reboot();
        verify(handler).sleep(60000);
    }

    @Test
    void soundActions() {
        AwtrixNgActions.playSound(actions, "ding");
        assertEquals("{\"file\":\"ding\"}", Helper.toJson(playedSound()));
    }

    @Test
    void rtttlAction() {
        AwtrixNgActions.playRtttl(actions, "two:d=4,o=5,b=200:c,e");
        assertEquals("{\"rtttl\":\"two:d=4,o=5,b=200:c,e\"}", Helper.toJson(playedSound()));
    }

    @Test
    void trackAction() {
        AwtrixNgActions.playTrack(actions, 42);
        assertEquals("{\"track\":42}", Helper.toJson(playedSound()));
    }

    @Test
    void stopSoundAction() {
        AwtrixNgActions.stopSound(actions, "alert");
        AwtrixNgActions.stopSound(actions, null);

        verify(handler).stopSound("alert");
        verify(handler).stopSound(null);
    }

    @Test
    void appActions() {
        AwtrixNgActions.nextApp(actions);
        AwtrixNgActions.previousApp(actions);
        AwtrixNgActions.switchApp(actions, "Time", true);
        AwtrixNgActions.switchApp(actions, "Date", null);
        AwtrixNgActions.setAppOrder(actions, List.of("Time", "weather"), List.of("Battery"));
        AwtrixNgActions.setAppEnabled(actions, "Battery", false);

        verify(handler).nextApp();
        verify(handler).previousApp();
        verify(handler).switchApp("Time", true);
        verify(handler).switchApp("Date", false);
        verify(handler).setAppOrder(List.of("Time", "weather"), List.of("Battery"));
        verify(handler).setAppEnabled("Battery", false);
    }

    @Test
    void dismissActions() {
        AwtrixNgActions.dismissNotification(actions);
        AwtrixNgActions.dismissNamedNotification(actions, "door");

        verify(handler).dismissNotification(null);
        verify(handler).dismissNotification("door");
    }

    @Test
    void simpleNotification() {
        AwtrixNgActions.showNotification(actions, "Hello", "1234");

        assertEquals("{\"text\":\"Hello\",\"icon\":\"1234\"}", Helper.toJson(shownNotification()));
    }

    @Test
    void simpleNotificationWithoutIcon() {
        AwtrixNgActions.showNotification(actions, "Hello", null);

        assertEquals("{\"text\":\"Hello\"}", Helper.toJson(shownNotification()));
    }

    @Test
    void customNotificationUsesNgKeys() {
        AwtrixNgActions.showCustomNotification(actions, Map.of("text", "Hi", "durationMs", 5000));

        assertEquals(JsonParser.parseString("{\"text\":\"Hi\",\"durationMs\":5000}"),
                JsonParser.parseString(Helper.toJson(shownNotification())));
    }

    @Test
    void customNotificationWithAnUnknownKeyIsNotSent() {
        AwtrixNgActions.showCustomNotification(actions, Map.of("text", "Hi", "duration", 5));

        verify(handler, never()).showNotification(any());
    }

    @Test
    void actionsOfAnotherBindingAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> AwtrixNgActions.reboot(otherActions));
        assertThrows(IllegalArgumentException.class, () -> AwtrixNgActions.reboot(null));
    }

    @Test
    void withoutHandlerNothingHappens() {
        AwtrixNgActions detached = new AwtrixNgActions();

        assertDoesNotThrow(() -> detached.reboot());
        assertDoesNotThrow(() -> detached.showCustomNotification(Map.of("text", "x")));
        assertNull(detached.getThingHandler());
    }
}
