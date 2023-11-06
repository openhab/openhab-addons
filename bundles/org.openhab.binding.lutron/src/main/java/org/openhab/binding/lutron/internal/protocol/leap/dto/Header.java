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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP message header
 *
 * @author Bob Adair - Initial contribution
 */
public class Header {
    public static final String STATUS_NO_CONTENT = "204 NoContent";
    public static final String STATUS_OK = "200 OK";

    @SerializedName("MessageBodyType")
    public String messageBodyType;
    @SerializedName("StatusCode")
    public String statusCode;
    @SerializedName("Url")
    public String url;

    public Header() {
    }
}
