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
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.GetDeviceStatus;
import org.openhab.binding.velux.internal.things.VeluxGwState;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeDeviceStatus} represents a complete set of transactions
 * for querying device status on the <B>Velux</B> bridge.
 * <P>
 * It therefore provides a method
 * <UL>
 * <LI>{@link #retrieve} for starting the detection.
 * <LI>{@link #getChannel} for accessing the retrieved information.
 * </UL>
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeDeviceStatus {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeDeviceStatus.class);

    // Type definitions, class-internal variables

    /**
     * Bridge information consisting of:
     * <ul>
     * <li>{@link #isRetrieved} describing the retrieval state,
     * <li>{@link #gwState} containing the brief gateway state,
     * <li>{@link #gwStateDescription} containing the verbose gateway state.
     * </ul>
     */
    public class Channel {
        public boolean isRetrieved = false;
        public StringType gwState = new StringType(VeluxBindingConstants.UNKNOWN);
        public StringType gwStateDescription = new StringType(VeluxBindingConstants.UNKNOWN);
    }

    private Channel channel;

    // Constructor methods

    /**
     * Constructor.
     * <P>
     * Initializes the internal data structure {@link #channel} of Velux actuators/products,
     * which is publicly accessible via the method {@link #getChannel()}.
     */
    public VeluxBridgeDeviceStatus() {
        logger.trace("VeluxBridgeDeviceStatus(constructor) called.");
        channel = new Channel();
    }

    // Class access methods

    /**
     * Provide access to the internal structure of the device status.
     *
     * @return a channel describing the overall actual device status.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Complete workflow for retrieving the firmware version, consisting of Login into bridge, querying the firmware
     * version and logout from bridge based on a well-prepared environment of a {@link VeluxBridgeProvider}, where the
     * results are stored in {@link VeluxBridgeDeviceStatus#channel}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @return <b>channel</b> of type {@link VeluxBridgeDeviceStatus.Channel} describing the overall result of this
     *         interaction.
     */
    public Channel retrieve(VeluxBridge bridge) {
        logger.trace("retrieve() called. About to query device status.");
        GetDeviceStatus bcp = bridge.bridgeAPI().getDeviceStatus();
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            VeluxGwState state = bcp.getState();
            channel.gwState = new StringType(state.toString());
            channel.gwStateDescription = new StringType(state.toDescription());
            channel.isRetrieved = true;
            logger.trace("retrieve() finished successfully with result {}.", state.toDescription());
        } else {
            channel.isRetrieved = false;
            logger.trace("retrieve() finished with failure.");
        }
        return channel;
    }
}
