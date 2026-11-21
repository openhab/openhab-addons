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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The KNX Data Secure state of a telegram.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum SecurityState {
    PLAIN("plain"),
    SECURED("secured"),
    NOT_DECRYPTED("not decrypted");

    private final String text;

    SecurityState(String text) {
        this.text = text;
    }

    /** {@return the words the console shows} */
    public String text() {
        return text;
    }
}
