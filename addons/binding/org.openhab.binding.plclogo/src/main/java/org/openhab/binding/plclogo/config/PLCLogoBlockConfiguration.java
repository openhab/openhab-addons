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
     * Get configured Siemens LOGO! output block name.
     *
     * @return Configured Siemens LOGO! output block name
     */
    public String getBlockName() {
        return block;
    }

    /**
     * Set Siemens LOGO! output block name.
     *
     * @param name Siemens LOGO! output block name
     */
    public void setBlockName(final String name) {
        Objects.requireNonNull(name, "PLCLogoBlockConfiguration: Block name may not be null.");
        this.block = name.trim();
    }

    /**
     * Returns if Siemens LOGO! block channel update must be forced.
     *
     * @return True, if channel update to be forced and false otherwise
     */
    public Boolean isUpdateForced() {
        return force;
    }

    /**
     * Set Siemens LOGO! block update must be forced.
     *
     * @param force Force update of Siemens LOGO! block
     */
    public void setForceUpdate(final Boolean force) {
        Objects.requireNonNull(force, "PLCLogoBlockConfiguration: Force may not be null.");
        this.force = force;
    }

    /**
     * Returns configured LOGO! block kind.
     * Can be VB, VD, VW, I, Q, M, AI, AQ, AM, NI, NAI, NQ or NAQ
     *
     * @param name Name of the LOGO! block
     * @see PLCLogoBindingConstants#LOGO_MEMORY_0BA7
     * @see PLCLogoBindingConstants#LOGO_MEMORY_0BA8
     * @return Kind of configured block
     */
    public String getBlockKind(final String name) {
        if (name != null) {
            if (Character.isDigit(name.charAt(1))) {
                return name.substring(0, 1);
            } else if (Character.isDigit(name.charAt(2))) {
                return name.substring(0, 2);
            } else if (Character.isDigit(name.charAt(3))) {
                return name.substring(0, 3);
            }
        }
        return null;
    }

    /**
     * Checks if block configuration is valid.
     *
     * @param name Name of the LOGO! block to check
     * @return True, if the name is valid and false otherwise
     */
    public abstract boolean isBlockValid(final String name);

    /**
     * Returns if block represents input.
     *
     * @param name Name of the LOGO! block to check
     * @return True, if block is input and false otherwise
     */
    public abstract boolean isInputBlock(final String name);

    /**
     * Return supported item type for this block.
     *
     * @return Supported item type
     */
    public abstract String getItemType();

}
