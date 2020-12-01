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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents a count and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Count {

    /** The count */
    private final int count;

    /**
     * Instantiates a new count from the specified count
     *
     * @param count the count
     */
    public Count(final int count) {
        this.count = count;
    }

    /**
     * Gets the count
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Count [count=" + count + "]";
    }
}
