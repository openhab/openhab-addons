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
package org.openhab.binding.freeboxos.internal.api.home;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link HomeNodeEndpointState} is a Java class used to map the
 * structure used by the home API
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class HomeNodeEndpointState {
    public static class HomeNodeEndpointStateResponse extends Response<HomeNodeEndpointState> {
    }

    public static enum ValueType {
        @SerializedName("bool")
        BOOL,
        @SerializedName("int")
        INT,
        @SerializedName("float")
        FLOAT,
        @SerializedName("void")
        VOID,
        @SerializedName("string")
        STRING;
    }

    private @Nullable JsonElement value;
    private ValueType valueType = ValueType.VOID;
    private long refresh;

    public @Nullable JsonElement getValue() {
        return value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public long getRefresh() {
        return refresh;
    }

    public @Nullable Boolean asBoolean() {
        final JsonElement theValue = value;
        if (theValue != null && theValue.isJsonPrimitive() && theValue.getAsJsonPrimitive().isBoolean()) {
            return theValue.getAsBoolean();
        }
        return null;
    }

    public @Nullable Integer asInt() {
        final JsonElement theValue = value;
        if (theValue != null && theValue.isJsonPrimitive() && theValue.getAsJsonPrimitive().isNumber()) {
            return theValue.getAsInt();
        }
        return null;
    }
}
