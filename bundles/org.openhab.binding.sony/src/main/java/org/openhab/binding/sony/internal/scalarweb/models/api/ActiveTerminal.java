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

/**
 * Sets the active terminal (the power status of a zone) and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ActiveTerminal {
    /** The active status value */
    public static final String ACTIVE = "active";

    /** The inactive status value */
    public static final String INACTIVE = "inactive";

    /** The URI describing the terminal */
    private final String uri;

    /** The status of the terminal */
    private final String active;

    /**
     * Constructs the active terminal
     * 
     * @param uri the non-null, non-empty terminal URI
     * @param active the non-null, non-empty active sttus
     */
    public ActiveTerminal(final String uri, final String active) {
        Validate.notEmpty(uri, "uri cannot be empty");
        Validate.notEmpty(active, "active cannot be empty");
        this.uri = uri;
        this.active = active;
    }

    /**
     * Get's the terminal URI
     * 
     * @return a non-null, non-empty URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get's the terminal active status
     * 
     * @return a non-null, non-empty active status
     */
    public String getActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ActiveTerminal [uri=" + uri + ", active=" + active + "]";
    }
}
