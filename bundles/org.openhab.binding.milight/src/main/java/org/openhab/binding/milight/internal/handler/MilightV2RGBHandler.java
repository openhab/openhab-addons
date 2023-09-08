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
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueueItem;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements functionality for the very first RGB bulbs for protocol version 2.
 * A lot of stuff is not supported by this type of bulbs and they are not auto discovered
 * and can only be add manually in the things file.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV2RGBHandler extends AbstractLedHandler {
    protected final Logger logger = LoggerFactory.getLogger(MilightV2RGBHandler.class);
    protected static final int BR_LEVELS = 9;

    public MilightV2RGBHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 5);
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

    // We have no real saturation control for RGB bulbs. If the saturation is under a 50% threshold
    // we just change to white mode instead.
    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        if (saturation < 50) {
            whiteMode(state);
        } else {
            setPower(true, state);
            state.hue360 = hue;
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_COLOR_SET),
                    new byte[] { 0x20, AbstractLedV3Handler.makeColor(hue), 0x55 }));

            if (brightness != -1) {
                setBrightness(brightness, state);
            }
        }
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        if (on) {
            logger.debug("milight: sendOn");
            byte[] messageBytes = null;
            // message rgb bulbs ON
            messageBytes = new byte[] { 0x22, 0x00, 0x55 };
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE), messageBytes));
        } else {
            logger.debug("milight: sendOff");
            byte[] messageBytes = null;

            // message rgb bulbs OFF
            messageBytes = new byte[] { 0x21, 0x00, 0x55 };

            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE), messageBytes));
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
    }

    @Override
    public void nightMode(MilightThingState state) {
    }

    @Override
    public void changeColorTemperature(int colorTempRelative, MilightThingState state) {
    }

    @Override
    public void setLedMode(int mode, MilightThingState state) {
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
    }

    @Override
    public void changeSaturation(int relativeSaturation, MilightThingState state) {
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
    }

    @Override
    public void setBrightness(int value, MilightThingState state) {
        if (value <= 0) {
            setPower(false, state);
            return;
        }

        if (value > state.brightness) {
            int repeatCount = (value - state.brightness) / 10;
            for (int i = 0; i < repeatCount; i++) {

                changeBrightness(+1, state);

            }
        } else if (value < state.brightness) {
            int repeatCount = (state.brightness - value) / 10;
            for (int i = 0; i < repeatCount; i++) {

                changeBrightness(-1, state);
            }
        }

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relativeBrightness, MilightThingState state) {
        int newPercent = state.brightness + relativeBrightness;
        if (newPercent < 0) {
            newPercent = 0;
        }
        if (newPercent > 100) {
            newPercent = 100;
        }
        if (state.brightness != -1 && newPercent == 0) {
            setPower(false, state);
        } else {
            setPower(true, state);
            int steps = (int) Math.abs(Math.floor(relativeBrightness * BR_LEVELS / 100.0));
            for (int s = 0; s < steps; ++s) {
                byte[] t = { (byte) (relativeBrightness < 0 ? 0x24 : 0x23), 0x00, 0x55 };
                sendQueue.queue(createNonRepeatable(t));
            }
        }
        state.brightness = newPercent;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
    }

    @Override
    public void previousAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue(createNonRepeatable(new byte[] { 0x28, 0x00, 0x55 }));
        state.animationMode = Math.min(state.animationMode - 1, 0);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue(createNonRepeatable(new byte[] { 0x27, 0x00, 0x55 }));
        state.animationMode = Math.max(state.animationMode + 1, 100);
    }
}
