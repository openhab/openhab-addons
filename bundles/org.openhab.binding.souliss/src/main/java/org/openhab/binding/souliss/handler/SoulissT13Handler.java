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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT13Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT13Handler extends SoulissGenericHandler {

    private Logger logger = LoggerFactory.getLogger(SoulissT13Handler.class);
    Configuration gwConfigurationMap;
    byte T1nRawState;

    public SoulissT13Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

    }

    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            if (_state instanceof OnOffType) {
                this.updateState(SoulissBindingConstants.STATEONOFF_CHANNEL, (OnOffType) _state);
            }

            if (_state instanceof OpenClosedType) {
                this.updateState(SoulissBindingConstants.STATEOPENCLOSE_CHANNEL, (OpenClosedType) _state);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.STATEONOFF_CHANNEL:
                    updateState(channelUID, getOHState_OnOff_FromSoulissVal(T1nRawState));
                    break;
                case SoulissBindingConstants.STATEOPENCLOSE_CHANNEL:
                    updateState(channelUID, getOHState_OpenClose_FromSoulissVal(T1nRawState));
                    break;
            }
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T1nRawState != _rawState) {
            this.setState(getOHState_OpenClose_FromSoulissVal(_rawState));
            this.setState(getOHState_OnOff_FromSoulissVal(_rawState));
        }
        T1nRawState = _rawState;
    }

    @Override
    public byte getRawState() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}
