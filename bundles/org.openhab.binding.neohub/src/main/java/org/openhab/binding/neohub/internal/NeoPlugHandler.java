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
        @Nullable
        String nameInHub = getThing().getProperties().get(PROPERTY_NEOHUB_NAME);

        if (command instanceof OnOffType && channelId.equals(CHAN_OFF_ON)) {
            return String.format(CMD_CODE_TIMER, 
                ((OnOffType) command).toString(), nameInHub);
        } else

        if (command instanceof OnOffType && channelId.equals(CHAN_MAN_AUTO)) {
            return String.format(CMD_CODE_MANUAL,
                invert((OnOffType) command).toString(), nameInHub);
        }
        return "";
    }

    
    /*
     * if it is a manual command, insert a command to turn on manual mode
     * then send the original command to the hub 
     */
    @Override
    protected void toNeoHubSendCommandSet(String channelId, Command command) {
        // if this is a manual command, switch to manual mode first..  
        if (channelId.equals(CHAN_OFF_ON) && command instanceof OnOffType) {
            toNeoHubSendCommand(CHAN_MAN_AUTO, OnOffType.from(false));
        }
        // send the actual command to the hub
        toNeoHubSendCommand(channelId, command);
    }


    /*
     * => executes a (de-bounced) update on the respective OpenHAB channels
     */
    @Override
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo device) {
        toOpenHabSendValueDebounced(CHAN_MAN_AUTO,  
            OnOffType.from(!device.stateManual())); 
                
        toOpenHabSendValueDebounced(CHAN_OFF_ON, 
            OnOffType.from(device.isTimerOn())); 
    }
}
