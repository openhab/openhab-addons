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

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PLCDateTimeConfiguration} holds configuration of Siemens LOGO! PLC
 * analog input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCDateTimeConfiguration extends PLCCommonConfiguration {

    private String block;
    private String type;

    /**
     * Get configured Siemens LOGO! block name.
     *
     * @return Configured Siemens LOGO! block name
     */
    public @NonNull String getBlockName() {
        return block;
    }

    /**
     * Set Siemens LOGO! block name.
     *
     * @param name Siemens LOGO! block name
     */
    public void setBlockName(final @NonNull String name) {
        Objects.requireNonNull(name, "PLCDateTimeConfiguration: Block name may not be null.");
        this.block = name.trim();
    }

    /**
     * Get configured Siemens LOGO! block name.
     *
     * @return Configured Siemens LOGO! block name
     */
    public @NonNull String getBlockType() {
        return type;
    }

    /**
     * Set Siemens LOGO! block name.
     *
     * @param name Siemens LOGO! output block name
     */
    public void setBlockType(final @NonNull String type) {
        Objects.requireNonNull(type, "PLCDateTimeConfiguration: Block name may not be null.");
        this.type = type.trim();
    }

    @Override
    public @NonNull String getChannelType() {
        return "DateTime";
    }

    @Override
    public @NonNull String getBlockKind() {
        return block.substring(0, 2);
    }

}
