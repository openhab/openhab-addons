/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAction1} class represents the action Niko Home Control I communication object. It contains all fields
 * representing a Niko Home Control action and has methods to trigger the action in Niko Home Control and receive action
 * updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcAction1 extends NhcAction {

    private final Logger logger = LoggerFactory.getLogger(NhcAction1.class);

    NhcAction1(String id, String name, ActionType type, @Nullable String location) {
        super(id, name, type, location);
    }

    /**
     * Sets state of action. This is the version for Niko Home Control I.
     *
     * @param state - The allowed values depend on the action type.
     *                  switch action: 0 or 100
     *                  dimmer action: between 0 and 100
     *                  rollershutter action: between 0 and 100
     */
    @Override
    public void setState(int state) {
        this.state = state;
        updateState();
    }

    /**
     * Sends action to Niko Home Control. This version is used for Niko Home Control I.
     *
     * @param command - The allowed values depend on the action type.
     */
    @Override
    public void execute(String command) {
        logger.debug("Niko Home Control: execute action {} of type {} for {}", command, this.type, this.id);

        String value = "";
        switch (getType()) {
            case GENERIC:
            case TRIGGER:
            case RELAY:
                if (command.equals(NHCON)) {
                    value = "100";
                } else {
                    value = "0";
                }
                break;
            case DIMMER:
                if (command.equals(NHCON)) {
                    value = "254";
                } else if (command.equals(NHCOFF)) {
                    value = "255";
                } else {
                    value = command;
                }
                break;
            case ROLLERSHUTTER:
                if (command.equals(NHCDOWN)) {
                    value = "254";
                } else if (command.equals(NHCUP)) {
                    value = "255";
                } else if (command.equals(NHCSTOP)) {
                    value = "253";
                }
        }

        if (nhcComm != null) {
            nhcComm.executeAction(this.id, value);
        }
    }
}
