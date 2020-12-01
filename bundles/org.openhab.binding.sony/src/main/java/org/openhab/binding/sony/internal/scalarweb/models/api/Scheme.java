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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the scheme and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Scheme {

    /** Various scheme identifiers */
    public static final String EXT_OUTPUT = "extOutput";
    public static final String EXT_INPUT = "extInput";
    public static final String RADIO = "radio";
    public static final String TV = "tv";
    public static final String STORAGE = "storage";

    /** The scheme identifier */
    private @Nullable String scheme;

    /**
     * Constructor used for deserialization only
     */
    public Scheme() {
    }

    /**
     * Gets the scheme identifier
     *
     * @return the scheme identifier
     */
    public @Nullable String getScheme() {
        return scheme;
    }

    @Override
    public int hashCode() {
        final String localScheme = scheme;
        return ((localScheme == null) ? 0 : localScheme.hashCode());
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return StringUtils.equals(scheme, ((Scheme) obj).scheme);
    }

    @Override
    public String toString() {
        return "Scheme [scheme=" + scheme + "]";
    }
}
