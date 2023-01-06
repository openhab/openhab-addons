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
package org.openhab.binding.ventaair.internal.message.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for messages
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class Message {

    @SerializedName(value = "Header")
    protected Header header;

    public Message(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }
}
