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
package org.openhab.binding.souliss.internal.handler;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
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
 * The {@link SoulissT19Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissT19Handler extends SoulissGenericHandler {
    private final Logger logger = LoggerFactory.getLogger(SoulissT19Handler.class);
    byte t1nRawStateByte0 = 0xF;
    byte t1nRawStateBrigthnessByte1 = 0x00;

    byte xSleepTime = 0;

    public SoulissT19Handler(Thing thing) {
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
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                            PercentType.valueOf(String.valueOf((t1nRawStateBrigthnessByte1 / 255) * 100)));
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

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL, (PercentType) command);
                        commandSEND(SoulissProtocolConstants.SOULISS_T1N_SET,
                                (byte) (((PercentType) command).shortValue() * 255.00 / 100.00));
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
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((byte) (SoulissProtocolConstants.SOULISS_T1N_TIMED + xSleepTime));
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

    public void setState(@Nullable PrimitiveType state) {
        super.setLastStatusStored();
        if (state != null) {
            updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
            logger.debug("T19, setting state to {}", state.toFullString());
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) state);
        }
    }

    public void setRawStateDimmerValue(byte dimmerValue) {
        try {
            if (dimmerValue != t1nRawStateByte0 && dimmerValue >= 0) {
                logger.debug("T19, setting dimmer to {}", dimmerValue);
                updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        PercentType.valueOf(String.valueOf(Math.round(((double) dimmerValue / 255) * 100))));
            }
        } catch (Exception ex) {
            logger.warn("UUID: {}, had an update dimmer state error:{}", this.getThing().getUID().getAsString(),
                    ex.getMessage());
        }
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (t1nRawStateByte0 != rawState) {
            this.setState(getOhStateOnOffFromSoulissVal(rawState));
        }
        t1nRawStateByte0 = rawState;
    }

    @Override
    public byte getRawState() {
        return t1nRawStateByte0;
    }

    public byte getRawStateDimmerValue() {
        return t1nRawStateBrigthnessByte1;
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
}
