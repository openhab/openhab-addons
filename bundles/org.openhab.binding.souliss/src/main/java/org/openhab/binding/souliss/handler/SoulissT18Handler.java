/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;

/**
 * The {@link SoulissT18Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT18Handler extends SoulissGenericHandler {

    // private Logger logger = LoggerFactory.getLogger(SoulissT18Handler.class);
    byte T1nRawState;
    Configuration gwConfigurationMap;
    byte xSleepTime = 0;

    @Override
    public void initialize() {

        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thing.getConfiguration();

        if (gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL) != null) {
            xSleepTime = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).byteValue();
        }
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    public SoulissT18Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.PULSE_CHANNEL:
                    updateState(channelUID, getOHState_OnOff_FromSoulissVal(T1nRawState));
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OnCmd);
                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OffCmd);
                        }
                    }
                    break;
            }
        }
    }

    void setState(PrimitiveType _state) {
        if (_state != null) {
            updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {

        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T1nRawState != _rawState) {
            this.setState(getOHState_OnOff_FromSoulissVal(_rawState));
        }
        T1nRawState = _rawState;
    }

    @Override
    public byte getRawState() {
        return T1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissBindingProtocolConstants.Souliss_T1n_OnCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OnFeedback;
            } else if (bCmd == SoulissBindingProtocolConstants.Souliss_T1n_OffCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OffFeedback;
            } else if (bCmd >= SoulissBindingProtocolConstants.Souliss_T1n_Timed) {
                // SLEEP
                return SoulissBindingProtocolConstants.Souliss_T1n_OnFeedback;
            }
        }
        return -1;
    }
}