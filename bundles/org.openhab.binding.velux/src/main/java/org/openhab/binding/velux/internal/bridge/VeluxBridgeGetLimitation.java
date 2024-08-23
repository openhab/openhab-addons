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
package org.openhab.binding.velux.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.GetProductLimitation;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeGetLimitation} represents a complete set of transactions
 * for retrieval of the limitation of an actuator defined on the <B>Velux</B> bridge.
 * <P>
 * It therefore provides the methods
 * <UL>
 * <LI>{@link VeluxBridgeGetLimitation#getMinimumLimitation} for querying the lower limitation of an actuator,</LI>
 * <LI>{@link VeluxBridgeGetLimitation#getMaximumLimitation} for querying the high limitation of an actuator.</LI>
 * </UL>
 *
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeGetLimitation {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeGetLimitation.class);

    // Private Objects

    private VeluxProductPosition limitationResult = VeluxProductPosition.UNKNOWN;

    // Class access methods

    /**
     * Login into bridge, instruct the bridge to pass a command towards an actuator based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @return true if successful, and false otherwise.
     */
    public boolean getMinimumLimitation(VeluxBridge bridge, int nodeId) {
        logger.trace("getMinimumLimitation(nodeId={}) called.", nodeId);

        boolean success = false;
        GetProductLimitation bcp = bridge.bridgeAPI().getProductLimitation();
        if (bcp != null) {
            bcp.setActuatorIdAndLimitationType(nodeId, true);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
                limitationResult = new VeluxProductPosition(bcp.getLimitation());
            }
        }
        logger.debug("getMinimumLimitation() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }

    /**
     * Login into bridge, instruct the bridge to pass a command towards an actuator based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @return true if successful, and false otherwise.
     */
    public boolean getMaximumLimitation(VeluxBridge bridge, int nodeId) {
        logger.trace("getMaximumLimitation(nodeId={}) called.", nodeId);

        boolean success = false;
        GetProductLimitation bcp = bridge.bridgeAPI().getProductLimitation();
        if (bcp != null) {
            bcp.setActuatorIdAndLimitationType(nodeId, false);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
                limitationResult = new VeluxProductPosition(bcp.getLimitation());
            }
        }
        logger.debug("getMaximumLimitation() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }

    /**
     * Return the limitation value.
     *
     * @return limitationResult of type VeluxProductPosition.
     */
    public VeluxProductPosition getLimitation() {
        return limitationResult;
    }
}
