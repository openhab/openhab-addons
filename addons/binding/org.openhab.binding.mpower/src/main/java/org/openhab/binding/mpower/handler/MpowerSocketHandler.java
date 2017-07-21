/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mpower.MpowerBindingConstants;
import org.openhab.binding.mpower.internal.MpowerSocketState;

/**
 * Handler for socket things. Forwards commands to bridge handler.
 *
 * @author Marko Donke - Initial contribution
 *
 */
public class MpowerSocketHandler extends BaseThingHandler {

    private MpowerSocketState currentState;

    public MpowerSocketHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            Bridge mPower = this.getBridge();
            if (mPower != null && mPower.getHandler() instanceof MpowerHandler) {
                MpowerHandler handler = (MpowerHandler) mPower.getHandler();
                int sockNumber = Integer.parseInt(
                        thing.getConfiguration().get(MpowerBindingConstants.SOCKET_NUMBER_PROP_NAME).toString());

                OnOffType type = (OnOffType) command;
                handler.sendSwitchCommandToMPower(sockNumber, type);
            }
        }
    }

    public MpowerSocketState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(MpowerSocketState currentState) {
        this.currentState = currentState;
    }
}