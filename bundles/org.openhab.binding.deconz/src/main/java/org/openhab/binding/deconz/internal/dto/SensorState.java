/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link SensorState} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link SensorMessage}.
 *
 * This should be in sync with the supported sensors from
 * https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorState {
    public @Nullable String airquality;
    public @Nullable Integer airqualityppb;
    public @Nullable Boolean alarm;
    public @Nullable Integer battery;
    public @Nullable Integer buttonevent;
    public @Nullable Boolean carbonmonoxide;
    public @Nullable Double consumption;
    public @Nullable Double consumption2;
    public @Nullable Double current;
    public @Nullable Boolean dark;
    public @Nullable Boolean daylight;
    public @Nullable Boolean fire;
    public @Nullable Integer gesture;
    public @Nullable Double humidity;
    public @Nullable String lastupdated;
    public @Nullable Integer lightlevel;
    public @Nullable Boolean lowbattery;
    public @Nullable Integer lux;
    public @Nullable Integer moisture;
    public @Nullable Boolean on;
    public @Nullable Boolean open;
    public Integer @Nullable [] orientation;
    public @Nullable Double power;
    public @Nullable Boolean presence;
    public @Nullable Integer pressure;
    public @Nullable Integer status;
    public @Nullable Boolean tampered;
    public @Nullable Double temperature;
    public @Nullable Integer tiltangle;
    public @Nullable Integer valve;
    public @Nullable Boolean vibration;
    public @Nullable Integer vibrationstrength;
    public @Nullable Double voltage;
    public @Nullable Boolean water;
    public @Nullable String windowopen;
    public double @Nullable [] xy;

    @Override
    public String toString() {
        return "SensorState{" + "airquality='" + airquality + "'" + ", airqualityppb=" + airqualityppb + ", alarm="
                + alarm + ", battery=" + battery + ", buttonevent=" + buttonevent + ", carbonmonoxide=" + carbonmonoxide
                + ", consumption=" + consumption + ", consumption2=" + consumption2 + ", current=" + current + ", dark="
                + dark + ", daylight=" + daylight + ", fire=" + fire + ", gesture=" + gesture + ", humidity=" + humidity
                + ", lastupdated='" + lastupdated + "'" + ", lightlevel=" + lightlevel + ", lowbattery=" + lowbattery
                + ", lux=" + lux + ", moisture=" + moisture + ", on=" + on + ", open=" + open + ", orientation="
                + Arrays.toString(orientation) + ", power=" + power + ", presence=" + presence + ", pressure="
                + pressure + ", status=" + status + ", tampered=" + tampered + ", temperature=" + temperature
                + ", tiltangle=" + tiltangle + ", valve=" + valve + ", vibration=" + vibration + ", vibrationstrength="
                + vibrationstrength + ", voltage=" + voltage + ", water=" + water + ", windowopen='" + windowopen + "'"
                + ", xy=" + Arrays.toString(xy) + "}";
    }
}
