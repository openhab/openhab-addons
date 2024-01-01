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
import org.openhab.binding.velux.internal.bridge.common.SetHouseStatusMonitor;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Modify HouseStatusMonitor</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #serviceActivation} to define the new service activation state.</LI>
 * </UL>
 *
 * @see SetHouseStatusMonitor
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCsetHouseStatusMonitor extends SetHouseStatusMonitor implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCsetHouseStatusMonitor.class);

    private static final String DESCRIPTION = "Modify HouseStatusMonitor";

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private boolean activateService = false;

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
        Command command = activateService ? Command.GW_HOUSE_STATUS_MONITOR_ENABLE_REQ
                : Command.GW_HOUSE_STATUS_MONITOR_DISABLE_REQ;
        success = false;
        finished = false;
        logger.debug("getRequestCommand() returns {} ({}).", command.name(), command.getCommand());
        return command.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        logger.debug("getRequestDataAsArrayOfBytes() data is {}.", new Packet(requestData).toString());
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = true;
        switch (Command.get(responseCommand)) {
            case GW_HOUSE_STATUS_MONITOR_ENABLE_CFM:
                logger.trace("setResponse(): service enable confirmed by bridge.");
                // returned enabled: successful if enable requested
                success = activateService;
                break;
            case GW_HOUSE_STATUS_MONITOR_DISABLE_CFM:
                logger.trace("setResponse(): service disable confirmed by bridge.");
                // returned disabled: successful if disable requested
                success = !activateService;
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
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
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link SetHouseStatusMonitor}
     */

    @Override
    public SetHouseStatusMonitor serviceActivation(boolean enableService) {
        this.activateService = enableService;
        return this;
    }
}
