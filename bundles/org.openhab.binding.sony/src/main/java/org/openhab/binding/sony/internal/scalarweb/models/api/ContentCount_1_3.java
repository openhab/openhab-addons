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
 * This class represents a content count request and is used for serialization only
 *
 * Versions:
 * <ol>
 * <li>1.3: {"uri":"string", "type":"string*", "target":"string", "view":"string", "path":"string"}</li>
 * </ol>
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentCount_1_3 {

    /** The uri */
    private final String uri;

    /**
     * Instantiates a new content count for the uri
     *
     * @param uri the non-null, non-empty uri
     */
    public ContentCount_1_3(final String uri) {
        Validate.notEmpty(uri, "uri cannot be empty");
        this.uri = uri;
    }

    /**
     * Gets the uri
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ContentCount_1_2 [uri=" + uri + "]";
    }
}
