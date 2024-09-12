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
import org.openhab.core.library.types.PercentType;
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
 * Handler for Lamp modules. These modules support ON, OFF and brightness level states
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class Cm11aLampHandler extends Cm11aAbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(Cm11aLampHandler.class);

    private State desiredState = UnDefType.UNDEF;

    /**
     * Constructor for the Thing
     *
     * @param thing
     */
    public Cm11aLampHandler(Thing thing) {
        super(thing);
        currentState = OnOffType.ON; // Assume it is on. During refresh it will be turned off and the currentState will
                                     // be updated appropriately
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("**** Cm11aLampHandler handleCommand command = {}, channelUID = {}", command.toString(),
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
            if (OnOffType.ON.equals(command)) {
                desiredState = OnOffType.ON;
            } else if (OnOffType.OFF.equals(command)) {
                desiredState = OnOffType.OFF;
            } else if (command instanceof PercentType percentCommand) {
                desiredState = percentCommand;
            } else if (command instanceof RefreshType) {
                // Refresh is triggered by framework during startup.
                // Force the lamp off by indicating it is currently on and we want it off
                desiredState = PercentType.ZERO; // Start with it off
                logger.info("Received REFRESH command for switch {}", houseUnitCode);
            } else {
                logger.error("Ignoring unknown command received for device: {}", houseUnitCode);
            }

            if (!(desiredState instanceof UnDefType)) {
                X10Interface x10Interface = cm11aHandler.getX10Interface();
                x10Interface.scheduleHWUpdate(this);
            }
        } else {
            logger.error("Attempted to change switch {} cm11a is not online", houseUnitCode);
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
            boolean x10Status = false;
            if (desiredState.equals(OnOffType.ON)) {
                x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_ON);
            } else if (desiredState.equals(OnOffType.OFF)) {
                x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_OFF);
            } else if (desiredState instanceof PercentType desiredStatePercent) {
                // desiredState must be a PercentType if we got here.
                // Calc how many bright increments we need to send (0 to 22)
                int desiredPercentFullBright = desiredStatePercent.intValue();
                int dims = (desiredPercentFullBright * X10_DIM_INCREMENTS) / 100;
                if (currentState.equals(OnOffType.ON)) {
                    // The current level isn't known because it would have gone to
                    // the same level as when last turned on. Need to go to full dim and then up to desired
                    // level.
                    x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_DIM, X10_DIM_INCREMENTS);
                    x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT, dims);
                } else if (currentState.equals(OnOffType.OFF)) {
                    // desiredState must be a PercentType if we got here. And, the light should be off
                    // We should just be able to send the appropriate number if dims
                    x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT, dims);
                } else if (currentState instanceof PercentType currentStatePercent) {
                    // This is the expected case
                    // Now currentState and desiredState are both PercentType's
                    // Need to calc how much to dim or brighten
                    int currentPercentFullBright = currentStatePercent.intValue();
                    int percentToBrighten = desiredPercentFullBright - currentPercentFullBright;
                    int brightens = (percentToBrighten * X10_DIM_INCREMENTS) / 100;
                    if (brightens > 0) {
                        x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_BRIGHT, brightens);
                    } else if (brightens < 0) {
                        x10Status = x10Interface.sendFunction(houseUnitCode, X10Interface.FUNC_DIM, -brightens);
                    }
                } else {
                    // Current state is not as expected
                    logger.warn("Starting state of dimmer was not as expected: {}", currentState.getClass().getName());
                }
            }

            // Now the hardware should have been updated. If successful update the status
            if (x10Status) {
                // Hardware update was successful so update OpenHAB
                updateState(channelUID, desiredState);
                setCurrentState(desiredState);
            } else {
                // Hardware update failed, log
                logger.error("cm11a failed to update device: {}", houseUnitCode);
            }
        }
    }
}
