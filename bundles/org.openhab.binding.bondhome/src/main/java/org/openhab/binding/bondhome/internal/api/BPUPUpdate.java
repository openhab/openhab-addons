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
package org.openhab.binding.bondhome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents update datagram sent by the Bond Push UDP Protocol
 *
 * The incoming JSON looks like this:
 *
 * {"B": "ZZBL12345", "t": "devices/aabbccdd/state", "i": "00112233bbeeeeff", "s" :200, "m": 0, "f": 255, "b": {"_":
 * "ab9284ef", "power": 1, "speed": 2}}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BPUPUpdate {
    // The Bond ID
    @SerializedName("B")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String bondId;
    // The topic (the path from HTTP URL)
    @SerializedName("t")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String topic;
    // The request ID
    @SerializedName("i")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String requestId;
    // The HTTP status code
    @SerializedName("s")
    @Expose(serialize = true, deserialize = true)
    public int statusCode;
    // HTTP method (0=GET, 1=POST, 2=PUT, 3=DELETE, 4=PATCH)
    @SerializedName("m")
    @Expose(serialize = true, deserialize = true)
    public int method;
    // flags (Olibra-internal use)
    @SerializedName("f")
    @Expose(serialize = true, deserialize = true)
    public int flag;
    // HTTP response body
    @SerializedName("b")
    @Expose(serialize = true, deserialize = true)
    public @Nullable BondDeviceState deviceState;
}
