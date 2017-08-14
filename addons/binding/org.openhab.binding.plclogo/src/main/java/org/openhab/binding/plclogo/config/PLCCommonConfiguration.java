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
 * The {@link PLCCommonConfiguration} is a base class for configuration
 * of Siemens LOGO! PLC blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
abstract class PLCCommonConfiguration {

    private @NonNull Boolean force = false;

    /**
     * Returns if Siemens LOGO! channels update must be forced.
     *
     * @return True, if channels update to be forced and false otherwise
     */
    public @NonNull Boolean isUpdateForced() {
        return force;
    }

    /**
     * Set Siemens LOGO! channels update must be forced.
     *
     * @param force Force update of Siemens LOGO! block
     */
    public void setForceUpdate(final @NonNull Boolean force) {
        Objects.requireNonNull(force, "PLCCommonConfiguration: Force may not be null.");
        this.force = force;
    }

    /**
     * Get configured Siemens LOGO! blocks kind.
     * Can be I, Q, M, NI or NQ for digital blocks, AI, AM,
     * AQ, NAI or NAQ for analog and VB, VW or VD for memory
     *
     * @return Configured Siemens LOGO! blocks kind
     */
    public abstract @NonNull String getBlockKind();

    public abstract @NonNull String getChannelType();

}
