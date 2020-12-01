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
 * This class represents the browser control and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class BrowserControl {

    /** The browser control */
    private final @Nullable String control;

    /**
     * Instantiates a new browser control.
     *
     * @param control the non-null, non-empty control
     */
    public BrowserControl(final @Nullable String control) {
        Validate.notEmpty(control, "control cannot be empty");
        this.control = control;
    }

    /**
     * Gets the control
     *
     * @return the control
     */
    public @Nullable String getControl() {
        return control;
    }

    @Override
    public String toString() {
        return "BrowserControl [control=" + control + "]";
    }
}
