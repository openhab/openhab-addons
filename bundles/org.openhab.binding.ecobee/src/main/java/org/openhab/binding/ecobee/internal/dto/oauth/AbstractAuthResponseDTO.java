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
package org.openhab.binding.ecobee.internal.dto.oauth;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AbstractAuthResponseDTO} represents the common fields returned in all auth responses.
 *
 * @author Mark Hilbush - Initial contribution
 */
public abstract class AbstractAuthResponseDTO {

    /*
     * Error code.
     */
    @SerializedName("error")
    public String error;

    /*
     * Textual description of error.
     */
    @SerializedName("error_description")
    public String errorDescription;

    /*
     * URI referencing OAuth documentation.
     */
    @SerializedName("error_uri")
    public String errorURI;
}
