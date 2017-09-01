/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.handler;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
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
 * @author Bob Raker
 *
 */
public class Cm11aApplianceHandler extends Cm11aAbstractHandler {

    private Logger logger = LoggerFactory.getLogger(Cm11aBridgeHandler.class);

    private State desiredState = UnDefType.UNDEF;

    public Cm11aApplianceHandler(Thing thing) {
        super(thing);

        Configuration config = thing.getConfiguration();
        if (config != null) {
            houseUnitCode = (String) config.get("HouseUnitCode");
            currentState = OnOffType.OFF;
            logger.trace("**** Cm11aSwitchHandler houseUnitCode = " + houseUnitCode);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.cm11a.handler.Cm11aAbstractHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("**** Cm11aSwitchHandler handleCommand command = " + command.toString() + ", channelUID = "
                + channelUID.getAsString());

        x10Function = 0;
        Bridge bridge = getBridge();
        this.channelUID = channelUID;

        // Make sure the bridge handler has been initialize and is online before processing requests for any of the
        // attached devices.

        if (bridge != null) {
            Cm11aBridgeHandler cm11aHandler = (Cm11aBridgeHandler) bridge.getHandler();
            if (cm11aHandler != null && cm11aHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (command.equals(OnOffType.ON)) {
                    x10Function = X10Interface.FUNC_ON;
                    desiredState = OnOffType.ON;
                } else if (command.equals(OnOffType.OFF)) {
                    x10Function = X10Interface.FUNC_OFF;
                    desiredState = OnOffType.OFF;
                }

                if (command instanceof RefreshType) {
                    logger.info("Received REFRESH command for switch " + houseUnitCode);
                } else if (x10Function > 0) {
                    X10Interface x10Interface = cm11aHandler.getX10Interface();
                    x10Interface.scheduleHWUpdate(this);
                } else {
                    logger.info(
                            "Received invalid command for switch " + houseUnitCode + " command: " + command.toString());
                }
            } else {
                logger.error("Attenpted to change switch to " + command.toString() + " for " + houseUnitCode
                        + " because the cm11a is not online");
            }
        } else {
            logger.error(
                    "Attenpted to change switch " + houseUnitCode + " but the cm11 module has not bee loaded yet.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.cm11a.handler.Cm11aAbstractHandler#updateHardware(org.openhab.binding.cm11a.internal.
     * X10Interface)
     */
    @Override
    public void updateHardware(X10Interface x10Interface) throws IOException, InvalidAddressException {

        if (!desiredState.equals(currentState)) {
            try {
                if (x10Interface.sendFunction(houseUnitCode, x10Function)) {
                    // Hardware update was successful so update OpenHAB
                    updateState(channelUID, desiredState);
                    currentState = desiredState;
                }
            } catch (InvalidAddressException e) {
                logger.error(
                        "cm11a was not able to update the cm11a because of an InvalidAddress exception. Check your hardware.",
                        e);
            } catch (IOException e) {
                logger.error("cm11a was not able to update the cm11a because of an IO exception. Check your hardware.",
                        e);
            }
        }
    }

}
