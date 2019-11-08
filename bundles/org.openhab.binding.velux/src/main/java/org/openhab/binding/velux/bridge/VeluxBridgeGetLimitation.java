/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.velux.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.bridge.common.GetProductLimitation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeGetLimitation} represents a complete set of transactions
 * for executing a scene defined on the <B>Velux</B> bridge.
 * <P>
 * It provides a method {@link VeluxBridgeGetLimitation#getLimitation} for querying a limitation of an actuators.
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeGetLimitation {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeGetLimitation.class);

    // Class access methods

    /**
     * Login into bridge, instruct the bridge to pass a command towards an actuator based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @param limitationMinimum query minimum or maximum.
     * @return true if successful, and false otherwise.
     */
    public boolean getLimitation(VeluxBridge bridge, int nodeId, boolean limitationMinimum) {
        logger.trace("getLimitation(nodeId={}) called.", nodeId);

        boolean success = false;
        GetProductLimitation bcp = bridge.bridgeAPI().getProductLimitation();
        if (bcp != null) {
            bcp.setActuatorIdAndLimitationType(nodeId, limitationMinimum);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
            }
        }
        logger.debug("getLimitation() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }

}
