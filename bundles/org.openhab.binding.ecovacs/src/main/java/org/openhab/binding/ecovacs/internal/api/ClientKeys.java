/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public class ClientKeys {

    private ClientKeys() {
        // Prevent instantiation
    }

    // TODO Find out where to get these keys and secret from to provide further information for OpenHab users.
    public static final String CLIENT_KEY = "1520391301804";
    public static final String CLIENT_SECRET = "6c319b2a5cd3e66e39159c2e28f2fce9";

    public static final String AUTH_CLIENT_KEY = "1520391491841";
    public static final String AUTH_CLIENT_SECRET = "77ef58ce3afbe337da74aa8c5ab963a9";
}
