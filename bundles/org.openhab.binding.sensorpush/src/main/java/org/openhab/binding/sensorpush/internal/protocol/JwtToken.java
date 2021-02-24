/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * JWT Token JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class JwtToken {

    /** Token type */
    public @Nullable String type;

    /** Issuer */
    public @Nullable String iss;

    /** Subject */
    public @Nullable String sub;

    /** Issued at (seconds since epoch) */
    public @Nullable Long iat;

    /** Expires (seconds since epoch) */
    public @NonNullByDefault({}) Long exp;

    public JwtToken() {
    }
}
