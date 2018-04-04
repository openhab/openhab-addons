/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import org.openhab.binding.milight.internal.MilightThingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements common functionality for Milight/Easybulb bulbs of protocol version 3.
 * Most of the implementation is found in the specific bulb classes though.
 * The class is state-less, use {@link MilightThingState} instead.
 *
 * @author David Graeff
 * @since 2.0
 */
public abstract class MilightV3 extends AbstractBulbInterface {
    public static final int MAX_ANIM_MODES = 10;
    protected final Logger logger = LoggerFactory.getLogger(MilightV3.class);

    public MilightV3(int type_offset, QueuedSend sendQueue, int zone) {
        super(type_offset, sendQueue, zone);
    }

    // we have to map [0,360] to [0,0xFF], where red equals hue=0 and the milight color 0xB0 (=176)
    public static byte make_color(int hue) {
        int mHue = (360 + 248 - hue) % 360; // invert and shift
        return (byte) (mHue * 255 / 360); // map to 256 values
    }

    @Override
    public void setLedMode(int mode, MilightThingState state) {
        // Not supported
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
        // Not supported
    }
}
