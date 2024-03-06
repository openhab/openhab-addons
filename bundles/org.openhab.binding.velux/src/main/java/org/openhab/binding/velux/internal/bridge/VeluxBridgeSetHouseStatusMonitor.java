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
import org.openhab.binding.velux.internal.bridge.common.SetHouseStatusMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeSetHouseStatusMonitor} represents a complete set of transactions
 * for modifying the service state of the <B>House Status Monitor</B> on the <B>Velux</B> bridge.
 * <P>
 * The HSM is responsible for continuous updates towards the communication initiator
 * about any changes of actuator states.
 * <P>
 * It therefore provides a method {@link VeluxBridgeSetHouseStatusMonitor#modifyHSM} for modifying the HSM settings.
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeSetHouseStatusMonitor {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeSetHouseStatusMonitor.class);

    // Class access methods

    /**
     * Login into bridge, modify HSM and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param enableService Flag whether the HSM should be activated.
     * @return true if successful or false otherwise.
     */
    public boolean modifyHSM(VeluxBridge bridge, boolean enableService) {
        logger.trace("modifyHSM({}) called.", enableService);

        boolean success = false;
        SetHouseStatusMonitor bcp = bridge.bridgeAPI().setHouseStatusMonitor();
        if (bcp != null) {
            bcp.serviceActivation(enableService);
            if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                success = true;
            }
        }
        logger.debug("modifyHSM() finished {}.", (success ? "successfully" : "with failure"));
        return success;
    }
}
