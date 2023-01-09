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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public abstract class AbstractPortalResponse {

    @SerializedName("todo")
    private final String todo;

    @SerializedName("result")
    private final String result;

    protected AbstractPortalResponse(String todo, String result) {
        this.todo = todo;
        this.result = result;
    }

    public boolean wasSuccessful() {
        return "ok".equals(result);
    }
}
