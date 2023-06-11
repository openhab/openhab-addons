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
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.QueueItem;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements common functionality for Milight/Easybulb bulbs of protocol version 3.
 * Most of the implementation is found in the specific bulb classes though.
 * The class is state-less, use {@link MilightThingState} instead.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLedV3Handler extends AbstractLedHandler {
    public static final int MAX_ANIM_MODES = 10;
    protected final Logger logger = LoggerFactory.getLogger(AbstractLedV3Handler.class);

    public AbstractLedV3Handler(Thing thing, QueuedSend sendQueue, int typeOffset) {
        super(thing, sendQueue, typeOffset);
    }

    // we have to map [0,360] to [0,0xFF], where red equals hue=0 and the milight color 0xB0 (=176)
    public static byte makeColor(int hue) {
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

    @Override
    public void changeSaturation(int relativeSaturation, MilightThingState state) {
        // Not supported
    }

    protected QueueItem createRepeatable(byte[] data) {
        return QueueItem.createRepeatable(socket, delayTimeMS, repeatTimes, address, port, data);
    }

    protected QueueItem createRepeatable(int uidc, byte[] data) {
        return new QueueItem(socket, uidc, data, true, delayTimeMS, repeatTimes, address, port);
    }

    protected QueueItem createNonRepeatable(byte[] data) {
        return QueueItem.createNonRepeatable(socket, delayTimeMS, address, port, data);
    }
}
