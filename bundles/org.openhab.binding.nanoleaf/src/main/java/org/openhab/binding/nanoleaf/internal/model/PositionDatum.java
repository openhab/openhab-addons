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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

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
    @SerializedName("shapeType")
    private int shapeType;

    public PositionDatum() {
    }

    public PositionDatum(int panelId, int posX, int posY, int orientation, int shapeType) {
        this.panelId = panelId;
        this.posX = posX;
        this.posY = posY;
        this.orientation = orientation;
        this.shapeType = shapeType;
    }

    public int getPanelId() {
        return panelId;
    }

    public void setPanelId(int panelId) {
        this.panelId = panelId;
    }

    public int getPosX() {
        if (getPanelSize() != 0 && posX % getPanelSize() == 99) { // hack: check the inaccuracy of 1
            posX = (posX / getPanelSize() + 1) * getPanelSize();
        }
        return posX;
    }

    public void setPosX(int x) {
        this.posX = x;
    }

    public int getPosY() {
        // we need to fix the positions: see
        // https://forum.nanoleaf.me/forum/aurora-open-api/squares-send-unprecise-layout-positions
        // unfortunately this cannot be done in the setter as gson does not access setters

        if (getPanelSize() != 0 && posY % getPanelSize() == 99) { // hack: check the inaccuracy of 1
            posY = (posY / getPanelSize() + 1) * getPanelSize();
        }
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

    public int getShapeType() {
        return shapeType;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }

    public Integer getPanelSize() {
        return (int) ShapeType.valueOf(shapeType).getSideLength();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PositionDatum pd = (PositionDatum) o;
        return (posX == pd.getPosX()) && (posY == pd.getPosY()) && (orientation == pd.getOrientation())
                && (shapeType == pd.getShapeType()) && (panelId == pd.getPanelId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + posX;
        result = prime * result + posY;
        result = prime * result + orientation;
        result = prime * result + shapeType;
        result = prime * result + panelId;
        return result;
    }
}
