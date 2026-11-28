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
 * How a bridge reaches the bus. A tunnel shows the line of its interface; a router only what the couplers let
 * onto the IP backbone.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum ConnectionKind {
    TUNNEL("tunnel"),
    SECURE_TUNNEL("secure tunnel"),
    ROUTER("router"),
    SECURE_ROUTER("secure router"),
    SERIAL("serial");

    private final String text;

    ConnectionKind(String text) {
        this.text = text;
    }

    /** {@return the words the console shows} */
    public String text() {
        return text;
    }
}
