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
public class StorageListRequest_1_2 extends StorageListRequest_1_1 {
    /** The is registered parm */
    private final String isRegistered;

    /**
     * Constructs the request with no URI
     */
    public StorageListRequest_1_2() {
        this("", "");
    }

    /**
     * Constructs the request with the URI and isRegistered
     * 
     * @param uri a possibly null, possibly empty uri
     * @param isRegistered a possibly null, possibly empty is registered parm
     */
    public StorageListRequest_1_2(final String uri, final String isRegistered) {
        super(uri);
        this.isRegistered = StringUtils.defaultIfEmpty(isRegistered, "");
    }

    @Override
    public String toString() {
        return "StorageListRequest_1_2 [uri=" + getUri() + ", isRegistered=" + isRegistered + "]";
    }
}
