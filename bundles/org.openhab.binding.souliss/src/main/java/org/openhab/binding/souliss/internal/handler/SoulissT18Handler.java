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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT18Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissT18Handler extends SoulissGenericHandler {

    byte t1nRawState = 0xF;
    byte xSleepTime = 0;

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

    public SoulissT18Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.PULSE_CHANNEL:
                    OnOffType valPulse = getOhStateOnOffFromSoulissVal(t1nRawState);
                    if (valPulse != null) {
                        updateState(channelUID, valPulse);
                    }
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
                default:
                    break;
            }
        }
    }

    void setState(@Nullable PrimitiveType state) {
        if (state != null) {
            updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) state);
        }
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (t1nRawState != rawState) {
            this.setState(getOhStateOnOffFromSoulissVal(rawState));
        }
        t1nRawState = rawState;
    }

    @Override
    public byte getRawState() {
        return t1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissProtocolConstants.SOULISS_T1N_ON_CMD) {
                return SoulissProtocolConstants.SOULISS_T1N_ON_FEEDBACK;
            } else if (bCmd == SoulissProtocolConstants.SOULISS_T1N_OFF_CMD) {
                return SoulissProtocolConstants.SOULISS_T1N_OFF_FEEDBACK;
            } else if (bCmd >= SoulissProtocolConstants.SOULISS_T1N_TIMED) {
                // SLEEP
                return SoulissProtocolConstants.SOULISS_T1N_ON_FEEDBACK;
            }
        }
        return -1;
    }
}
