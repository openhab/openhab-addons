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
 * This class represents a preset broadcast station and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PresetBroadcastStation {
    /** The URI of the preset */
    private final String uri;

    /** The frequency of the preset (may not be used - part of the URI) */
    private final @Nullable String frequency;

    /**
     * Creates the preset from the uri
     * 
     * @param uri a non-null, non-empty URI
     */
    public PresetBroadcastStation(final String uri) {
        Validate.notEmpty(uri, "uri");

        this.uri = uri;
        this.frequency = null;
    }

    /**
     * Gets the URI
     * 
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the frequency
     * 
     * @return the frequency
     */
    public @Nullable String getFrequency() {
        return frequency;
    }
}
