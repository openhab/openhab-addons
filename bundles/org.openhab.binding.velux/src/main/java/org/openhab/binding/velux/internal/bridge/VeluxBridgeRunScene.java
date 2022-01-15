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
import org.openhab.binding.velux.internal.bridge.common.RunScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeRunScene} represents a complete set of transactions
 * for executing a scene defined on the <B>Velux</B> bridge.
 * <P>
 * It provides a method {@link VeluxBridgeRunScene#execute} for execution of a scene.
 * Any parameters are controlled by {@link org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration}.
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeRunScene {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeRunScene.class);

    /**
     * Login into bridge, execute a scene and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param sceneNo Number of scene to be executed.
     * @return true if successful, and false otherwise.
     */
    public boolean execute(VeluxBridge bridge, int sceneNo) {
        logger.trace("execute({}) called.", sceneNo);

        RunScene bcp = bridge.bridgeAPI().runScene().setSceneId(sceneNo);
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.debug("execute() finished successfully.");
            return true;
        } else {
            logger.trace("execute() finished with failure.");
            return false;
        }
    }

    /**
     * Login into bridge, execute a scene and logout from bridge based
     * on a well-prepared environment of a {@link VeluxBridgeProvider}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @param sceneNo Number of scene to be executed.
     * @param velocity integer representing the velocity.
     * @return true if successful, and false otherwise.
     */
    public boolean execute(VeluxBridge bridge, int sceneNo, int velocity) {
        logger.trace("execute({},{}) called.", sceneNo, velocity);

        RunScene bcp = bridge.bridgeAPI().runScene().setVelocity(velocity).setSceneId(sceneNo);
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.debug("execute() finished successfully.");
            return true;
        } else {
            logger.trace("execute() finished with failure.");
            return false;
        }
    }
}
