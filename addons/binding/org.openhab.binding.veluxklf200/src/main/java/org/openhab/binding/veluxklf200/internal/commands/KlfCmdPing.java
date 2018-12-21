/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a 'ping' of the KLF200 unit. This command is typically used to keep
 * the KLF200's socket connection alive. After a period of approximately 15
 * minutes of inactivity, the KLF200 unit shuts down its sockets. Periodically
 * sending this command will keep this from happening.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdPing extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdPing.class);

    /** The gateway state. */
    private String gatewayState;

    /** The gateway sub state. */
    private String gatewaySubState;

    /**
     * Gets the gateway state.
     *
     * @return the gateway state
     */
    public String getGatewayState() {
        return gatewayState + "::" + gatewaySubState;
    }

    /**
     * Constructor.
     */
    public KlfCmdPing() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        short responseCode = KLFUtils.decodeKLFCommand(data);
        switch (responseCode) {
            case KLFCommandCodes.GW_GET_STATE_CFM:
                switch (data[FIRSTBYTE]) {
                    case 0x0:
                        gatewayState = "Test mode";
                        break;
                    case 0x1:
                        gatewayState = "Gateway mode, no actuator nodes in the system table";
                        break;
                    case 0x2:
                        gatewayState = "Gateway mode, with one or more actuator nodes in the system table";
                        break;
                    case 0x3:
                        gatewayState = "Beacon mode, not configured by a remote controller";
                        break;
                    case 0x4:
                        gatewayState = "Beacon mode, has been configured by a remote controller";
                        break;
                    default:
                        gatewayState = "Unknown";
                        break;
                }
                switch (data[FIRSTBYTE + 1]) {
                    case 0x0:
                        gatewaySubState = "Idle state";
                        break;
                    case 0x1:
                        gatewaySubState = "Performing task in Configuration Service handler";
                        break;
                    case 0x2:
                        gatewaySubState = "Performing Scene Configuration";
                        break;
                    case 0x3:
                        gatewaySubState = "Performing Information Service Configuration";
                        break;
                    case 0x4:
                        gatewaySubState = "Performing Contact input Configuration";
                        break;
                    case (byte) 0x80:
                        gatewaySubState = "Performing task in Command Handler";
                        break;
                    case (byte) 0x81:
                        gatewaySubState = "Performing task in Activate Group Handler";
                        break;
                    case (byte) 0x82:
                        gatewaySubState = "Performing task in Activate Scene Handler";
                        break;
                    default:
                        gatewaySubState = "Unknown";
                        break;
                }
                this.commandStatus = CommandStatus.COMPLETE;
                break;
            default:
                // This should not happen. If it does, the most likely cause is that
                // the KLFCommandStructure has not been configured or implemented
                // correctly.
                this.commandStatus = CommandStatus.ERROR;
                logger.error("Processing requested for a KLF response code (command code) that is not supported: {}.",
                        responseCode);
                break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#getKLFCommandStructure
     * ()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.PING;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        return new byte[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }

}
