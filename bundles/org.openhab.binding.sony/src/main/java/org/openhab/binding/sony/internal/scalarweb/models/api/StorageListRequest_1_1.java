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

/**
 * The storage list request class used for serialization only.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class StorageListRequest_1_1 {

    /** The storage list items */
    private final String uri;

    /**
     * Constructs the request with no URI
     */
    public StorageListRequest_1_1() {
        this("");
    }

    /**
     * Constructs the request with the URI
     * 
     * @param uri a possibly null, possibly empty uri
     */
    public StorageListRequest_1_1(final String uri) {
        this.uri = StringUtils.defaultIfEmpty(uri, "");
    }

    /**
     * Get's the URI
     * 
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "StorageListRequest_1_1 [uri=" + uri + "]";
    }
}
