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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the current text form and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TextFormResult {
    /** The current text */
    private @Nullable String text;

    /**
     * Constructor used for deserialization only
     */
    public TextFormResult() {
    }

    /**
     * Gets the current text
     *
     * @return the current text
     */
    public @Nullable String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextForm [text=" + text + "]";
    }
}
