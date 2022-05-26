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
package org.openhab.binding.evcc.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the status response (/api/state).
 * This DTO was written for evcc version 0.91.
 *
 * @author Florian Hotze - Initial contribution
 */
public class Status {

    @SerializedName("result")
    private Result result;

    public Result getResult() {
        return result;
    }
}
