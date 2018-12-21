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
 * Command to enable the HomeStatus monitor on the KLF200
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdEnableHomeStatusMonitor extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdEnableHomeStatusMonitor.class);

    /**
     * Constructor
     *
     */
    public KlfCmdEnableHomeStatusMonitor() {
        super();
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
        return KLFCommandStructure.ENABLE_HOUSE_STATUS_MONITOR;
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
            case KLFCommandCodes.GW_HOUSE_STATUS_MONITOR_ENABLE_CFM:
                logger.debug("Completed enabling of the Home Status Monitor.");
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
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     *
     * Encodes the password into a byte array with a fixed length of 32 bytes.
     * If the password is shorter than this, the array is padded with zero's.
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
