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
import org.openhab.binding.velux.internal.bridge.common.SetProductLimitation;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeSetLimitation} represents a complete set of transactions
 * for modifying the limitation of an actuator defined on the <B>Velux</B> bridge.
 * <P>
 * It therefore provides the methods
 * <UL>
 * <LI>{@link VeluxBridgeSetLimitation#setMinimumLimitation} for modifying the lower limitation of an actuator,</LI>
 * <LI>{@link VeluxBridgeSetLimitation#setMaximumLimitation} for modifying the high limitation of an actuator.</LI>
 * </UL>
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeSetLimitation {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeSetLimitation.class);

    // Class access methods

    /**
     * Login into bridge, modify the scene parameters and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @param limitationMinimum new value for minimum limit.
     * @return true if successful, and false otherwise.
     */
    public boolean setMinimumLimitation(VeluxBridge bridge, int nodeId, VeluxProductPosition limitationMinimum) {
        logger.trace("setMinimumLimitation(nodeId={}, limitation={}) called.", nodeId, limitationMinimum);

        SetProductLimitation bcp = bridge.bridgeAPI().setProductLimitation();
        if (bcp == null) {
            logger.info("setMinimumLimitation(): aborting processing as there is handler available.");
            return false;
        }
        bcp.setActuatorIdAndMinimumLimitation(nodeId, limitationMinimum.getPositionAsVeluxType());
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.trace("setMinimumLimitation() finished successfully.");
            return true;
        }
        logger.trace("setMinimumLimitation() finished with failure.");
        return false;
    }

    /**
     * Login into bridge, modify the scene parameters and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param nodeId Number of Actuator to be modified.
     * @param limitationMaximum new value for maximum limit.
     * @return true if successful, and false otherwise.
     */
    public boolean setMaximumLimitation(VeluxBridge bridge, int nodeId, VeluxProductPosition limitationMaximum) {
        logger.trace("setMaximumLimitation(nodeId={}, limitation={}) called.", nodeId, limitationMaximum);

        SetProductLimitation bcp = bridge.bridgeAPI().setProductLimitation();
        if (bcp == null) {
            logger.info("setMaximumLimitation(): aborting processing as there is handler available.");
            return false;
        }
        bcp.setActuatorIdAndMaximumLimitation(nodeId, limitationMaximum.getPositionAsVeluxType());
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.trace("setMaximumLimitation() finished successfully.");
            return true;
        }
        logger.trace("setMaximumLimitation() finished with failure.");
        return false;
    }
}
