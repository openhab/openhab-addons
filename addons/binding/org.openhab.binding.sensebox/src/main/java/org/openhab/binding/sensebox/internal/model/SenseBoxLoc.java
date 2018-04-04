/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.internal.model;

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
