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
 * The enum {@link Category} is the internal class to set category to the cleaning request
 *
 * @author Lapenta Giuseppe - Initial Contribution
 */
@NonNullByDefault
public enum Category {
    MANUAL(1),
    HOUSE(2),
    SPOT(3),
    MAP(4);

    private final int category;

    Category(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }
}
