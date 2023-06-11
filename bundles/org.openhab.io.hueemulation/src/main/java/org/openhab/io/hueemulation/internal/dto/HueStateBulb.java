/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

import org.openhab.core.library.types.PercentType;

/**
 * Hue API state object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - "extended color light bulbs" support
 *
 */
public class HueStateBulb extends HueStatePlug {
    // https://github.com/openhab/openhab-addons/issues/2881
    // Apparently the maximum brightness is 254
    public static final int MAX_BRI = 254;
    public int bri = 0;

    /** white color temperature, 154 (cold) - 500 (warm) */
    public static final int MAX_CT = 500;
    public int ct = 500;

    protected HueStateBulb() {
    }

    public HueStateBulb(boolean on) {
        super(on);
        this.bri = on ? MAX_BRI : 1;
    }

    /**
     * Create a hue state with the given brightness percentage
     *
     * @param brightness Brightness percentage
     * @param on On value
     */
    public HueStateBulb(PercentType brightness, boolean on) {
        super(on);
        this.bri = Math.max(1, (int) (brightness.intValue() * MAX_BRI / 100.0 + 0.5));
    }

    public PercentType toBrightnessType() {
        int bri = this.bri * 100 / MAX_BRI;

        if (!this.on) {
            bri = 0;
        }
        return new PercentType(bri);
    }

    @Override
    public String toString() {
        return "on: " + on + ", brightness: " + bri + ", reachable: " + reachable;
    }
}
