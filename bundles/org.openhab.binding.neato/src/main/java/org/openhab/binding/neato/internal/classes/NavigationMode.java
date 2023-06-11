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
package org.openhab.binding.neato.internal.classes;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The enum {@link NavigationMode} is the internal class to set navigation mode to the cleaning request
 *
 * @author Lapenta Giuseppe - Initial Contribution
 */
@NonNullByDefault
public enum NavigationMode {
    NORMAL(1),
    EXTRA_CARE(2),
    DEEP(3); // Note that navigationMode can only be set to 3 if mode is 2, otherwise an error will be returned.

    private final int navigationMode;

    NavigationMode(int navigationMode) {
        this.navigationMode = navigationMode;
    }

    public int getNavigationMode() {
        return navigationMode;
    }
}
