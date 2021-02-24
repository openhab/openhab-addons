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

/**
 * Access Token Request JSON object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class AccessTokenRequest extends Response {

    public String authorization;

    /** Construct an AccessTokenRequest using the provided authorization token */
    public AccessTokenRequest(String authToken) {
        authorization = authToken;
    }
}
