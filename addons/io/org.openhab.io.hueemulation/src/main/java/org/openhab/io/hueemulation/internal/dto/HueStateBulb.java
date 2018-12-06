/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Hue API state object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - "extended color light bulbs" support
 *
 */
public class HueStateBulb extends HueStatePlug {
    // https://github.com/openhab/openhab2-addons/issues/2881
    // Apparently the maximum brightness is 254
    public static int MAX_BRI = 254;
    public int bri = 0;

    /** white color temperature, 154 (cold) - 500 (warm) */
    public static int MAX_CT = 500;
    public int ct = 500;

    protected HueStateBulb() {
    }

    public HueStateBulb(boolean on) {
        super(on);
        this.bri = on ? MAX_BRI : 0;
    }

    /**
     * Create a hue state with the given brightness percentage
     *
     * @param brightness Brightness percentage
     * @param on         On value
     */
    public HueStateBulb(PercentType brightness, boolean on) {
        super(on);
        this.bri = brightness.intValue() * MAX_BRI / 100;
    }

    @Override
    public String toString() {
        return "[on: " + on + " bri: " + bri + " reachable: " + reachable;
    }
}
