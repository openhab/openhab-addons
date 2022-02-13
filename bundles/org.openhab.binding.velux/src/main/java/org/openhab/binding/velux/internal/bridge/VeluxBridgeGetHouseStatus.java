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
import org.openhab.binding.velux.internal.bridge.common.GetHouseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeGetHouseStatus} represents a complete set of transactions
 * for receiving the current state by the <B>House Status Monitor</B> on the <B>Velux</B> bridge.
 * <P>
 * The HSM is responsible for continuous updates towards the communication initiator
 * about any changes of actuator states.
 * <P>
 * It therefore provides a method {@link VeluxBridgeGetHouseStatus#evaluateState} for check of availability of House
 * Monitoring Messages.
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeGetHouseStatus {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeGetHouseStatus.class);

    // Class access methods

    /**
     * Login into bridge, fetch the HSM state and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @return true if successful or false otherwise.
     */
    public boolean evaluateState(VeluxBridge bridge) {
        logger.trace("evaluateState() called.");

        boolean success = false;
        GetHouseStatus bcp = bridge.bridgeAPI().getHouseStatus();
        if (bcp != null) {
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
            }
        }
        logger.debug("evaluateState() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }
}
