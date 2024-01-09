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
package org.openhab.binding.souliss.internal.handler;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT16Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissT16Handler extends SoulissGenericHandler {
    private final Logger logger = LoggerFactory.getLogger(SoulissT16Handler.class);
    private byte t1nRawStateByte0 = 0xF;
    private byte t1nRawStateRedByte1 = 0x00;
    private byte t1nRawStateGreenByte2 = 0x00;
    private byte t1nRawStateBluByte3 = 0x00;

    private HSBType hsbState = HSBType.WHITE;

    byte xSleepTime = 0;

    public SoulissT16Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    OnOffType valOnOff = getOhStateOnOffFromSoulissVal(t1nRawStateByte0);
                    if (valOnOff != null) {
                        updateState(channelUID, valOnOff);
                    }
                    break;
                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    updateState(channelUID, gethsb(t1nRawStateRedByte1, t1nRawStateGreenByte2, t1nRawStateBluByte3));
                    break;
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(channelUID,
                            PercentType.valueOf(gethsb(t1nRawStateRedByte1, t1nRawStateGreenByte2, t1nRawStateBluByte3)
                                    .getBrightness().toString()));
                    break;
                default:
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_ON_CMD);

                    } else if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_OFF_CMD);
                    }
                    break;
                case SoulissBindingConstants.WHITE_MODE_CHANNEL:
                    if (command instanceof OnOffType) {
                        hsbState = HSBType.fromRGB(255, 255, 255);
                        commandSendRgb(SoulissProtocolConstants.SOULISS_T1N_SET, (byte) 255, (byte) 255, (byte) 255);
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, hsbState);
                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((byte) (SoulissProtocolConstants.SOULISS_T1N_TIMED + xSleepTime));
                        // set Off
                        updateState(channelUID, OnOffType.OFF);
                    }
                    break;

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL,
                                gethsb(t1nRawStateRedByte1, t1nRawStateGreenByte2, t1nRawStateBluByte3));
                        commandSendRgb(SoulissProtocolConstants.SOULISS_T1N_SET,
                                (byte) (hsbState.getRed().shortValue() * (255.00 / 100)),
                                (byte) (hsbState.getGreen().shortValue() * (255.00 / 100)),
                                (byte) (hsbState.getBlue().shortValue() * (255.00 / 100)));

                    } else if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_ON_CMD);

                    } else if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_OFF_CMD);
                    }
                    break;

                case SoulissBindingConstants.ROLLER_BRIGHTNESS_CHANNEL:
                    if (command.equals(UpDownType.UP)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_BRIGHT_UP);
                    } else if (command.equals(UpDownType.DOWN)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_BRIGHT_DOWN);
                    }
                    break;

                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    if (command instanceof HSBType localHsbState) {
                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                                PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSendRgb(SoulissProtocolConstants.SOULISS_T1N_SET,
                                (byte) (localHsbState.getRed().shortValue() * 255.00 / 100),
                                (byte) (localHsbState.getGreen().shortValue() * 255.00 / 100),
                                (byte) (localHsbState.getBlue().shortValue() * 255.00 / 100));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);

        var configurationMap = getThing().getConfiguration();

        if (configurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL) != null) {
            xSleepTime = ((BigDecimal) configurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).byteValue();
        }
        if (configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    void setState(@Nullable PrimitiveType state) {
        super.setLastStatusStored();
        updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
        if (state != null) {
            logger.debug("T16, setting state to {}", state.toFullString());
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) state);
        }
    }

    @Override
    public void setRawState(byte rawState) {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public void setRawStateCommand(byte rawStateByte0) {
        super.setLastStatusStored();
        if (rawStateByte0 != t1nRawStateByte0) {
            this.setState(getOhStateOnOffFromSoulissVal(rawStateByte0));
        }
    }

    public void setRawStateRgb(byte rawStateRedByte1, byte rawStateGreenByte2, byte rawStateBluByte3) {
        super.setLastStatusStored();

        if (rawStateRedByte1 != t1nRawStateRedByte1 || rawStateGreenByte2 != t1nRawStateGreenByte2
                || rawStateBluByte3 != t1nRawStateBluByte3) {
            HSBType localHsbState = gethsb(rawStateRedByte1, rawStateGreenByte2, rawStateBluByte3);
            logger.debug("T16, setting color to {},{},{}", rawStateRedByte1, rawStateGreenByte2, rawStateBluByte3);

            updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                    PercentType.valueOf(localHsbState.getBrightness().toString()));

            updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, localHsbState);
        }

        t1nRawStateRedByte1 = rawStateRedByte1;
        t1nRawStateGreenByte2 = rawStateGreenByte2;
        t1nRawStateBluByte3 = rawStateBluByte3;
    }

    @Override
    public byte getRawState() {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public byte getRawStateCommand() {
        return t1nRawStateByte0;
    }

    public byte[] getRawStateValues() {
        return new byte[] { t1nRawStateRedByte1, t1nRawStateGreenByte2, t1nRawStateBluByte3 };
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissProtocolConstants.SOULISS_T1N_ON_CMD) {
                return SoulissProtocolConstants.SOULISS_T1N_ON_COIL;
            } else if (bCmd == SoulissProtocolConstants.SOULISS_T1N_OFF_CMD) {
                return SoulissProtocolConstants.SOULISS_T1N_OFF_COIL;
            } else if (bCmd >= SoulissProtocolConstants.SOULISS_T1N_TIMED) {
                // SLEEP
                return SoulissProtocolConstants.SOULISS_T1N_ON_COIL;
            }
        }
        return -1;
    }

    HSBType gethsb(byte rawStateRedByte1, byte rawStateGreenByte2, byte rawStateBluByte3) {
        return HSBType.fromRGB(rawStateRedByte1, rawStateGreenByte2, rawStateBluByte3);
    }
}
