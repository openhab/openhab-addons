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
package org.openhab.binding.pilight.internal.handler;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.CHANNEL_STATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Code;
import org.openhab.binding.pilight.internal.dto.Device;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightSwitchHandler extends PilightBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(PilightSwitchHandler.class);

    public PilightSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateFromStatus(Status status) {
        String state = status.getValues().get("state");
        if (state != null) {
            updateState(CHANNEL_STATE, OnOffType.valueOf(state.toUpperCase()));
        }
    }

    @Override
    void updateFromConfigDevice(Device device) {
    }

    @Override
    protected @Nullable Action createUpdateCommand(ChannelUID unused, Command command) {
        if (command instanceof OnOffType) {
            Code code = new Code();
            code.setDevice(getName());
            code.setState(command.equals(OnOffType.ON) ? Code.STATE_ON : Code.STATE_OFF);

            Action action = new Action(Action.ACTION_CONTROL);
            action.setCode(code);
            return action;
        }

        logger.warn("A pilight switch only accepts OnOffType commands.");
        return null;
    }
}
