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
 * This class represents the request to delete content and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DeleteContent {

    /** The uri that identifies the content to delete */
    private final String uri;

    /**
     * Instantiates a new delete content
     *
     * @param uri the non-null, non-empty uri
     */
    public DeleteContent(final String uri) {
        Validate.notEmpty(uri, "Uri cannot be empty");
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "DeleteContent [uri=" + uri + "]";
    }
}
