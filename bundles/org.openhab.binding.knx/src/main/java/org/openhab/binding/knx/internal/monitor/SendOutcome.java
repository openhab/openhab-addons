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
 * What became of a telegram openHAB sent.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum SendOutcome {
    /** The library call has not ended yet; only a read request is recorded that early. */
    PENDING("pending"),
    /** The library call ended without an error; on a tunnel this means the interface confirmed it. */
    SENT("sent"),
    /** The library gave up waiting for the confirmation of the interface. */
    NOT_CONFIRMED("not confirmed"),
    /** A read request went out and no answer came within the response timeout. */
    NO_ANSWER("no answer"),
    /** Any other error. */
    FAILED("failed");

    private final String text;

    SendOutcome(String text) {
        this.text = text;
    }

    /** {@return the words the console shows} */
    public String text() {
        return text;
    }
}
