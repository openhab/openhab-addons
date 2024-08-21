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
package org.openhab.binding.cm11a.internal.handler;

import java.io.IOException;

import org.openhab.binding.cm11a.internal.InvalidAddressException;
import org.openhab.binding.cm11a.internal.X10Interface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
