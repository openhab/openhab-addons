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
package org.openhab.binding.squeezebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StatusResponseDTO} is the response received from a player status request.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class StatusResponseDTO {

    /**
     * Id. Currently unused.
     */
    @SerializedName("id")
    public String id;

    /**
     * Method name. Normally "slim.request"
     */
    @SerializedName("method")
    public String method;

    /**
     * Parameters passed in the query. Currently unused.
     */
    @SerializedName("params")
    public Object params;

    /**
     * Contains the result of the query
     */
    @SerializedName("result")
    public StatusResultDTO result;
}
