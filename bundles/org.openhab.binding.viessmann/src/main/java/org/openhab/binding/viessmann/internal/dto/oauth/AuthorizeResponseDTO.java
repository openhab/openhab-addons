/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.dto.oauth;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthorizeResponseDTO} provides the authorize response
 *
 * @author Ronny Grun - Initial contribution
 */
public class AuthorizeResponseDTO extends AbstractAuthResponseDTO {

    /*
     * The error message when authorization fails.
     */
    public String errorMsg;

    /*
     * The authorization token needed to request the access and refresh tokens.
     */
    public String code;

    /*
     * The requested Scope from the original request. This must match the original request.
     */
    public String scope;

    /*
     * The number of minutes until the PIN expires. Ensure you inform the user how much time they have.
     */
    @SerializedName("expires_in")
    public Integer expiresIn;

    /*
     * The minimum amount of seconds which must pass between polling attempts for a token.
     */
    public Integer interval;
}
