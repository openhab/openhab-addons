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
package org.openhab.binding.mercedesme.internal.dto;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;

/**
 * The {@link TokenResponse} dto contains JSon body of token response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TokenResponse {
    public String access_token = Constants.NOT_SET;
    public String refresh_token = Constants.NOT_SET;
    public String token_type = Constants.NOT_SET;
    public int expires_in;
    public Instant created = Instant.now();
}
