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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.bridge.slip.SCrunProductCommand;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxProduct;
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

    private final VeluxBridge bridge;
    private final @Nullable SCrunProductCommand bcp;

    private int nodeID;
    private int mainPosition;
    private @Nullable FunctionalParameters functionalParameters;

    /**
     * Create and initialise the bcp.
     *
     * @param bridge the bridge to send the command to.
     */
    public VeluxBridgeRunProductCommand(VeluxBridge bridge) {
        this.bridge = bridge;
        RunProductCommand bcp = bridge.bridgeAPI().runProductCommand();
        if (bcp instanceof SCrunProductCommand) {
            this.bcp = (SCrunProductCommand) bcp;
        } else {
            this.bcp = null;
        }
    }

    // Class access methods

    /**
     * Set the command parameters.
     *
     * @param nodeID the actuator node to be commanded.
     * @param mainPosition the new main position.
     * @param functionalParameters the new functional parameters.
     */
    public void setParameters(int nodeID, VeluxProductPosition mainPosition,
            @Nullable FunctionalParameters functionalParameters) {
        this.nodeID = nodeID;
        this.mainPosition = mainPosition.getPositionAsVeluxType();
        this.functionalParameters = functionalParameters;
    }

    /**
     * Send the command and wait for the result.
     *
     * @return true if the command was sent.
     */
    public boolean sendCommand() {
        logger.debug("sendCommand() called, nodeID:{}, mainPosition:{}, functionalParameters:{}", nodeID, mainPosition,
                functionalParameters);
        boolean success = false;
        SCrunProductCommand bcp = this.bcp;
        if (bcp != null) {
            bcp.setNodeIdAndParameters(nodeID, mainPosition, functionalParameters);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
            }
        }
        logger.debug("sendCommand() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }

    public Command getRequestingCommand() {
        SCrunProductCommand bcp = this.bcp;
        return bcp != null ? bcp.getRequestingCommand() : Command.UNDEFTYPE;
    }

    public VeluxProduct getProduct() {
        SCrunProductCommand bcp = this.bcp;
        return bcp != null ? bcp.getProduct() : VeluxProduct.UNKNOWN;
    }
}
