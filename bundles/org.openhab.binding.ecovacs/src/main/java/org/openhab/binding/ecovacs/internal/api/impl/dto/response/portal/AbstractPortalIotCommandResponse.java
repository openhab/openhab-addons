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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class AbstractPortalIotCommandResponse {
    @SerializedName("id")
    private final String id;

    @SerializedName("ret")
    private final String result;

    @SerializedName("debug")
    private final String failureMessage;

    public AbstractPortalIotCommandResponse(String id, String result, String failureMessage) {
        this.id = id;
        this.result = result;
        this.failureMessage = failureMessage;
    }

    public boolean wasSuccessful() {
        return "ok".equals(result);
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
