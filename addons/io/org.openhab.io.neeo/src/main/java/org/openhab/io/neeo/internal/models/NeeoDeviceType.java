/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines a NEEO device type. There are many unknown and not working device types that NEEO supports and has never
 * published. This helper class will simply identify two common types - an EXCLUDE (which excludes the associated thing
 * from NEEO) and the ACCESSOIRE (which is the generic device type supported by NEEO). All other device types may be
 * entered by the user and will be passed directly to the NEEO brain (regardless if it's valid or not).
 *
 * Note: this class will also be used for backward compatibility at some point (when they change ACCESSOIRE to
 * ACCESSORY) and any other transformations that may come up.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceType {
    /** Represents an device that should be excluded */
    public static final NeeoDeviceType EXCLUDE = new NeeoDeviceType("");
    /** Represents an accessory device (spelled the way NEEO spells it) */
    public static final NeeoDeviceType ACCESSOIRE = new NeeoDeviceType("ACCESSOIRE");
    /** Represents an light device */
    static final NeeoDeviceType LIGHT = new NeeoDeviceType("LIGHT");

    /** Represents the propery way to spell accessory! */
    private static final String ACCESSORY = "ACCESSORY";

    /** The text value of the device type */
    private final String text;

    /**
     * Constructs the NeeoDeviceType using the specified text
     *
     * @param text the text
     */
    private NeeoDeviceType(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = StringUtils.equalsIgnoreCase(text, ACCESSORY) ? ACCESSOIRE.text : text;
    }

    /**
     * Parses the text into a NeeoDeviceType (ignoring case)
     *
     * @param text the text to parse
     * @return the possibly null NeeoDeviceType
     */
    public static NeeoDeviceType parse(final String text) {
        if (StringUtils.isEmpty(text)) {
            return EXCLUDE;
        }

        if (StringUtils.equalsIgnoreCase(text, ACCESSOIRE.text) || StringUtils.equalsIgnoreCase(text, ACCESSORY)) {
            return ACCESSOIRE;
        }

        return new NeeoDeviceType(text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return StringUtils.equals(text, ((NeeoDeviceType) obj).text);
    }

    @Override
    public String toString() {
        return text;
    }
}
