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
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DATE_TIME_ITEM;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PLCDateTimeConfiguration} holds configuration of Siemens LOGO! PLC
 * analog input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDateTimeConfiguration extends PLCCommonConfiguration {

    private String block = "";
    private String type = "";

    /**
     * Get configured Siemens LOGO! block name.
     *
     * @return Configured Siemens LOGO! block name
     */
    public String getBlockName() {
        return block;
    }

    /**
     * Set Siemens LOGO! block name.
     *
     * @param name Siemens LOGO! block name
     */
    public void setBlockName(final String name) {
        this.block = name.trim();
    }

    /**
     * Get configured Siemens LOGO! block name.
     *
     * @return Configured Siemens LOGO! block name
     */
    public String getBlockType() {
        return type;
    }

    /**
     * Set Siemens LOGO! block name.
     *
     * @param type Siemens LOGO! output block type
     */
    public void setBlockType(final String type) {
        this.type = type.trim();
    }

    @Override
    public String getChannelType() {
        return DATE_TIME_ITEM;
    }

    @Override
    public String getBlockKind() {
        return block.substring(0, 2);
    }
}
