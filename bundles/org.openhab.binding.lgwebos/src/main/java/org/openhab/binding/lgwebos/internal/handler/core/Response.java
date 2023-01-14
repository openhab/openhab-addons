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
package org.openhab.binding.lgwebos.internal.handler.core;

import com.google.gson.JsonElement;

/**
 * {@link Response} is a value object for a response message from WebOSTV.
 *
 * @author Sebastian Prehn - Initial contribution
 */
public class Response {
    /** Required response type */
    private String type;
    /** Optional payload */
    private JsonElement payload;
    /**
     * Message ID to which this is a response to.
     * This is optional.
     */
    private Integer id;

    public Response() {
        // no-argument constructor for gson
    }

    /** Optional error message. */
    private String error;

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }

    public JsonElement getPayload() {
        return payload;
    }
}
