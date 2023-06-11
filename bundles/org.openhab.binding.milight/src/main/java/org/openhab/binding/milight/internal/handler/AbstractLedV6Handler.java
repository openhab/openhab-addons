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
import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueueItem;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements basic V6 bulb functionally. But commands are different for different v6 bulbs, so almost all the work is
 * done in subclasses.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLedV6Handler extends AbstractLedHandler {
    protected final Logger logger = LoggerFactory.getLogger(AbstractLedV6Handler.class);

    protected static final int MAX_BR = 100; // Maximum brightness (0x64)
    protected static final int MAX_SAT = 100; // Maximum saturation (0x64)
    protected static final int MAX_TEMP = 100; // Maximum colour temperature (0x64)

    protected @NonNullByDefault({}) MilightV6SessionManager session;

    public AbstractLedV6Handler(Thing thing, QueuedSend sendQueue, int typeOffset) {
        super(thing, sendQueue, typeOffset);
    }

    @Override
    protected void start(AbstractBridgeHandler handler) {
        BridgeV6Handler h = (BridgeV6Handler) handler;
        session = h.getSessionManager();
    }

    protected abstract byte getAddr();

    protected abstract byte getBrCmd();

    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        if (hue > 360 || hue < 0) {
            logger.error("Hue argument out of range");
            return;
        }

        // 0xFF = Red, D9 = Lavender, BA = Blue, 85 = Aqua, 7A = Green, 54 = Lime, 3B = Yellow, 1E = Orange
        // we have to map [0,360] to [0,0xFF], where red equals hue=0 and the milight color 0xFF (=255)
        // Integer milightColorNo = (256 + 0xFF - (int) (hue / 360.0 * 255.0)) % 256;

        // Compute destination hue and current hue value, each mapped to 256 values.
        // int cHue = state.hue360 * 255 / 360; // map to 256 values
        int dHue = hue * 255 / 360; // map to 256 values
        sendRepeatableCat(ProtocolConstants.CAT_COLOR_SET, 1, dHue, dHue, dHue, dHue);

        state.hue360 = hue;

        if (brightness != -1) {
            setBrightness(brightness, state);
        }

        if (saturation != -1) {
            setSaturation(saturation, state);
        }
    }

    @Override
    public void setBrightness(int newvalue, MilightThingState state) {
        int value = Math.min(Math.max(newvalue, 0), 100);

        if (value == 0) {
            setPower(false, state);
        } else if (state.brightness == 0) {
            // If if was dimmed to minimum (off), turn it on again
            setPower(true, state);
        }

        int br = (value * MAX_BR) / 100;
        br = Math.min(br, MAX_BR);
        br = Math.max(br, 0);
        sendRepeatableCat(ProtocolConstants.CAT_BRIGHTNESS_SET, getBrCmd(), br);

        state.brightness = value;
    }

    @Override
    public void changeColorTemperature(int colorTempRelative, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (colorTempRelative == 0) {
            return;
        }

        int ct = (state.colorTemperature * MAX_TEMP) / 100 + colorTempRelative;
        ct = Math.min(ct, MAX_TEMP);
        ct = Math.max(ct, 0);
        setColorTemperature(ct * 100 / MAX_TEMP, state);
    }

    @Override
    public void changeBrightness(int relativeBrightness, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (relativeBrightness == 0) {
            return;
        }

        int br = (state.brightness * MAX_BR) / 100 + relativeBrightness;
        br = Math.min(br, MAX_BR);
        br = Math.max(br, 0);

        setBrightness(br * 100 / MAX_BR, state);
    }

    @Override
    public void changeSaturation(int relativeSaturation, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (relativeSaturation == 0) {
            return;
        }

        int br = (state.brightness * MAX_BR) / 100 + relativeSaturation;
        br = Math.min(br, MAX_BR);
        br = Math.max(br, 0);

        setSaturation(br * 100 / MAX_BR, state);
    }

    @Override
    public void previousAnimationMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int mode = state.animationMode - 1;
        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);

        setLedMode(mode, state);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int mode = state.animationMode + 1;
        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);

        setLedMode(mode, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
            return;
        }

        switch (channelUID.getId()) {
            case MilightBindingConstants.CHANNEL_LINKLED: {
                sendQueue.queue(new QueueItem(socket, uidc(ProtocolConstants.CAT_LINK),
                        session.makeLink(getAddr(), config.zone, true), true, delayTimeMS, repeatTimes, address, port));
                break;
            }
            case MilightBindingConstants.CHANNEL_UNLINKLED: {
                sendQueue.queue(new QueueItem(socket, uidc(ProtocolConstants.CAT_LINK),
                        session.makeLink(getAddr(), config.zone, false), true, delayTimeMS, repeatTimes, address,
                        port));
                break;
            }
            default:
                super.handleCommand(channelUID, command);
        }
    }

    protected void sendNonRepeatable(int... data) {
        sendQueue.queue(QueueItem.createNonRepeatable(socket, delayTimeMS, address, port,
                session.makeCommand(getAddr(), config.zone, data)));
    }

    protected void sendRepeatableCat(int cat, int... data) {
        sendQueue.queue(new QueueItem(socket, uidc(cat), session.makeCommand(getAddr(), config.zone, data), true,
                delayTimeMS, repeatTimes, address, port));
    }

    protected void sendRepeatable(int... data) {
        sendQueue.queue(QueueItem.createRepeatable(socket, delayTimeMS, repeatTimes, address, port,
                session.makeCommand(getAddr(), config.zone, data)));
    }
}
