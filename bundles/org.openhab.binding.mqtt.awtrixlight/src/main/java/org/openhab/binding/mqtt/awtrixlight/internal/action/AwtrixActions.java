/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

    @RuleAction(label = "Fade Indicator", description = "Fade indicator with indicatorId")
    public void fadeIndicator(int indicatorId, int[] rgb, int fadeTimeInMs) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.fadeIndicator(indicatorId, rgb, fadeTimeInMs);
        }
    }

    @RuleAction(label = "Activate Indicator", description = "Turn on indicator with indicatorId")
    public void activateIndicator(int indicatorId, int[] rgb) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.activateIndicator(indicatorId, rgb);
        }
    }

    @RuleAction(label = "Deactivate Indicator", description = "Turn off indicator with indicatorId")
    public void deactivateIndicator(int indicatorId) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.deactivateIndicator(indicatorId);
        }
    }

    @RuleAction(label = "Reboot", description = "Reboots the device")
    public void reboot() {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.reboot();
        }
    }

    @RuleAction(label = "Upgrade", description = "Performs firmware upgrade")
    public void upgrade() {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.upgrade();
        }
    }

    @RuleAction(label = "Play Sound", description = "Plays the sound file with given name (without extension) if it exists")
    public void playSound(String melody) {
        AwtrixLightBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.playSound(melody);
        }
    }
}
