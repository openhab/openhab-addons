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
 * Command to perform a login operation on the KLF200 unit. Specifically, a
 * GW_PASSWORD_ENTER_REQ is sent with the password and then handle and interpret
 * a GW_PASSWORD_ENTER_RES response to determine if the login was successful.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdLogin extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdLogin.class);

    /** Password for the KLF200 unit. */
    String password;

    /**
     * Constructor, expects the {@link password} to be supplied.
     *
     * @param password
     *                     Password for the KLF200 unit.
     */
    public KlfCmdLogin(String password) {
        super();
        this.password = password;
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
        return KLFCommandStructure.LOGIN;
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
            case KLFCommandCodes.GW_PASSWORD_ENTER_CFM:
                logger.trace("Handling GW_PASSWORD_ENTER_CFM with payload {}", KLFUtils.formatBytes(data));
                if (data[FIRSTBYTE] == 0) {
                    // Authentication was successful.
                    this.commandStatus = CommandStatus.COMPLETE;
                } else {
                    // Authentication failed.
                    this.commandStatus = CommandStatus.ERROR;
                }
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
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#isValid()
     */
    @Override
    public boolean isValid() {
        if ((null == this.password) || (this.password.length() < 1) || (this.password.length() > 32)) {
            logger.error("The password is not valid.");
            return false;
        }
        return true;
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
        byte[] data = new byte[32];
        byte[] password = this.password.getBytes();
        if (password.length > data.length) {
            logger.error(
                    "Password specified is longer ({} characters) than the maximum lenght (32 characters) permissible.",
                    password.length);
            return null;
        }
        System.arraycopy(password, 0, data, 0, password.length);
        return data;
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
