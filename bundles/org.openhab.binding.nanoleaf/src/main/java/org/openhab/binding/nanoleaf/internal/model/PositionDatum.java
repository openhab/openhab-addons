/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
 * Represents panel position
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class PositionDatum {

    private int panelId;
    @SerializedName("x")
    private int posX;
    @SerializedName("y")
    private int posY;
    @SerializedName("o")
    private int orientation;

    public int getPanelId() {
        return panelId;
    }

    public void setPanelId(int panelId) {
        this.panelId = panelId;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int x) {
        this.posX = x;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int y) {
        this.posY = y;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int o) {
        this.orientation = o;
    }
}
