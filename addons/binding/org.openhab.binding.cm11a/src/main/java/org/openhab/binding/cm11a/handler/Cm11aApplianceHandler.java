/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.cm11a.internal.InvalidAddressException;
import org.openhab.binding.cm11a.internal.X10Interface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Appliance (also called Switch) modules. These modules only support ON and OFF states
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class Cm11aApplianceHandler extends Cm11aAbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(Cm11aApplianceHandler.class);

    private State desiredState = UnDefType.UNDEF;

    public Cm11aApplianceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("**** Cm11aApplianceHandler handleCommand command = {}, channelUID = {}", command,
                channelUID.getAsString());

        x10Function = 0;
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Unable to handle command. Bridge is null.");
            return;
        }
        this.channelUID = channelUID;

        Cm11aBridgeHandler cm11aHandler = (Cm11aBridgeHandler) bridge.getHandler();
        if (cm11aHandler != null && cm11aHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            if (command == OnOffType.ON) {
                x10Function = X10Interface.FUNC_ON;
                desiredState = OnOffType.ON;
            } else if (command == OnOffType.OFF) {
                x10Function = X10Interface.FUNC_OFF;
                desiredState = OnOffType.OFF;
            } else if (command instanceof RefreshType) {
                x10Function = X10Interface.FUNC_OFF;
                desiredState = OnOffType.OFF;
                logger.info("Received REFRESH command for switch {}", houseUnitCode);
            }

            if (x10Function > 0) {
                X10Interface x10Interface = cm11aHandler.getX10Interface();
                x10Interface.scheduleHWUpdate(this);
            } else {
                logger.debug("Received invalid command for switch {} command: {}", houseUnitCode, command);
            }
        } else {
            logger.debug("Attempted to change switch to {} for {} because the cm11a is not online", command,
                    houseUnitCode);
        }
    }

    @Override
    public void updateHardware(X10Interface x10Interface) throws IOException, InvalidAddressException {
        if (!desiredState.equals(currentState)) {
            if (x10Interface.sendFunction(houseUnitCode, x10Function)) {
                // Hardware update was successful so update openHAB
                updateState(channelUID, desiredState);
                setCurrentState(desiredState);
            }
        }
    }

}
