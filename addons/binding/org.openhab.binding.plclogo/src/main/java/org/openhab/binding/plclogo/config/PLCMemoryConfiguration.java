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
 * The {@link PLCMemoryConfiguration} is a class for configuration
 * of Siemens LOGO! PLC memory input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCMemoryConfiguration extends PLCCommonConfiguration {

    private String block;
    private @NonNull Integer threshold = 0;

    /**
     * Get configured Siemens LOGO! memory block name.
     *
     * @return Configured Siemens LOGO! memory block name
     */
    public @NonNull String getBlockName() {
        return block;
    }

    /**
     * Set Siemens LOGO! memory block name.
     *
     * @param name Siemens LOGO! memory block name
     */
    public void setBlockName(final @NonNull String name) {
        Objects.requireNonNull(name, "PLCMemoryConfiguration: Block name may not be null.");
        this.block = name.trim();
    }

    /**
     * Get Siemens LOGO! blocks update threshold.
     *
     * @return Configured Siemens LOGO! update threshold
     */
    public @NonNull Integer getThreshold() {
        return threshold;
    }

    /**
     * Set Siemens LOGO! blocks update threshold.
     *
     * @param force Force update of Siemens LOGO! blocks
     */
    public void setThreshold(final @NonNull Integer threshold) {
        Objects.requireNonNull(threshold, "PLCMemoryConfiguration: Threshold may not be null.");
        this.threshold = threshold;
    }

    @Override
    public @NonNull String getChannelType() {
        final String kind = getBlockKind();
        return kind.equalsIgnoreCase("VB") && block.contains(".") ? "Switch" : "Number";
    }

    @Override
    public @NonNull String getBlockKind() {
        return block.substring(0, 2);
    }

}
