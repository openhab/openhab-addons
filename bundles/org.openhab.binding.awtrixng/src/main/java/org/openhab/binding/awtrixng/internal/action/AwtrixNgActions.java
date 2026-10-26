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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.NotificationParams;
import org.openhab.binding.awtrixng.internal.TextMarkup;
import org.openhab.binding.awtrixng.internal.dto.NotificationPayload;
import org.openhab.binding.awtrixng.internal.dto.SoundSpec;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixNgActions} are the actions of an AWTRIX NG clock that rules can call. Colors are given as
 * {@code #RRGGBB}, durations in milliseconds, as the clock has them.
 *
 * @author Thomas Lauterbach - Initial contribution (actions of the AWTRIX Light binding)
 * @author Keith T. Garner - Adapted to AWTRIX NG
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AwtrixNgActions.class)
@ThingActionsScope(name = "awtrixng")
@NonNullByDefault
public class AwtrixNgActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AwtrixNgActions.class);

    private @Nullable AwtrixNgBridgeHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AwtrixNgBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private static AwtrixNgActions of(@Nullable ThingActions actions) {
        if (actions instanceof AwtrixNgActions awtrixNgActions) {
            return awtrixNgActions;
        }
        throw new IllegalArgumentException("Instance is not an AwtrixNgActions class.");
    }

    @RuleAction(label = "Blink Indicator", description = "Blinks an indicator in a color")
    public void blinkIndicator(
            @ActionInput(name = "indicator", label = "Indicator", description = "The indicator, 1 to 3") int indicator,
            @ActionInput(name = "color", label = "Color", description = "Color as #RRGGBB, e.g. #FF0000 for red") String color,
            @ActionInput(name = "blinkMs", label = "Blink Time (ms)", description = "Time between blinks in milliseconds, 0 to 65535") int blinkMs) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setIndicator(indicator, color, blinkMs, null);
        }
    }

    public static void blinkIndicator(@Nullable ThingActions actions, int indicator, String color, int blinkMs) {
        of(actions).blinkIndicator(indicator, color, blinkMs);
    }

    @RuleAction(label = "Fade Indicator", description = "Fades an indicator in a color")
    public void fadeIndicator(
            @ActionInput(name = "indicator", label = "Indicator", description = "The indicator, 1 to 3") int indicator,
            @ActionInput(name = "color", label = "Color", description = "Color as #RRGGBB, e.g. #FF0000 for red") String color,
            @ActionInput(name = "fadeMs", label = "Fade Time (ms)", description = "Time of the fade in milliseconds, 0 to 65535") int fadeMs) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setIndicator(indicator, color, null, fadeMs);
        }
    }

    public static void fadeIndicator(@Nullable ThingActions actions, int indicator, String color, int fadeMs) {
        of(actions).fadeIndicator(indicator, color, fadeMs);
    }

    @RuleAction(label = "Activate Indicator", description = "Switches an indicator on in a color")
    public void activateIndicator(
            @ActionInput(name = "indicator", label = "Indicator", description = "The indicator, 1 to 3") int indicator,
            @ActionInput(name = "color", label = "Color", description = "Color as #RRGGBB, e.g. #FF0000 for red") String color) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setIndicator(indicator, color, null, null);
        }
    }

    public static void activateIndicator(@Nullable ThingActions actions, int indicator, String color) {
        of(actions).activateIndicator(indicator, color);
    }

    @RuleAction(label = "Deactivate Indicator", description = "Switches an indicator off")
    public void deactivateIndicator(
            @ActionInput(name = "indicator", label = "Indicator", description = "The indicator, 1 to 3") int indicator) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.resetIndicator(indicator);
        }
    }

    public static void deactivateIndicator(@Nullable ThingActions actions, int indicator) {
        of(actions).deactivateIndicator(indicator);
    }

    @RuleAction(label = "Set Mood Light", description = "Fills the display with a color")
    public void setMoodlight(
            @ActionInput(name = "color", label = "Color", description = "Color as #RRGGBB, e.g. #3366FF") String color,
            @ActionInput(name = "brightness", label = "Brightness", description = "Brightness from 0 to 255, empty for the current brightness") @Nullable Integer brightness) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setMoodlight(color, brightness);
        }
    }

    public static void setMoodlight(@Nullable ThingActions actions, String color, @Nullable Integer brightness) {
        of(actions).setMoodlight(color, brightness);
    }

    @RuleAction(label = "Clear Mood Light", description = "Switches the mood light off")
    public void clearMoodlight() {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.clearMoodlight();
        }
    }

    public static void clearMoodlight(@Nullable ThingActions actions) {
        of(actions).clearMoodlight();
    }

    @RuleAction(label = "Reboot", description = "Reboots the clock")
    public void reboot() {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.reboot();
        }
    }

    public static void reboot(@Nullable ThingActions actions) {
        of(actions).reboot();
    }

    @RuleAction(label = "Sleep", description = "Sends the clock to deep sleep")
    public void sleep(
            @ActionInput(name = "durationMs", label = "Duration (ms)", description = "How long the clock sleeps, in milliseconds") int durationMs) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.sleep(durationMs);
        }
    }

    public static void sleep(@Nullable ThingActions actions, int durationMs) {
        of(actions).sleep(durationMs);
    }

    @RuleAction(label = "Play Sound", description = "Plays a sound that is stored on the clock")
    public void playSound(
            @ActionInput(name = "name", label = "Name", description = "Name of the stored sound") String name) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            SoundSpec sound = new SoundSpec();
            sound.file = name;
            localHandler.play(sound);
        }
    }

    public static void playSound(@Nullable ThingActions actions, String name) {
        of(actions).playSound(name);
    }

    @RuleAction(label = "Play RTTTL", description = "Plays a melody in RTTTL notation")
    public void playRtttl(
            @ActionInput(name = "rtttl", label = "RTTTL", description = "The melody, e.g. two:d=4,o=5,b=200:c,e") String rtttl) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            SoundSpec sound = new SoundSpec();
            sound.rtttl = rtttl;
            localHandler.play(sound);
        }
    }

    public static void playRtttl(@Nullable ThingActions actions, String rtttl) {
        of(actions).playRtttl(rtttl);
    }

    @RuleAction(label = "Play Track", description = "Plays a track of the DFPlayer of the clock")
    public void playTrack(
            @ActionInput(name = "track", label = "Track", description = "Number of the track, 1 to 2999") int track) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            SoundSpec sound = new SoundSpec();
            sound.track = track;
            localHandler.play(sound);
        }
    }

    public static void playTrack(@Nullable ThingActions actions, int track) {
        of(actions).playTrack(track);
    }

    @RuleAction(label = "Stop Sound", description = "Stops the sounds of the clock")
    public void stopSound(
            @ActionInput(name = "group", label = "Group", description = "alert, app or radio, empty to stop all sounds") @Nullable String group) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.stopSound(group);
        }
    }

    public static void stopSound(@Nullable ThingActions actions, @Nullable String group) {
        of(actions).stopSound(group);
    }

    @RuleAction(label = "Dismiss Notification", description = "Dismisses the notification that is shown")
    public void dismissNotification() {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.dismissNotification(null);
        }
    }

    public static void dismissNotification(@Nullable ThingActions actions) {
        of(actions).dismissNotification();
    }

    @RuleAction(label = "Dismiss Named Notification", description = "Dismisses the notification that was shown with the name")
    public void dismissNamedNotification(
            @ActionInput(name = "name", label = "Name", description = "The name of the notification") String name) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.dismissNotification(name);
        }
    }

    public static void dismissNamedNotification(@Nullable ThingActions actions, String name) {
        of(actions).dismissNamedNotification(name);
    }

    @RuleAction(label = "Next App", description = "Switches to the next app")
    public void nextApp() {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.nextApp();
        }
    }

    public static void nextApp(@Nullable ThingActions actions) {
        of(actions).nextApp();
    }

    @RuleAction(label = "Previous App", description = "Switches to the previous app")
    public void previousApp() {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.previousApp();
        }
    }

    public static void previousApp(@Nullable ThingActions actions) {
        of(actions).previousApp();
    }

    @RuleAction(label = "Switch App", description = "Switches to the app with the name")
    public void switchApp(
            @ActionInput(name = "name", label = "Name", description = "The name of the app, e.g. Time") String name,
            @ActionInput(name = "fast", label = "Fast", description = "Switch at once, without the transition") boolean fast) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.switchApp(name, fast);
        }
    }

    public static void switchApp(@Nullable ThingActions actions, String name, @Nullable Boolean fast) {
        of(actions).switchApp(name, Boolean.TRUE.equals(fast));
    }

    @RuleAction(label = "Set App Order", description = "Sets the order of the apps and which ones are switched off")
    public void setAppOrder(
            @ActionInput(name = "order", label = "Order", description = "The apps in the order they rotate, empty to keep the order") @Nullable List<String> order,
            @ActionInput(name = "disabled", label = "Disabled", description = "All apps that are switched off, every other app is switched on") List<String> disabled) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setAppOrder(order, disabled);
        }
    }

    public static void setAppOrder(@Nullable ThingActions actions, @Nullable List<String> order,
            List<String> disabled) {
        of(actions).setAppOrder(order, disabled);
    }

    @RuleAction(label = "Set App Enabled", description = "Switches one app on or off in the rotation")
    public void setAppEnabled(
            @ActionInput(name = "name", label = "Name", description = "The name of the app") String name,
            @ActionInput(name = "enabled", label = "Enabled", description = "True to switch the app on") boolean enabled) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.setAppEnabled(name, enabled);
        }
    }

    public static void setAppEnabled(@Nullable ThingActions actions, String name, boolean enabled) {
        of(actions).setAppEnabled(name, enabled);
    }

    @RuleAction(label = "Show Notification", description = "Shows a notification with a text and an icon")
    public void showNotification(
            @ActionInput(name = "text", label = "Text", description = "The text to show") String text,
            @ActionInput(name = "icon", label = "Icon", description = "ID of an icon of the clock, empty for none") @Nullable String icon) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            NotificationPayload notification = new NotificationPayload();
            notification.text = TextMarkup.toJson(text, null);
            notification.icon = icon == null || icon.isBlank() ? null : icon;
            localHandler.showNotification(notification);
        }
    }

    public static void showNotification(@Nullable ThingActions actions, String text, @Nullable String icon) {
        of(actions).showNotification(text, icon);
    }

    @RuleAction(label = "Show Custom Notification", description = "Shows a notification with the keys of the AWTRIX NG payload")
    public void showCustomNotification(
            @ActionInput(name = "params", label = "Parameters", description = "The keys of the notification, such as text, textColor, durationMs, hold, stack, wakeup, sound and name. A key that NG does not know rejects the notification.") Map<String, ?> params) {
        AwtrixNgBridgeHandler localHandler = this.handler;
        if (localHandler == null) {
            return;
        }
        try {
            localHandler.showNotification(NotificationParams.toPayload(params));
        } catch (IllegalArgumentException e) {
            logger.warn("Ignoring the notification: {}", e.getMessage());
        }
    }

    public static void showCustomNotification(@Nullable ThingActions actions, Map<String, ?> params) {
        of(actions).showCustomNotification(params);
    }
}
