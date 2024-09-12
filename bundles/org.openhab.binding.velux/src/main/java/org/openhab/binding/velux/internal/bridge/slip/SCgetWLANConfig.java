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
package org.openhab.binding.velux.internal.bridge.slip;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.GetWLANConfig;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxGwWLAN;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve WLAN configuration</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getWLANConfig} to retrieve the current WLAN configuration.</LI>
 * </UL>
 *
 * @see GetWLANConfig
 * @see SlipBridgeCommunicationProtocol
 *
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetWLANConfig extends GetWLANConfig implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetWLANConfig.class);

    private static final String DESCRIPTION = "Retrieve WLAN configuration";
    private static final Command COMMAND = Command.GW_GET_NETWORK_SETUP_REQ;

    /*
     * Message Objects
     */

    private byte[] requestData = new byte[0];
    private short responseCommand;
    @SuppressWarnings("unused")
    private byte @Nullable [] responseData;

    /*
     * ===========================================================
     * Constructor Method
     */

    public SCgetWLANConfig() {
        logger.trace("SCgetWLANConfig(constructor) called.");
        requestData = new byte[1];
    }

    /*
     * ===========================================================
     * Methods required for interface {@link SlipBridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        return requestData;
    }

    @Override
    public void setResponse(short thisResponseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        logger.trace("setResponseCommand({}, {}) called.", thisResponseCommand, new Packet(thisResponseData));
        responseCommand = thisResponseCommand;
        responseData = thisResponseData;
    }

    @Override
    public boolean isCommunicationFinished() {
        return true;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return (responseCommand == Command.GW_GET_NETWORK_SETUP_CFM.getShort());
    }

    /*
     * ===========================================================
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public VeluxGwWLAN getWLANConfig() {
        logger.trace("getWLANConfig() called.");
        // Enhancement idea: Velux should provide an enhanced API.
        return new VeluxGwWLAN(VeluxBindingConstants.UNKNOWN, VeluxBindingConstants.UNKNOWN);
    }
}
