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
 * This class represents the text form request and is used for serialization only. Note that this class is currently
 * broken as the encryption doesn't seem to work (see
 * https://pro-bravia.sony.net/develop/integrate/rest-api/doc/Data-Encryption_401146660/index.html)
 *
 * Versions:
 * <ol>
 * <li>1.0: string</li>
 * <li>1.1: {"encKey":"string", "text":"string"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TextFormRequest_1_1 {
    /** The encryption key */
    private final String encKey;

    /** The text to send */
    private final @Nullable String text;

    /**
     * Instantiates a new text form request.
     *
     * @param encKey the non-null, non-empty encryption key
     * @param text the possibly null, possibly empty text
     */
    public TextFormRequest_1_1(final String encKey, final @Nullable String text) {
        Validate.notEmpty(encKey, "encKey cannot be empty");
        this.encKey = encKey;
        this.text = text;
    }

    /**
     * Gets the encryption key
     *
     * @return the encryption key
     */
    public String getEncKey() {
        return encKey;
    }

    /**
     * Gets the text to send
     *
     * @return the text to send
     */
    public @Nullable String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextFormRequest_1_1 [encKey=" + encKey + ", text=" + text + "]";
    }
}
