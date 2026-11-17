/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.calimero.secure.KnxSecureException;

/**
 * A mistake in the KNX Secure settings the user can fix, with a text for the bridge status.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class SecureSetupException extends KnxSecureException {
    private static final long serialVersionUID = 1L;

    private final String textKey;

    /**
     * @param textKey key of the text in the add-on's translations
     * @param message the same in English, for logs and for status text that cannot be translated
     */
    public SecureSetupException(String textKey, String message) {
        super(message);
        this.textKey = textKey;
    }

    /** {@return the text for the thing status, translated by openHAB when it is shown} */
    public String statusText() {
        return "@text/" + textKey;
    }
}
