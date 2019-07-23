/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BsbLanApiParameterSetRequest} reflects the request sent
 * when setting a parameter.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameterSetRequest {

    public enum Type {
        @SerializedName("0")
        SET(0),
        @SerializedName("1")
        INF(1);

        private final int value;
        Type(int value)
        {
            this.value = value;
        }
    
        public int getValue()
        {
            return value;
        }
    }

    @SerializedName("Parameter")
    private int parameter;

    @SerializedName("Value")
    private String value;

    @SerializedName("Type")
    private Type type;

    public int getParameter() {
        return parameter;
    }

    public void setParameter(int value) {
        parameter = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        type = value;
    }
}
