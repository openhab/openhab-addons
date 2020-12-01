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
package org.openhab.binding.sony.internal.providers.sources;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper class representing a name conversion and is used for deserialization only
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
class MetaConvert {
    /** The old name pattern */
    private final Pattern oldName;

    /** The new name */
    private final String newName;

    /**
     * Constructs the conversion from the old name (pattern) to a new name
     * 
     * @param oldName a non-null old name pattern
     * @param newName a non-null, non-empty new name to convert to
     */
    MetaConvert(final Pattern oldName, final String newName) {
        Objects.requireNonNull(oldName, "oldName cannot be null");
        Validate.notEmpty(newName, "newName cannot be empty");

        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * The old name pattern
     * 
     * @return a possibly null old name pattern
     */
    public Pattern getOldName() {
        return oldName;
    }

    /**
     * The new name
     * 
     * @return a possibly null, possibly empty name
     */
    public @Nullable String getNewName() {
        return newName;
    }
}
