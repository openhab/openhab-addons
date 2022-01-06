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
import org.openhab.binding.velux.internal.bridge.common.SetSceneVelocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeSetSceneVelocity} represents a complete set of transactions
 * for modifying the silent-mode of a scene defined on the <B>Velux</B> bridge.
 * <P>
 * It therefore provides a method
 * <UL>
 * <LI>{@link VeluxBridgeSetSceneVelocity#setSilentMode} for modifying the behaviour of a scene.
 * </UL>
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@Deprecated
@NonNullByDefault
public class VeluxBridgeSetSceneVelocity {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeSetSceneVelocity.class);

    // Class access methods

    /**
     * Login into bridge, modify the scene parameters and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param sceneNo Number of scene to be modified.
     * @param silentMode Mode of this mentioned scene.
     * @return true if successful, and false otherwise.
     */
    public boolean setSilentMode(VeluxBridge bridge, int sceneNo, boolean silentMode) {
        logger.trace("setSilentMode({},{}) called.", sceneNo, silentMode);

        SetSceneVelocity bcp = bridge.bridgeAPI().setSceneVelocity();
        bcp.setMode(sceneNo, silentMode);
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.trace("setSilentMode() finished successfully.");
            return true;
        }
        logger.trace("setSilentMode() finished with failure.");
        return false;
    }
}
