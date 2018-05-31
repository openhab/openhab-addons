/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.handler;

import static org.openhab.binding.smappee.SmappeeBindingConstants.PARAMETER_ACTUATOR_ID;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeActuatorHandler} is responsible for handling commands and sets the actual status for an actuator.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeActuatorHandler extends AbstractSmappeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SmappeeActuatorHandler.class);

    private String applianceId;

    public SmappeeActuatorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SmappeeService smappeeService = getSmappeeService();
        if (smappeeService == null || !smappeeService.isInitialized()) {
            return;
        }

        if (command instanceof OnOffType) {
            OnOffType commandOnOff = (OnOffType) command;

            smappeeService.putPlugOnOff(applianceId, commandOnOff == OnOffType.ON);

        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        applianceId = thing.getConfiguration().get(PARAMETER_ACTUATOR_ID).toString();
    }
}
