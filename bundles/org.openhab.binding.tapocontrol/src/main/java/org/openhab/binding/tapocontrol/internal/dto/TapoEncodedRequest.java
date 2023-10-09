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
package org.openhab.binding.tapocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * {@TapoEncodedRequest} holds encoded data sent to device
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoEncodedRequest(@Expose String method, @Expose Object params, @Expose long requestTimeMils) {

    /**
     * Create request with command (method) and data (params) sent to device
     */
    public TapoEncodedRequest(Object params) {
        this("securePassthrough", "{request:'" + params.toString() + "'}", System.currentTimeMillis());
    }
}
