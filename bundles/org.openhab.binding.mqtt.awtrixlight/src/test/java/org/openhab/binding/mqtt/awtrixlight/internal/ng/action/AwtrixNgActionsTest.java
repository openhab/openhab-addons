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
package org.openhab.binding.mqtt.awtrixlight.internal.ng.action;

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
import org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgHelper;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.NotificationPayload;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.SoundSpec;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.handler.AwtrixNgBridgeHandler;
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
        AwtrixNgActions.sleepMs(actions, 60000);

        verify(handler).reboot();
        verify(handler).sleep(60000);
    }

    @Test
    void sleepTakesSecondsAsOnAwtrix3() {
        AwtrixNgActions.sleep(actions, 60);

        verify(handler).sleep(60000);
    }

    @Test
    void sleepStopsAtTheLargestTimeTheClockTakes() {
        AwtrixNgActions.sleep(actions, Integer.MAX_VALUE);

        verify(handler).sleep(Integer.MAX_VALUE);
    }

    @Test
    void indicatorsTakeAnRgbArrayAsOnAwtrix3() {
        AwtrixNgActions.blinkIndicator(actions, 1, new int[] { 255, 0, 0 }, 500);
        AwtrixNgActions.fadeIndicator(actions, 2, new int[] { 0, 255, 0 }, 1000);
        AwtrixNgActions.activateIndicator(actions, 3, new int[] { 0, 0, 255 });

        verify(handler).setIndicator(1, "#FF0000", 500, null);
        verify(handler).setIndicator(2, "#00FF00", null, 1000);
        verify(handler).setIndicator(3, "#0000FF", null, null);
    }

    @Test
    void indicatorWithAnInvalidRgbArrayIsIgnored() {
        AwtrixNgActions.activateIndicator(actions, 1, new int[] { 255, 0 });
        AwtrixNgActions.activateIndicator(actions, 1, new int[] { 256, 0, 0 });

        verifyNoInteractions(handler);
    }

    @Test
    void upgradeSendsNothing() {
        AwtrixNgActions.upgrade(actions);

        verifyNoInteractions(handler);
    }

    @Test
    void customNotificationOfAwtrix3IsTranslated() {
        AwtrixNgActions.showCustomNotification(actions,
                Map.of("text", "Washing", "color", new int[] { 255, 0, 0 }, "duration", 10), true, false, false, null,
                "ding", true);

        NotificationPayload notification = shownNotification();
        assertEquals("#FF0000", notification.textColor);
        assertEquals(10000L, notification.durationMs);
        assertEquals(true, notification.hold);
        assertEquals(false, notification.wakeup);
        assertEquals(false, notification.stack);
        assertEquals("ding", notification.sound.file);
        assertEquals(true, notification.sound.loop);
    }

    @Test
    void customNotificationOfAwtrix3WithKeysWithoutEquivalentIsStillShown() {
        AwtrixNgActions.showCustomNotification(actions, Map.of("text", "Hi", "topText", true, "pos", 2), false, false,
                true, null, null, false);

        NotificationPayload notification = shownNotification();
        assertEquals(true, notification.stack);
        assertNull(notification.sound);
    }

    @Test
    void customNotificationOfAwtrix3WithAnUnknownKeyIsNotShown() {
        AwtrixNgActions.showCustomNotification(actions, Map.of("text", "Hi", "bogus", 1), false, false, true, null,
                null, false);

        verifyNoInteractions(handler);
    }

    @Test
    void soundActions() {
        AwtrixNgActions.playSound(actions, "ding");
        assertEquals("{\"file\":\"ding\"}", AwtrixNgHelper.toJson(playedSound()));
    }

    @Test
    void rtttlAction() {
        AwtrixNgActions.playRtttl(actions, "two:d=4,o=5,b=200:c,e");
        assertEquals("{\"rtttl\":\"two:d=4,o=5,b=200:c,e\"}", AwtrixNgHelper.toJson(playedSound()));
    }

    @Test
    void trackAction() {
        AwtrixNgActions.playTrack(actions, 42);
        assertEquals("{\"track\":42}", AwtrixNgHelper.toJson(playedSound()));
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
    void actionsWithoutTheNameOrTextTheyNeedPassNothingOn() {
        AwtrixNgActions.dismissNamedNotification(actions, null);
        AwtrixNgActions.showNotification(actions, null, "1234");

        // A null name would otherwise dismiss the notification that is shown
        verify(handler, never()).dismissNotification(any());
        verify(handler, never()).showNotification(any());
    }

    @Test
    void appActionsPassAMissingNameOnToBeRejected() {
        AwtrixNgActions.switchApp(actions, null, true);
        AwtrixNgActions.setAppEnabled(actions, null, true);

        verify(handler).switchApp(null, true);
        verify(handler).setAppEnabled(null, true);
    }

    @Test
    void simpleNotification() {
        AwtrixNgActions.showNotification(actions, "Hello", "1234");

        assertEquals("{\"text\":\"Hello\",\"icon\":\"1234\"}", AwtrixNgHelper.toJson(shownNotification()));
    }

    @Test
    void simpleNotificationWithoutIcon() {
        AwtrixNgActions.showNotification(actions, "Hello", null);

        assertEquals("{\"text\":\"Hello\"}", AwtrixNgHelper.toJson(shownNotification()));
    }

    @Test
    void customNotificationUsesNgKeys() {
        AwtrixNgActions.showCustomNotification(actions, Map.of("text", "Hi", "durationMs", 5000));

        assertEquals(JsonParser.parseString("{\"text\":\"Hi\",\"durationMs\":5000}"),
                JsonParser.parseString(AwtrixNgHelper.toJson(shownNotification())));
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
