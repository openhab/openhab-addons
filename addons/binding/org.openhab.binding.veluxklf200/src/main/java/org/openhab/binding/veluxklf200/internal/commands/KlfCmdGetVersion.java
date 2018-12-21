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
 * The Class KLFCMD_GetVersion.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdGetVersion extends BaseKLFCommand {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(KlfCmdGetVersion.class);

    /** The software version. */
    private String softwareVersion;

    /** The hardware version. */
    private String hardwareVersion;

    /** The product type. */
    private String productType;

    /** The product group. */
    private String productGroup;

    /**
     * Instantiates a new KLFCM D get version.
     */
    public KlfCmdGetVersion() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        short responseCode = KLFUtils.decodeKLFCommand(data);
        switch (responseCode) {
            case KLFCommandCodes.GW_GET_VERSION_CFM:
                this.softwareVersion = (KLFUtils.extractOneByte(data[FIRSTBYTE]) & 0xFF) + ".";
                this.softwareVersion += (KLFUtils.extractOneByte(data[FIRSTBYTE + 1]) & 0xFF) + ".";
                this.softwareVersion += (KLFUtils.extractOneByte(data[FIRSTBYTE + 2]) & 0xFF) + ".";
                this.softwareVersion += (KLFUtils.extractOneByte(data[FIRSTBYTE + 3]) & 0xFF) + ".";
                this.softwareVersion += (KLFUtils.extractOneByte(data[FIRSTBYTE + 4]) & 0xFF) + ".";
                this.softwareVersion += (KLFUtils.extractOneByte(data[FIRSTBYTE + 5]) & 0xFF);

                this.hardwareVersion = "" + (data[FIRSTBYTE + 6] & 0xFF);

                switch (data[FIRSTBYTE + 7]) {
                    case 14:
                        this.productGroup = "Remote Control";
                        break;
                    default:
                        this.productGroup = "Unknown";
                }

                switch (data[FIRSTBYTE + 8]) {
                    case 3:
                        this.productType = "KLF200";
                        break;
                    default:
                        this.productType = "Unknown";
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

    /**
     * Gets the software version.
     *
     * @return the software version
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Gets the hardware version.
     *
     * @return the hardware version
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }

    /**
     * Gets the product type.
     *
     * @return the product type
     */
    public String getProductType() {
        return productType;
    }

    /**
     * Gets the product group.
     *
     * @return the product group
     */
    public String getProductGroup() {
        return productGroup;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#getKLFCommandStructure()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.GET_VERSION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        return new byte[] {};
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#extractSession(short, byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }
}
