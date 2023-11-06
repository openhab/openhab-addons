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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents rhythm module position
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class RhythmPos {

    @SerializedName("x")
    private float posX;
    @SerializedName("y")
    private float posY;
    @SerializedName("o")
    private float orientation;

    public float getPosX() {
        return posX;
    }

    public void setPosX(float x) {
        this.posX = x;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float y) {
        this.posY = y;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float o) {
        this.orientation = o;
    }
}
