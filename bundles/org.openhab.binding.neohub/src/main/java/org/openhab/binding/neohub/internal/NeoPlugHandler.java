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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link NeoPlugHandler} is the OpenHab Handler for NeoPlug devices 
 * Note: inherits almost all the functionality of a {@link NeoBaseHandler}
 *  
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoPlugHandler extends NeoBaseHandler {

    public NeoPlugHandler(Thing thing) {
        super(thing);
    }


    // =========== methods of NeoBaseHandler that are overridden ================
    

    /*
     * build the command string
     */
    @Override
    protected String toNeoHubBuildCommandString(String channelId, Command command) {
        String cmdValue;

        @Nullable
        String nameInHub = getThing().getProperties().get(PROPERTY_NEO_HUB_NAME);

        if (command instanceof OnOffType) {
            cmdValue = ((OnOffType) command).toString();
            switch(channelId)
            {
                case CHANNEL_MANUAL_MODE: {  
                    return String.format(CMD_CODE_MANUAL, cmdValue, nameInHub);
                }
                case CHANNEL_SWITCH_STATE: {   
                    return String.format(CMD_CODE_TIMER, cmdValue, nameInHub);
                }
            }
        }
        return "";
    }

    
    /*
     * if it is a manual command, insert a command to turn on manual mode
     * then send the original command to the hub 
     */
    @Override
    protected void toNeoHubSendCommandSet(String channel, Command command) {
        // if this is a manual command, switch to manual mode first..  
        if (channel.equals(CHANNEL_SWITCH_STATE) && command instanceof OnOffType) {
            toNeoHubSendCommand(CHANNEL_MANUAL_MODE, OnOffType.from(true));
        }
        // send the actual command to the hub
        toNeoHubSendCommand(channel, command);
    }


    /*
     * => executes a (de-bounced) update on the respective OpenHAB channels
     */
    @Override
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo
            pollResponse) {
        // set the (RO) state switch value
        toOpenHabSendValueDebounced(CHANNEL_SWITCH_STATE, 
                OnOffType.from(pollResponse.isTimerOn()));

        // the plug is in manual mode.. 
        if (pollResponse.stateManual()) {
            toOpenHabSendValueDebounced(CHANNEL_MANUAL_MODE, 
                    OnOffType.ON);
        } 
        
        // the plug is in auto Mode.. 
        if (pollResponse.stateAuto()) { 
            toOpenHabSendValueDebounced(CHANNEL_MANUAL_MODE, 
                    OnOffType.OFF);
        }
    }

}
