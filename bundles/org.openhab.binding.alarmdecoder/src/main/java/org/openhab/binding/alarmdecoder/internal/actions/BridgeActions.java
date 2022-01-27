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
package org.openhab.binding.alarmdecoder.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.handler.ADBridgeHandler;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeActions} class defines thing actions for alarmdecoder bridges.
 *
 * @author Bob Adair - Initial contribution
 */
@ThingActionsScope(name = "alarmdecoder")
@NonNullByDefault
public class BridgeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(BridgeActions.class);

    private @Nullable ADBridgeHandler bridge;

    public BridgeActions() {
        logger.trace("Alarm Decoder bridge actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ADBridgeHandler) {
            this.bridge = (ADBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridge;
    }

    /**
     * Reboot thing action
     */
    @RuleAction(label = "reboot the device", description = "Reboot the Alarm Decoder device.")
    public void reboot() {
        ADBridgeHandler bridge = this.bridge;
        if (bridge != null) {
            bridge.sendADCommand(ADCommand.reboot());
            logger.debug("Sending reboot command.");
        } else {
            logger.debug("Request for reboot action, but bridge is undefined.");
        }
    }

    // Static method for Rules DSL backward compatibility
    public static void reboot(ThingActions actions) {
        ((BridgeActions) actions).reboot();
    }
}
