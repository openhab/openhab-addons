/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.dto;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LightState} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link LightMessage}.
 *
 * This should be in sync with the supported lights from
 * https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightState {
    public @Nullable String lastupdated;
    public @Nullable Boolean reachable;
    public @Nullable Boolean on;
    public @Nullable Integer bri;

    public @Nullable String alert;
    public @Nullable String colormode;
    public @Nullable String effect;

    // depending on the type of light
    public @Nullable Integer hue;
    public @Nullable Integer sat;
    public @Nullable Integer ct;
    public Double @Nullable [] xy;

    public @Nullable Integer transitiontime;

    @Override
    public String toString() {
        return "LightState{" + "lastupdated='" + lastupdated + '\'' + ", reachable=" + reachable + ", on=" + on
                + ", bri=" + bri + ", alert='" + alert + '\'' + ", colormode='" + colormode + '\'' + ", effect='"
                + effect + '\'' + ", hue=" + hue + ", sat=" + sat + ", ct=" + ct + ", xy=" + Arrays.toString(xy)
                + ", transitiontime=" + transitiontime + '}';
    }
}
