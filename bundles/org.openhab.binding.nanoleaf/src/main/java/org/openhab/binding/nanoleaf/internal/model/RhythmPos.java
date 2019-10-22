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
package org.openhab.binding.nanoleaf.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents rhythm module position
 *
 * @author Martin Raepple - Initial contribution
 */
public class RhythmPos {

    @SerializedName("x")
    private Float posX;
    @SerializedName("y")
    private Float posY;
    @SerializedName("o")
    private Float orientation;

    public Float getPosX() {
        return posX;
    }

    public void setPosX(Float x) {
        this.posX = x;
    }

    public Float getPosY() {
        return posY;
    }

    public void setPosY(Float y) {
        this.posY = y;
    }

    public Float getOrientation() {
        return orientation;
    }

    public void setOrientation(Float o) {
        this.orientation = o;
    }

}
