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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link NeoPlugHandler} is the OpenHAB Handler for NeoPlug devices Note:
 * inherits almost all the functionality of a {@link NeoBaseHandler}
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
@NonNullByDefault
public class NeoPlugHandler extends NeoBaseHandler {

    public NeoPlugHandler(Thing thing) {
        super(thing);
    }

    // =========== methods of NeoBaseHandler that are overridden ================

    @Override
    protected String toNeoHubBuildCommandString(String channelId, Command command) {
        NeoBaseConfiguration config = this.config;
        if (config != null) {
            if (command instanceof OnOffType && channelId.equals(CHAN_PLUG_OUTPUT_STATE)) {
                return String.format(CMD_CODE_TIMER, ((OnOffType) command).toString(), config.deviceNameInHub);
            }
            if (command instanceof OnOffType && channelId.equals(CHAN_PLUG_AUTO_MODE)) {
                return String.format(CMD_CODE_MANUAL, invert((OnOffType) command).toString(), config.deviceNameInHub);
            }
        }
        return "";
    }

    @Override
    protected void toNeoHubSendCommandSet(String channelId, Command command) {
        // if this is a manual command, switch to manual mode first..
        if (channelId.equals(CHAN_PLUG_OUTPUT_STATE) && command instanceof OnOffType) {
            toNeoHubSendCommand(CHAN_PLUG_AUTO_MODE, OnOffType.from(false));
        }
        // send the actual command to the hub
        toNeoHubSendCommand(channelId, command);
    }

    @Override
    protected void toOpenHabSendChannelValues(NeoHubAbstractDeviceData.AbstractRecord deviceRecord) {
        boolean offline = deviceRecord.offline();
        toOpenHabSendValueDebounced(CHAN_PLUG_AUTO_MODE, OnOffType.from(!deviceRecord.stateManual()), offline);
        toOpenHabSendValueDebounced(CHAN_PLUG_OUTPUT_STATE, OnOffType.from(deviceRecord.isTimerOn()), offline);
    }
}
