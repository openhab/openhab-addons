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
import org.openhab.binding.velux.internal.bridge.common.GetLANConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxBridgeLANConfig} represents a complete set of transactions
 * for retrieving the network configuration of the <B>Velux</B> bridge.
 * <P>
 * It provides the following methods:
 * <UL>
 * <LI>{@link #retrieve} for retrieval of information.
 * <LI>{@link #getChannel} for accessing the retrieved information.
 * </UL>
 *
 * @see VeluxBridgeProvider
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeLANConfig {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeLANConfig.class);

    // Type definitions, class-internal variables

    /**
     * IP Network configuration, consisting of:
     * <ul>
     * <li>isRetrieved (boolean flag),
     * <li>ipAddress,
     * <li>subnetMask,
     * <li>defaultGW and
     * <li>enabledDHCP.
     * </ul>
     */
    public class Channel {
        public boolean isRetrieved = false;
        public StringType openHABipAddress = new StringType(VeluxBindingConstants.UNKNOWN);
        public StringType openHABsubnetMask = new StringType(VeluxBindingConstants.UNKNOWN);
        public StringType openHABdefaultGW = new StringType(VeluxBindingConstants.UNKNOWN);
        public OnOffType openHABenabledDHCP = OnOffType.OFF;
    }

    private Channel channel;

    // Constructor methods

    /**
     * Constructor.
     * <P>
     * Initializes the internal data structure {@link #channel} of Velux LAN information,
     * which is publicly accessible via the method {@link #getChannel()}.
     */
    public VeluxBridgeLANConfig() {
        logger.trace("VeluxBridgeLANConfig(constructor) called.");
        channel = new Channel();
    }

    // Class access methods

    /**
     * Provide access to the internal structure of LAN information.
     *
     * @return a channel describing the overall actual LAN information.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Complete workflow for retrieving the network configuration, consisting of Login into bridge, querying
     * the network configuration and logout from bridge based on a well-prepared environment of a
     * {@link VeluxBridgeProvider}, where the results are stored within as well in
     * {@link VeluxBridgeLANConfig#channel}.
     *
     * @param bridge Initialized Velux bridge handler.
     * @return <b>channel</b> of type {@link VeluxBridgeLANConfig.Channel} describing the overall result of this
     *         interaction.
     */
    public Channel retrieve(VeluxBridge bridge) {
        logger.trace("retrieve() called.");

        GetLANConfig bcp = bridge.bridgeAPI().getLANConfig();
        if (bridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
            logger.trace("retrieve() found successfully configuration {}.", bcp.getLANConfig());
            channel.openHABipAddress = new StringType(bcp.getLANConfig().getIpAddress());
            channel.openHABsubnetMask = new StringType(bcp.getLANConfig().getSubnetMask());
            channel.openHABdefaultGW = new StringType(bcp.getLANConfig().getDefaultGW());
            channel.openHABenabledDHCP = OnOffType.from(bcp.getLANConfig().getDHCP());
            channel.isRetrieved = true;
        } else {
            channel.isRetrieved = false;
            logger.trace("retrieve() finished with failure.");
        }
        return channel;
    }
}
