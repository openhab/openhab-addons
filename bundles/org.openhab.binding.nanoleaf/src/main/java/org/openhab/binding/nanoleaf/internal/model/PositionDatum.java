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
 * Represents panel position
 *
 * @author Martin Raepple - Initial contribution
 */
public class PositionDatum {

    private Integer panelId;
    @SerializedName("x")
    private Integer posX;
    @SerializedName("y")
    private Integer posY;
    @SerializedName("o")
    private Integer orientation;

    public Integer getPanelId() {
        return panelId;
    }

    public void setPanelId(Integer panelId) {
        this.panelId = panelId;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer x) {
        this.posX = x;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer y) {
        this.posY = y;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer o) {
        this.orientation = o;
    }

}
