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
import org.eclipse.smarthome.core.library.types.PercentType;
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
 * Handler for Lamp modules. These modules support ON, OFF and brightness level states
 *
 * @author Bob Raker
 *
 */
public class Cm11aLampHandler extends Cm11aAbstractHandler {

    private Logger logger = LoggerFactory.getLogger(Cm11aBridgeHandler.class);

    protected static int DIM_LEVELS = 22;
    private State desiredState = UnDefType.UNDEF;

    /**
     * Constructor for the Thing
     *
     * @param thing
     */
    public Cm11aLampHandler(Thing thing) {
        super(thing);

        Configuration config = thing.getConfiguration();
        if (config != null) {
            houseUnitCode = (String) config.get("HouseUnitCode");
            currentState = new PercentType(0);
            logger.trace("**** Cm11aSwitchHandler houseUnitCode = " + houseUnitCode);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("**** Cm11aSwitchHandler handleCommand command = " + command.toString() + ", channelUID = "
                + channelUID.getAsString());

        x10Function = 0;
        Bridge bridge = getBridge();
        this.channelUID = channelUID;

        // Make sure the bridge handler has been initialized and is online before processing requests for any of the
        // attached devices.
        if (bridge != null) {
            Cm11aBridgeHandler cm11aHandler = (Cm11aBridgeHandler) bridge.getHandler();
            if (cm11aHandler != null && cm11aHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (OnOffType.ON.equals(command)) {
                    desiredState = OnOffType.ON; // PercentType.HUNDRED;
                } else if (OnOffType.OFF.equals(command)) {
                    desiredState = OnOffType.OFF; // PercentType.ZERO;
                } else if (command instanceof PercentType) {
                    desiredState = (PercentType) command;
                } else if (command instanceof RefreshType) {
                    logger.info("Received REFRESH command for switch " + houseUnitCode);
                } else {
                    logger.error("Ignoring unknown command received for device: " + houseUnitCode);
                }

                if (!(desiredState instanceof UnDefType)) {
                    X10Interface x10Interface = cm11aHandler.getX10Interface();
                    x10Interface.scheduleHWUpdate(this);
                }
            } else {
                logger.error("Attenpted to change switch " + houseUnitCode + " cm11a is not online");
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
                boolean x10Status = false;
                if (desiredState.equals(OnOffType.ON)) {
                    x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_ON);
                } else if (desiredState.equals(OnOffType.OFF)) {
                    x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_OFF);
                } else {
                    // desiredState must be a PercentType if we got here.
                    // Verify the type and calc how many bright increments (0 to 22) we need to send
                    if (desiredState instanceof PercentType) {
                        // Calc how many bright increments we need to send (0 to 22)
                        int desiredPercentFullBright = ((PercentType) desiredState).intValue();
                        int dims = (desiredPercentFullBright * DIM_LEVELS) / 100;
                        if (currentState.equals(OnOffType.ON)) {
                            // The current level isn't known because it would have gone to
                            // the same level as when last turned on. Need to go to full dim and then up to desired
                            // level.
                            x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_DIM, DIM_LEVELS);
                            x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT, dims);
                        } else if (currentState.equals(OnOffType.OFF)) {
                            // desiredState must be a PercentType if we got here. And, the light should be off
                            // We should just be able to send the appropriate number if dims
                            x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT, dims);
                        } else if (currentState instanceof PercentType) {
                            // This is the expected case
                            // Now currentState and desiredState are both PercentType's
                            // Need to calc how much to dim or brighten
                            int currentPercentFullBright = ((PercentType) currentState).intValue();
                            int percentToBrighten = desiredPercentFullBright - currentPercentFullBright;
                            int brightens = (percentToBrighten * 22) / 100;
                            if (brightens > 0) {
                                x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT,
                                        brightens);
                            } else if (brightens < 0) {
                                x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_DIM, -brightens);
                            } else {
                                // No change needed
                            }
                        } else {
                            // Current state is not as expected
                            logger.warn("Starting state of dimmer was not as expected: "
                                    + currentState.getClass().getName());
                        }
                    } else {
                        // we should have never gotten here. Desired state is not as expected
                        logger.warn(
                                "Starting state of dimmer was not as expected: " + currentState.getClass().getName());
                    }

                }

                // Now the hardware should have been updated. If successful update the status
                if (x10Status) {
                    // Hardware update was successful so update OpenHAB
                    updateState(channelUID, desiredState);
                    setCurrentState(desiredState);
                    currentState = desiredState;
                } else {
                    // Hardware update failed, log
                    logger.error("cm11a failed to update device: " + houseUnitCode);
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
