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
package org.openhab.binding.sensebox.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseBoxGeometry} holds a de-serialized representation
 * of the API response and the data therein...
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxGeometry {

    @SerializedName("coordinates")
    private List<Double> data;

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }
}
