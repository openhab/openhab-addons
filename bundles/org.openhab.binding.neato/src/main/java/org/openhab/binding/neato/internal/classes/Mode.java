/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.neato.internal.classes;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The enum {@link Mode} is the internal class to set cleaning mode to the cleaning request
 *
 * @author Lapenta Giuseppe - Initial Contribution
 */
@NonNullByDefault
public enum Mode {
    ECO(1),
    TURBO(2); // Note that navigationMode can only be set to 3 if mode is 2, otherwise an error will be returned.

    private final int mode;

    Mode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
