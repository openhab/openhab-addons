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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents layout of the light panels
 *
 * @author Martin Raepple - Initial contribution
 * @author Stefan Höhn - further improvements
 */
@NonNullByDefault
public class Layout {

    private int numPanels;

    private final Logger logger = LoggerFactory.getLogger(Layout.class);

    private @Nullable List<PositionDatum> positionData = null;

    public Layout() {
    }

    public Layout(List<PositionDatum> positionData) {
        this.positionData = new ArrayList<>(positionData);
        this.numPanels = positionData.size();
    }

    public int getNumPanels() {
        return numPanels;
    }

    public void setNumPanels(int numPanels) {
        this.numPanels = numPanels;
    }

    public @Nullable List<PositionDatum> getPositionData() {
        return positionData;
    }

    public void setPositionData(List<PositionDatum> positionData) {
        this.positionData = positionData;
    }

    /**
     * Returns a text representation for a canvas layout.
     *
     * Note only canvas supported currently due to its easy geometry
     *
     * @return a String containing the layout
     */
    public String getLayoutView() {
        List<PositionDatum> localPositionData = positionData;
        if (localPositionData != null) {
            String view = "";

            int minx = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxy = Integer.MIN_VALUE;
            int sideLength = Integer.MIN_VALUE;

            final int noofDefinedPanels = localPositionData.size();

            /*
             * Since 5.0.0 sidelengths are panelspecific and not delivered per layout but only the individual panel.
             * The only approximation we can do then is to derive the max-sidelength
             * the other issue is that panel sidelength have become fix per paneltype which has to be retrieved in a
             * hardcoded way.
             */
            for (int index = 0; index < noofDefinedPanels; index++) {
                PositionDatum panel = localPositionData.get(index);
                logger.debug("Layout: Panel position data x={} y={}", panel.getPosX(), panel.getPosY());

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
                if (panel.getPanelSize() > sideLength) {
                    sideLength = panel.getPanelSize();
                }
            }

            int shiftWidth = sideLength / 2;

            if (shiftWidth == 0) {
                // seems we do not have squares here
                return "Cannot render layout. Please note that layout views are only supported for square panels.";
            }

            int lineY = maxy;
            Map<Integer, PositionDatum> map;

            while (lineY >= miny) {
                map = new TreeMap<>();
                for (int index = 0; index < noofDefinedPanels; index++) {

                    if (localPositionData != null) {
                        PositionDatum panel = localPositionData.get(index);

                        if (panel.getPosY() == lineY) {
                            map.put(panel.getPosX(), panel);
                        }
                    }
                }
                lineY -= shiftWidth;
                for (int x = minx; x <= maxx; x += shiftWidth) {
                    if (map.containsKey(x)) {
                        PositionDatum panel = map.get(x);
                        if (panel != null) {
                            int panelId = panel.getPanelId();
                            view += String.format("%5s ", panelId);
                        } else {
                            view += "      ";
                        }
                    } else {
                        view += "      ";
                    }
                }
                view += System.lineSeparator();
            }

            return view;
        } else {
            return "";
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Layout l = (Layout) o;

        if (numPanels != l.getNumPanels()) {
            return false;
        }

        List<PositionDatum> pd = getPositionData();
        List<PositionDatum> otherPd = l.getPositionData();
        if (pd == null && otherPd == null) {
            return true;
        }

        if (pd == null || otherPd == null) {
            return false;
        }

        return pd.equals(otherPd);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getNumPanels();
        List<PositionDatum> pd = getPositionData();
        if (pd != null) {
            for (PositionDatum p : pd) {
                result = prime * result + p.hashCode();
            }
        }

        return result;
    }
}
