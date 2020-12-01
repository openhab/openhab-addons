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
 * <li>1.0: {"source":"string", "type":"string"}</li>
 * <li>1.1: {"source":"string", "type":"string", "target":"string"}</li>
 * <li>1.2: unknown (may have switched to uri like in 1.3 - waiting for example)</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentCount_1_0 {

    /** The source */
    private final String source;

    /**
     * Instantiates a new content count for the source
     *
     * @param source the non-null, non-empty source
     */
    public ContentCount_1_0(final String source) {
        Validate.notEmpty(source, "source cannot be empty");
        this.source = source;
    }

    /**
     * Gets the source
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "ContentCount_1_0 [source=" + source + "]";
    }
}
