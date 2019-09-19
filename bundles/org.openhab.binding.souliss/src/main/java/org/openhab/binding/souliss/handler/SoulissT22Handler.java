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
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT22Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT22Handler extends SoulissGenericTypical implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT22Handler.class);

    public SoulissT22Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    // updateState(channelUID, T2nState);
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_OpenCmd);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_CloseCmd);
                        }
                    } else if (command instanceof StopMoveType) {
                        if (command.equals(StopMoveType.STOP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_StopCmd);
                        }
                    }
                    break;
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_OpenCmd_Local);
                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_CloseCmd_Local);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void setState(PrimitiveType _state) {

        super.setLastStatusStored();
        if (_state != null) {
            if (_state instanceof UpDownType) {
                this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, (UpDownType) _state);
                // this.updateThing(this.thing);
            } else if (_state instanceof StopMoveType) {
                this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, (State) _state);
                // this.updateThing(this.thing);
            }
        }
        // this.updateThing(this.thing);
    }

    public void setState_Message(String rollershutterMessage) {
        this.updateState(SoulissBindingConstants.ROLLERSHUTTER_STATE_CHANNEL_CHANNEL,
                StringType.valueOf(rollershutterMessage));
        // this.updateThing(this.thing);

    }

}
