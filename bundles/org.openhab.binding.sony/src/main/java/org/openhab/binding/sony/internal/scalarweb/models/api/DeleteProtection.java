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
 * This class represents the request to delete protection and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DeleteProtection {

    /** The uri that identifies the resource to protect */
    private final String uri;

    /** True if should be protected */
    private final boolean isProtected;

    /**
     * Instantiates a new delete protection request
     *
     * @param uri the non-null, non-empty uri identifying the result
     * @param isProtected whether it should be protected
     */
    public DeleteProtection(final String uri, final boolean isProtected) {
        Validate.notEmpty(uri, "uri cannot be empty");
        this.uri = uri;
        this.isProtected = isProtected;
    }

    @Override
    public String toString() {
        return "DeleteProtection [uri=" + uri + ", isProtected=" + isProtected + "]";
    }
}
