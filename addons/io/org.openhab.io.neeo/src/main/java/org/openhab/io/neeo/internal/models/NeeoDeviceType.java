/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

/**
 * Defines a NEEO device type. There are many unknown and not working device types that NEEO supports and has never
 * published. This helper class will simply identify two common types - an EXCLUDE (which excludes the associated thing
 * from NEEO) and the ACCESSOIRE (which is the generic device type supported by NEEO). All other device types may be
 * entered by the user and will be passed directly to the NEEO brain (regardless if it's valid or not).
 *
 * Note: this class will also be used for backward compatibility at some point (when they change ACCESSOIRE to
 * ACCESSORY) and any other transformations that may come up.
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceType {
    public static final NeeoDeviceType EXCLUDE = new NeeoDeviceType("");
    public static final NeeoDeviceType ACCESSOIRE = new NeeoDeviceType("ACCESSOIRE");
    static final NeeoDeviceType LIGHT = new NeeoDeviceType("LIGHT");

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NeeoDeviceType other = (NeeoDeviceType) obj;
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return text;
    }
}
