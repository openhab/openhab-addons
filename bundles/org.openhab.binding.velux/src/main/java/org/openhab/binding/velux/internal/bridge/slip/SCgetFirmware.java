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
import org.openhab.binding.velux.internal.bridge.common.GetFirmware;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxGwFirmware;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Get Firmware Version</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getFirmware} to retrieve the Velux firmware version.</LI>
 * </UL>
 *
 * @see GetFirmware
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetFirmware extends GetFirmware implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetFirmware.class);

    private static final String DESCRIPTION = "Retrieve firmware version";
    private static final Command COMMAND = Command.GW_GET_VERSION_REQ;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int cfmSoftwareVersionCommand = 0;
    private int cfmSoftwareVersionWhole = 0;
    private int cfmSoftwareVersionSub = 0;
    private int cfmSoftwareVersionBranch = 0;
    private int cfmSoftwareVersionBuild = 0;
    private int cfmSoftwareVersionMicroBuild = 0;
    private int cfmHardwareVersion = 0;
    private int cfmProductGroup = 0;
    private int cfmProductType = 0;

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
     * Methods required for interface {@link SlipBridgeCommunicationProtocol}.
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
        requestData = new byte[1];
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = false;
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_GET_VERSION_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 9)) {
                    finished = true;
                    break;
                }
                cfmSoftwareVersionCommand = responseData.getOneByteValue(0);
                cfmSoftwareVersionWhole = responseData.getOneByteValue(1);
                cfmSoftwareVersionSub = responseData.getOneByteValue(2);
                cfmSoftwareVersionBranch = responseData.getOneByteValue(3);
                cfmSoftwareVersionBuild = responseData.getOneByteValue(4);
                cfmSoftwareVersionMicroBuild = responseData.getOneByteValue(5);
                cfmHardwareVersion = responseData.getOneByteValue(6);
                cfmProductGroup = responseData.getOneByteValue(7);
                cfmProductType = responseData.getOneByteValue(8);
                success = true;
                finished = true;
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

    /*
     * ===========================================================
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public VeluxGwFirmware getFirmware() {
        String result = String.format("Software version %d.%d.%d.%d.%d.%d, Hardware version %d.%d.%d",
                cfmSoftwareVersionCommand, cfmSoftwareVersionWhole, cfmSoftwareVersionSub, cfmSoftwareVersionBranch,
                cfmSoftwareVersionBuild, cfmSoftwareVersionMicroBuild, cfmHardwareVersion, cfmProductGroup,
                cfmProductType);
        logger.trace("getFirmware() returns {}.", result);
        return new VeluxGwFirmware(result);
    }
}
