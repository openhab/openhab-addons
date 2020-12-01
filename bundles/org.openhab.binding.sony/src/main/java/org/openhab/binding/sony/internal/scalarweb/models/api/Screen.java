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
 * This class represents the screen and is used for serialization/deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Screen {
    /** The screen identifier */
    private @Nullable String screen;

    /**
     * Constructor used for deserialization only
     */
    public Screen() {
    }

    /**
     * Instantiates a new screen request
     *
     * @param screen the screen identifier
     */
    public Screen(final String screen) {
        Validate.notEmpty(screen, "screen cannot be empty");
        this.screen = screen;
    }

    /**
     * Gets the screen identifier
     *
     * @return the screen identifier
     */
    public @Nullable String getScreen() {
        return screen;
    }

    @Override
    public String toString() {
        return "Screen [screen=" + screen + "]";
    }
}
