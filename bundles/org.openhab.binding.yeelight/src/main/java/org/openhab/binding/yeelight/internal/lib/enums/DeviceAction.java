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
package org.openhab.binding.yeelight.internal.lib.enums;

/**
 * @author Coaster Li - Initial contribution
 * @author Joe Ho - Added duration
 */
public enum DeviceAction {
    open,
    close,
    brightness,
    color,
    colortemperature,
    increase_bright,
    decrease_bright,
    increase_ct,
    decrease_ct,
    background_color,
    background_brightness,
    background_on,
    background_off,
    nightlight_off,
    nightlight_on;

    private String mStrValue;
    private int mIntValue;
    private int mIntDuration;

    public void putValue(String value) {
        this.mStrValue = value;
    }

    public void putValue(int value) {
        this.mIntValue = value;
    }

    public void putDuration(int duration) {
        this.mIntDuration = duration;
    }

    public String strValue() {
        return mStrValue;
    }

    public int intValue() {
        return mIntValue;
    }

    public int intDuration() {
        return mIntDuration;
    }
}
