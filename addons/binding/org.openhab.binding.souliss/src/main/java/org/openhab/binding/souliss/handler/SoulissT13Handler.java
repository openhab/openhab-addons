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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT13Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT13Handler extends SoulissGenericTypical implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT13Handler.class);
    OnOffType T1n_ONOFF_State = OnOffType.OFF;
    OpenClosedType T1n_OPENCLOSE_State = OpenClosedType.OPEN;

    public SoulissT13Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            if (_state instanceof OnOffType) {
                if (((OnOffType) _state) != this.T1n_ONOFF_State) {
                    this.updateState(SoulissBindingConstants.STATEONOFF_CHANNEL, (OnOffType) _state);
                    this.T1n_ONOFF_State = (OnOffType) _state;
                }
            }

            if (_state instanceof OpenClosedType) {
                if (((OpenClosedType) _state) != this.T1n_OPENCLOSE_State) {
                    this.updateState(SoulissBindingConstants.STATEOPENCLOSE_CHANNEL, (OpenClosedType) _state);
                    this.T1n_OPENCLOSE_State = (OpenClosedType) _state;
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.STATEONOFF_CHANNEL:
                    updateState(channelUID, T1n_ONOFF_State);
                    break;
                case SoulissBindingConstants.STATEOPENCLOSE_CHANNEL:
                    updateState(channelUID, T1n_OPENCLOSE_State);
                    break;
            }
        }

    }
}
