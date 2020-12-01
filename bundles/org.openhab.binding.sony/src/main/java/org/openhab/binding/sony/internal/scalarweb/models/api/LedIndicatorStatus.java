/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the request to set the LED status and is used for serialization/deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class LedIndicatorStatus {

    /** The mode */
    private @Nullable String mode;

    /** The status */
    private @Nullable String status;

    /**
     * Constructor used for deserialization only
     */
    public LedIndicatorStatus() {
    }

    /**
     * Instantiates a new led indicator status.
     *
     * @param mode the non-null, non-empty mode
     * @param status the non-null, non-empty status
     */
    public LedIndicatorStatus(final String mode, final String status) {
        Validate.notEmpty(mode, "mode cannot be empty");
        Validate.notEmpty(status, "status cannot be empty");
        this.mode = mode;
        this.status = status;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public @Nullable String getMode() {
        return mode;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public @Nullable String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "LedIndicatorStatus [mode=" + mode + ", status=" + status + "]";
    }
}
