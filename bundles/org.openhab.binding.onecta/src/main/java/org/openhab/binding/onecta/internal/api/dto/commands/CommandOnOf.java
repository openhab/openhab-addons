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
package org.openhab.binding.onecta.internal.api.dto.commands;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class CommandOnOf {
    @SerializedName("value")
    public String value;

    @SerializedName("path")
    public String path;

    public CommandOnOf(Enums.OnOff value) {
        this.value = value.getValue();
    }

    public CommandOnOf(Enums.OnOff value, String path) {
        this.value = value.getValue();
        this.path = path;
    }
}
