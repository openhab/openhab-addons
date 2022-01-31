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
package org.openhab.binding.velux.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeRunProductCommand} represents a complete set of transactions
 * for executing a scene defined on the <B>Velux</B> bridge.
 * <P>
 * It provides a method {@link VeluxBridgeRunProductCommand#sendCommand} for sending a parameter change command.
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeRunProductCommand {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeRunProductCommand.class);

    // Class access methods

    /**
     * Login into bridge, instruct the bridge to pass a command towards an actuator based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @param value Target value for Actuator main parameter.
     * @return true if successful, and false otherwise.
     */
    public boolean sendCommand(VeluxBridge bridge, int nodeId, VeluxProductPosition value) {
        logger.trace("sendCommand(nodeId={},value={}) called.", nodeId, value);

        boolean success = false;
        RunProductCommand bcp = bridge.bridgeAPI().runProductCommand();
        if (bcp != null) {
            int veluxValue = value.getPositionAsVeluxType();

            bcp.setNodeAndMainParameter(nodeId, veluxValue);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
            }
        }
        logger.debug("sendCommand() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }
}
