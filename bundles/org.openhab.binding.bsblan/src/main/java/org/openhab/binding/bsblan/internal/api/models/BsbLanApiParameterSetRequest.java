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
        INF(0),
        @SerializedName("1")
        SET(1);

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

    // Although setting specifying the parameter as int also seems to work,
    // we use a String here as this is the it is noted in the documentation.
    @SerializedName("Parameter")
    public String parameter;

    @SerializedName("Value")
    public String value;

    @SerializedName("Type")
    public Type type;
}
