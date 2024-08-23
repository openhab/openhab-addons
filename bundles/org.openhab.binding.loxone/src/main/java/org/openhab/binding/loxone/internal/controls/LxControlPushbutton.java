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
package org.openhab.binding.loxone.internal.controls;

import java.io.IOException;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * A pushbutton type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a pushbutton control covers virtual input of type pushbutton
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlPushbutton extends LxControlSwitch {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlPushbutton(uuid);
        }

        @Override
        String getType() {
            return "pushbutton";
        }
    }

    /**
     * Command string used to set control's state to ON and OFF (tap)
     */
    private static final String CMD_PULSE = "Pulse";

    LxControlPushbutton(LxUuid uuid) {
        super(uuid);
    }

    @Override
    void handleSwitchCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON) {
                sendAction(CMD_PULSE);
            } else {
                off();
            }
        }
    }
}
