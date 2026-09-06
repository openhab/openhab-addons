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
package org.openhab.binding.tapocontrol.internal.devices.wifi.lightswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;

/**
 * Device data for Kasa dimmer switches.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class TapoDimmerSwitchData extends TapoLightSwitchData {
    @Expose(serialize = true, deserialize = true)
    private @Nullable Integer brightness;

    public int getBrightness() {
        Integer brightness = this.brightness;
        return brightness == null ? 0 : brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness > 0 ? brightness : null;
        switchOnOff(brightness > 0);
    }
}
