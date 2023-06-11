/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.velux.internal.bridge.common.RunProductDiscovery;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Ask the Bridge to detect (new) products/actuators</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * There are no methods in addition to the mentioned interface.
 *
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCrunProductDiscovery extends RunProductDiscovery implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCrunProductDiscovery.class);

    private static final String DESCRIPTION = "Detect Products/Actuators";
    private static final Command COMMAND = Command.GW_CS_DISCOVER_NODES_REQ;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqNodeType = 0; // NO_TYPE (All nodes except controller)

    /*
     * ===========================================================
     * Message Objects
     */

    private byte[] requestData = new byte[0];

    /*
     * ===========================================================
     * Result Objects
     */

    private boolean success = false;
    private boolean finished = false;

    /*
     * ===========================================================
     * Methods required for interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        success = false;
        finished = false;
        logger.debug("getRequestCommand() returns {} ({}).", COMMAND.name(), COMMAND.getCommand());
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        logger.trace("getRequestDataAsArrayOfBytes() returns data for detection with type {}.", reqNodeType);
        Packet request = new Packet(new byte[1]);
        request.setOneByteValue(0, reqNodeType);
        requestData = request.toByteArray();
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = false;
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_CS_DISCOVER_NODES_CFM:
                logger.trace("setResponse(): received confirmation for discovery mode.");
                break;

            case GW_CS_DISCOVER_NODES_NTF:
                finished = true;
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 131)) {
                    break;
                }
                int ntfDiscoverStatus = responseData.getOneByteValue(130);
                switch (ntfDiscoverStatus) {
                    case 0:
                        logger.trace("setResponse(): returned status: OK. Discovered nodes. See bit array.");
                        success = true;
                        break;
                    case 5:
                        logger.warn("setResponse(): returned status: ERROR - Failed. CS not ready.");
                        break;
                    case 6:
                        logger.trace(
                                "setResponse(): returned status: OK. But some nodes were not added to system table (e.g. System table has reached its limit).");
                        break;
                    case 7:
                        logger.warn("setResponse(): returned status: ERROR - CS busy with another task.");
                        break;
                    default:
                        logger.warn("setResponse({}): returned status={} (Reserved/unknown).",
                                Command.get(responseCommand).toString(), ntfDiscoverStatus);
                        break;
                }
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
                finished = true;
        }
        KLF200Response.outroLogging(logger, success, finished);
    }

    @Override
    public boolean isCommunicationFinished() {
        return finished;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return success;
    }
}
