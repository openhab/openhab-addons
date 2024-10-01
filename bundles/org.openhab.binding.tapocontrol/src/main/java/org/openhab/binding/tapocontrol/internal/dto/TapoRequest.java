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
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * Holds data sent to device
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoRequest(@Expose String method, @Expose @Nullable Object params,
        @Expose long requestTimeMils) implements TapoBaseRequestInterface {

    /**
     * Create request with command (method) and data (params) sent to device
     */
    public TapoRequest(String method, @Nullable Object params) {
        this(method, params, System.currentTimeMillis());
    }

    /**
     * Create request with command (method) sent to device
     */
    public TapoRequest(String method) {
        this(method, null, System.currentTimeMillis());
    }

    /***********************************************
     * RETURN VALUES
     **********************************************/

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
