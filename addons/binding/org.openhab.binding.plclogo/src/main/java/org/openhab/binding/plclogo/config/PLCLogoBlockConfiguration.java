/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.config;

import java.util.Objects;

import org.openhab.binding.plclogo.PLCLogoBindingConstants;

/**
 * The {@link PLCLogoBlockConfiguration} is a base class for configuration
 * of Siemens LOGO! PLC input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public abstract class PLCLogoBlockConfiguration {

    private String block;
    private Boolean force = false;

    /**
     * Get configured Siemens LOGO! device block name.
     *
     * @return Configured Siemens LOGO! block name
     */
    public String getBlockName() {
        return block;
    }

    /**
     * Set Siemens LOGO! device block name.
     *
     * @param name Siemens LOGO! block name
     */
    public void setBlockName(final String name) {
        Objects.requireNonNull(name, "PLCLogoBlockConfiguration: Block name may not be null.");
        this.block = name.trim();
    }

    /**
     * Returns if Siemens LOGO! device block channel update must be forced.
     *
     * @return True, if channel update to be forced and false otherwise
     */
    public Boolean isUpdateForced() {
        return force;
    }

    /**
     * Set Siemens LOGO! device blocks update must be forced.
     *
     * @param force Force update of Siemens LOGO! device blocks
     */
    public void setForceUpdate(final Boolean force) {
        Objects.requireNonNull(force, "PLCLogoBlockConfiguration: Force may not be null.");
        this.force = force;
    }

    /**
     * Checks if current block configuration is valid.
     *
     * @return True, if the name is valid and false otherwise
     */
    public abstract boolean isBlockValid();

    /**
     * Returns configured LOGO! block kind.
     * Can be VB, VD, VW, I, Q, M, AI, AQ, AM, NI, NAI, NQ or NAQ
     *
     * @see PLCLogoBindingConstants#LOGO_MEMORY_0BA7
     * @see PLCLogoBindingConstants#LOGO_MEMORY_0BA8
     * @return Kind of configured block
     */
    public String getBlockKind() {
        if (Character.isDigit(block.charAt(1))) {
            return block.substring(0, 1);
        } else if (Character.isDigit(block.charAt(2))) {
            return block.substring(0, 2);
        } else if (Character.isDigit(block.charAt(3))) {
            return block.substring(0, 3);
        }
        return null;
    }

    /**
     * Returns if block represents input.
     *
     * @return True, if block is input and false otherwise
     */
    public abstract boolean isInputBlock();

    /**
     * Return supported item type for this block.
     *
     * @return Supported item type
     */
    public abstract String getItemType();
}
