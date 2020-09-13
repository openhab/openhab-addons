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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents layout of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class Layout {

    private int numPanels;
    private int sideLength;

    private @Nullable List<PositionDatum> positionData = null;

    public int getNumPanels() {
        return numPanels;
    }

    public void setNumPanels(int numPanels) {
        this.numPanels = numPanels;
    }

    public int getSideLength() {
        return sideLength;
    }

    public void setSideLength(int sideLength) {
        this.sideLength = sideLength;
    }

    public @Nullable List<PositionDatum> getPositionData() {
        return positionData;
    }

    public void setPositionData(List<PositionDatum> positionData) {
        this.positionData = positionData;
    }

    /**
     * Returns an text representation for a canvas layout.
     *
     * Note only canvas supported currently due to its easy geometry
     * 
     * @return a String containing the layout
     */
    public String getLayoutView() {
        if (positionData != null) {
            String view = "";

            int minx = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxy = Integer.MIN_VALUE;

            final int noofDefinedPanels = positionData.size();
            for (int index = 0; index < noofDefinedPanels; index++) {
                if (positionData != null) {
                    @Nullable
                    PositionDatum panel = positionData.get(index);

                    if (panel != null) {
                        if (panel.getPosX() < minx) {
                            minx = panel.getPosX();
                        }
                        if (panel.getPosX() > maxx) {
                            maxx = panel.getPosX();
                        }
                        if (panel.getPosY() < miny) {
                            miny = panel.getPosY();
                        }
                        if (panel.getPosY() > maxy) {
                            maxy = panel.getPosY();
                        }
                    }
                }
            }

            int shiftWidth = getSideLength() / 2;

            int lineY = maxy;
            Map<Integer, PositionDatum> map;

            while (lineY >= miny) {
                map = new TreeMap<>();
                for (int index = 0; index < noofDefinedPanels; index++) {

                    if (positionData != null) {
                        @Nullable
                        PositionDatum panel = positionData.get(index);

                        if (panel != null && panel.getPosY() == lineY)
                            map.put(panel.getPosX(), panel);
                    }
                }
                lineY -= shiftWidth;
                for (int x = minx; x <= maxx; x += shiftWidth) {
                    if (map.containsKey(x)) {
                        @Nullable
                        PositionDatum panel = map.get(x);
                        view += String.format("%5s ", panel.getPanelId());
                    } else
                        view += "      ";
                }
                view += "\n";
            }

            return view;
        } else
            return "";
    }
}
