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
package org.openhab.binding.mqtt.awtrixlight.internal.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.handler.AwtrixLightBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actions for the Awtrix clock.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AwtrixActions.class)
@ThingActionsScope(name = "mqtt.awtrixlight")
@NonNullByDefault
public class AwtrixActions implements ThingActions {

    private @Nullable AwtrixLightBridgeHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AwtrixLightBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Blink Indicator", description = "Blink indicator with indicatorId")
    public void blinkIndicator(
            @ActionInput(name = "indicatorId", label = "Indicator ID", description = "The ID of the indicator (1-3)") int indicatorId,
            @ActionInput(name = "rgb", label = "Color (RGB)", description = "RGB color array, e.g. [255, 0, 0] for red") int[] rgb,
            @ActionInput(name = "blinkTimeInMs", label = "Blink Time (ms)", description = "Time in milliseconds between blinks") int blinkTimeInMs) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.blinkIndicator(indicatorId, rgb, blinkTimeInMs);
        }
    }

    public static void blinkIndicator(@Nullable ThingActions actions, int indicatorId, int[] rgb, int blinkTimeInMs) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.blinkIndicator(indicatorId, rgb, blinkTimeInMs);
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Fade Indicator", description = "Fade indicator with indicatorId")
    public void fadeIndicator(
            @ActionInput(name = "indicatorId", label = "Indicator ID", description = "The ID of the indicator (1-3)") int indicatorId,
            @ActionInput(name = "rgb", label = "Color (RGB)", description = "RGB color array, e.g. [255, 0, 0] for red") int[] rgb,
            @ActionInput(name = "fadeTimeInMs", label = "Fade Time (ms)", description = "Time in milliseconds for the fade transition") int fadeTimeInMs) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.fadeIndicator(indicatorId, rgb, fadeTimeInMs);
        }
    }

    public static void fadeIndicator(@Nullable ThingActions actions, int indicatorId, int[] rgb, int fadeTimeInMs) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.fadeIndicator(indicatorId, rgb, fadeTimeInMs);
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Activate Indicator", description = "Turn on indicator with indicatorId")
    public void activateIndicator(
            @ActionInput(name = "indicatorId", label = "Indicator ID", description = "The ID of the indicator (1-3)") int indicatorId,
            @ActionInput(name = "rgb", label = "Color (RGB)", description = "RGB color array, e.g. [255, 0, 0] for red") int[] rgb) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.activateIndicator(indicatorId, rgb);
        }
    }

    public static void activateIndicator(@Nullable ThingActions actions, int indicatorId, int[] rgb) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.activateIndicator(indicatorId, rgb);
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Deactivate Indicator", description = "Turn off indicator with indicatorId")
    public void deactivateIndicator(
            @ActionInput(name = "indicatorId", label = "Indicator ID", description = "The ID of the indicator (1-3)") int indicatorId) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.deactivateIndicator(indicatorId);
        }
    }

    public static void deactivateIndicator(@Nullable ThingActions actions, int indicatorId) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.deactivateIndicator(indicatorId);
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Reboot", description = "Reboots the device")
    public void reboot() {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.reboot();
        }
    }

    public static void reboot(@Nullable ThingActions actions) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.reboot();
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Sleep", description = "Send device to deep sleep")
    public void sleep(
            @ActionInput(name = "seconds", label = "Seconds", description = "Number of seconds the device should sleep") int seconds) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.sleep(seconds);
        }
    }

    public static void sleep(@Nullable ThingActions actions, int seconds) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.sleep(seconds);
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Upgrade", description = "Performs firmware upgrade")
    public void upgrade() {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.upgrade();
        }
    }

    public static void upgrade(@Nullable ThingActions actions) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.upgrade();
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Play Sound", description = "Plays the sound file with given name (without extension) if it exists")
    public void playSound(
            @ActionInput(name = "melody", label = "Melody", description = "The sound file name saved in the clock's MELODIES folder (without file extension)") String melody) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.playSound(melody);
        }
    }

    public static void playSound(@Nullable ThingActions actions, @Nullable String melody) {
        if (actions instanceof AwtrixActions awtrixActions) {
            if (melody != null) {
                awtrixActions.playSound(melody);
            }
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Play RTTTL", description = "Plays the melody provided in RTTTL format")
    public void playRtttl(
            @ActionInput(name = "rtttl", label = "RTTTL", description = "The RTTTL string to play") String rtttl) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.playRtttl(rtttl);
        }
    }

    public static void playRtttl(@Nullable ThingActions actions, @Nullable String rtttl) {
        if (actions instanceof AwtrixActions awtrixActions) {
            if (rtttl != null) {
                awtrixActions.playRtttl(rtttl);
            }
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Show Notification", description = "Shows a default notification with an icon")
    public void showNotification(
            @ActionInput(name = "message", label = "Message", description = "The message to show") String message,
            @ActionInput(name = "icon", label = "Icon", description = "The name of the icon saved on the device that is shown with the message") String icon) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("text", message);
            params.put("icon", icon);
            localHandler.showNotification(false, false, true, "", "", false, params);
        }
    }

    public static void showNotification(@Nullable ThingActions actions, @Nullable String message,
            @Nullable String icon) {
        if (actions instanceof AwtrixActions awtrixActions) {
            if (message != null && icon != null) {
                awtrixActions.showNotification(message, icon);
            }
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Show Custom Notification", description = "Shows a notification with specified options")
    public void showCustomNotification(
            @ActionInput(name = "appParams", label = "App Parameters", description = "Map holding any parameter available for an Awtrix App, keyed by the App Channel id") Map<String, ?> appParams,
            @ActionInput(name = "hold", label = "Hold", description = "Whether the notification should stay on the screen until the select button is pressed") boolean hold,
            @ActionInput(name = "wakeUp", label = "Wake Up", description = "Whether the notification should wake up the device if the display is currently switched off") boolean wakeUp,
            @ActionInput(name = "stack", label = "Stack", description = "Whether the notification should be stacked on top of the previous notification or replace it") boolean stack,
            @ActionInput(name = "rtttl", label = "RTTTL", description = "Play the specified RTTTL ringtone when displaying the notification. Leave empty for no sound or when sound is set") @Nullable String rtttl,
            @ActionInput(name = "sound", label = "Sound", description = "Play the specified sound file when displaying the notification. Leave empty for no sound or when rtttl is set") @Nullable String sound,
            @ActionInput(name = "loopSound", label = "Loop Sound", description = "Whether the sound should be played in a loop until the notification is dismissed") boolean loopSound) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            // appParams arrives as e.g. Map<String, Serializable> from DSL rules' newHashMap(), which
            // Java generics invariance does not accept where a Map<String, Object> is declared even
            // though every value trivially is an Object - copy through the wildcard to satisfy the
            // handler's signature without weakening it too.
            localHandler.showNotification(hold, wakeUp, stack, rtttl, sound, loopSound, new HashMap<>(appParams));
        }
    }

    public static void showCustomNotification(@Nullable ThingActions actions, @Nullable Map<String, ?> appParams,
            @Nullable Boolean hold, @Nullable Boolean wakeUp, @Nullable Boolean stack, @Nullable String rtttl,
            @Nullable String sound, @Nullable Boolean loopSound) {
        if (actions instanceof AwtrixActions awtrixActions) {
            if (appParams != null) {
                awtrixActions.showCustomNotification(appParams, Boolean.TRUE.equals(hold),
                        Boolean.TRUE.equals(wakeUp), Boolean.TRUE.equals(stack), rtttl, sound,
                        Boolean.TRUE.equals(loopSound));
            }
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }

    @RuleAction(label = "Dismiss Notification", description = "Dismisses the currently shown notification")
    public void dismissNotification() {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.dismissNotification();
        }
    }

    public static void dismissNotification(@Nullable ThingActions actions) {
        if (actions instanceof AwtrixActions awtrixActions) {
            awtrixActions.dismissNotification();
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }
}
