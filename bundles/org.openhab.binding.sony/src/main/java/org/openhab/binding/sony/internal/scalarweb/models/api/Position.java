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
 * This class represents the request to set the picture-in-picture (PIP) location and is used for
 * deserialization/serialization
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Position {
    /** The PIP position */
    private @Nullable String position;

    /**
     * Constructor used for deserialization only
     */
    public Position() {
    }

    /**
     * Instantiates a new position
     *
     * @param position the non-null, non-empty position
     */
    public Position(final String position) {
        Validate.notEmpty(position, "position cannot be empty");
        this.position = position;
    }

    /**
     * Gets the position
     *
     * @return the position
     */
    public @Nullable String getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Position [position=" + position + "]";
    }
}
