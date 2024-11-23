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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ambilight;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightTopologyDTO} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/topology endpoint to retrieve the ambilight topology information.
 * <p>
 * Endpoint returns:
 * <p>
 * layers (integer): The number of layers.
 * <p>
 * left (integer): The number of pixels on the left.
 * <p>
 * top (integer): The number of pixels on the top.
 * <p>
 * right (integer): The number of pixels on the right.
 * <p>
 * bottom (integer): The number of pixels on the bottom.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class AmbilightTopologyDTO {

    @JsonProperty("top")
    private int top;

    @JsonProperty("left")
    private int left;

    @JsonProperty("bottom")
    private int bottom;

    @JsonProperty("layers")
    private int layers;

    @JsonProperty("right")
    private int right;

    public AmbilightTopologyDTO() {
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public void setLayers(int layers) {
        this.layers = layers;
    }

    public int getLayers() {
        return layers;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    @JsonIgnore
    public int getPixelSizeForGivenSide(String side) {
        int value;
        switch (side) {
            case "left":
                value = left;
                break;
            case "right":
                value = right;
                break;
            case "top":
                value = top;
                break;
            case "bottom":
                value = bottom;
                break;
            default:
                throw new IllegalStateException("Unexpected side: " + side);
        }
        return value;
    }

    @Override
    public String toString() {
        return "AmbilightTopologyDTO{" + "top = '" + top + '\'' + ",left = '" + left + '\'' + ",bottom = '" + bottom
                + '\'' + ",layers = '" + layers + '\'' + ",right = '" + right + '\'' + "}";
    }
}
