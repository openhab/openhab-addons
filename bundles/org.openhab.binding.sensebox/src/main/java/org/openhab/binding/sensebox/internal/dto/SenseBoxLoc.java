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
package org.openhab.binding.sensebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseBoxLoc} holds a de-serialized representation
 * of the API response and the data therein...
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxLoc {

    @SerializedName("geometry")
    private SenseBoxGeometry geometry;

    public SenseBoxGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(SenseBoxGeometry geometry) {
        this.geometry = geometry;
    }
}
