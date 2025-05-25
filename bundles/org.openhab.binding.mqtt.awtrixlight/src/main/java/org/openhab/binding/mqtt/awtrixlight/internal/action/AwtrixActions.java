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

package org.openhab.binding.mqtt.awtrixlight.internal.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.handler.AwtrixLightBridgeHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Actions for the Awtrix clock.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
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
    public void blinkIndicator(int indicatorId, int[] rgb, int blinkTimeInMs) {
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
    public void fadeIndicator(int indicatorId, int[] rgb, int fadeTimeInMs) {
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
    public void activateIndicator(int indicatorId, int[] rgb) {
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
    public void deactivateIndicator(int indicatorId) {
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
    public void sleep(int seconds) {
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
    public void playSound(String melody) {
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
    public void playRtttl(String rtttl) {
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
    public void showNotification(String message, String icon) {
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
    public void showCustomNotification(Map<String, Object> appParams, boolean hold, boolean wakeUp, boolean stack,
            @Nullable String rtttl, @Nullable String sound, boolean loopSound) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.showNotification(hold, wakeUp, stack, rtttl, sound, loopSound, appParams);
        }
    }

    public static void showCustomNotification(@Nullable ThingActions actions, @Nullable Map<String, Object> appParams,
            boolean hold, boolean wakeUp, boolean stack, @Nullable String rtttl, @Nullable String sound,
            boolean loopSound) {
        if (actions instanceof AwtrixActions awtrixActions) {
            if (appParams != null) {
                awtrixActions.showCustomNotification(appParams, hold, wakeUp, stack, rtttl, sound, loopSound);
            }
        } else {
            throw new IllegalArgumentException("Instance is not an AwtrixActions class.");
        }
    }
}
