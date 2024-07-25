/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Head} is responsible for storing the "Head" node of the JSON response from the
 * {@link BaseFroniusResponse}.
 *
 * The contents of the response object will vary depending on the preceding request but it always contains a common
 * response header and a request body.
 *
 * @author Thomas Rokohl - Initial contribution
 */
@NonNullByDefault
public class Head {
    @SerializedName("RequestArguments")
    private @Nullable HeadRequestArguments requestArguments;
    @SerializedName("Status")
    private @Nullable HeadStatus status;
    @SerializedName("Timestamp")
    private @Nullable String timestamp;

    public @Nullable HeadRequestArguments getRequestArguments() {
        return requestArguments;
    }

    public @Nullable HeadStatus getStatus() {
        return status;
    }

    public @Nullable String getTimestamp() {
        return timestamp;
    }
}
