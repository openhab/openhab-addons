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
package org.openhab.binding.miio.internal.basic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping actions conditions
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoDeviceActionCondition {

    @SerializedName("name")
    @Expose
    private @Nullable String name;
    @SerializedName("parameters")
    @Expose
    private @Nullable JsonElement parameters;

    public String getName() {
        final @Nullable String command = this.name;
        return command != null ? command : "";
    }

    public void setName(String command) {
        this.name = command;
    }

    public JsonElement getParameters() {
        final JsonElement parameter = this.parameters;
        return parameter != null ? parameter : JsonNull.INSTANCE;
    }

    public void setParameters(JsonArray parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "MiIoDeviceActionCondition [condition=" + name + ",parameters=" + getParameters().toString() + "]";
    }
}
