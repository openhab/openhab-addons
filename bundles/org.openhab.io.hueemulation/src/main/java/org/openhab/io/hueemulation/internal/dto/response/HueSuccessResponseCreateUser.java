/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This object describes the right hand side of "success".
 * The response looks like this:
 *
 * <pre>
 * {
 *   "success":{
 *      "username": "-the-username-"
 *   }
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueSuccessResponseCreateUser extends HueSuccessResponse {
    public String username;
    // For DTLS setup of the new hue entertain DTLS/UDP protocol
    // The PSK identity matches the “username”, and the PSK key matches the “clientkey”.
    public String clientkey;

    public HueSuccessResponseCreateUser(String username, String clientkey) {
        this.username = username;
        this.clientkey = clientkey;
    }
}
