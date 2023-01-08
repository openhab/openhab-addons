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
package org.openhab.binding.bsblan.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BsbLanApiParameterSetRequestDTO} reflects the request sent
 * when setting a parameter.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameterSetRequestDTO implements BsbLanApiContentDTO {

    public enum Type {
        @SerializedName("0")
        INF("INF"),
        @SerializedName("1")
        SET("SET");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type getTypeWithFallback(String value) {
            if (value != null) {
                for (Type t : Type.values()) {
                    if (t.value.toLowerCase().equals(value.toLowerCase())) {
                        return t;
                    }
                }
            }
            // fallback to SET
            return Type.SET;
        }
    }

    // Although specifying the parameter as int (which would be nicer) also seems to work,
    // we use a String here as this is the way it is noted in the documentation.
    @SerializedName("Parameter")
    public String parameter;

    @SerializedName("Value")
    public String value;

    @SerializedName("Type")
    public Type type;
}
