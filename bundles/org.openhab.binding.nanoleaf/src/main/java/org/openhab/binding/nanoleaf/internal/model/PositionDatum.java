/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;

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
    @SerializedName("shapeType")
    private int shapeType;

    private static Map<Integer, Integer> panelSizes = new HashMap<Integer, Integer>();

    public PositionDatum() {
        // initialize constant sidelengths for panels. See https://forum.nanoleaf.me/docs chapter 3.3
        if (panelSizes.isEmpty()) {
            panelSizes.put(0, 150); // Triangle
            panelSizes.put(1, 0); // Rhythm N/A
            panelSizes.put(2, 100); // Square
            panelSizes.put(3, 100); // Control Square Master
            panelSizes.put(4, 100); // Control Square Passive
            panelSizes.put(7, 67); // Hexagon
            panelSizes.put(8, 134); // Triangle Shapes
            panelSizes.put(9, 67); // Mini Triangle Shapes
            panelSizes.put(12, 0); // Shapes Controller (N/A)
        }
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
        return panelSizes.getOrDefault(shapeType, 0);
    }
}
