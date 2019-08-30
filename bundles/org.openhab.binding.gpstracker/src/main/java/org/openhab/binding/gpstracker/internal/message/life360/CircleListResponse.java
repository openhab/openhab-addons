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
package org.openhab.binding.gpstracker.internal.message.life360;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The {@link CircleListResponse} is a Life360 message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class CircleListResponse {

    @SerializedName("circles")
    private List<CirclesItem> circles;

    public void setCircles(List<CirclesItem> circles) {
        this.circles = circles;
    }

    public List<CirclesItem> getCircles() {
        return circles;
    }

    @Override
    public String toString() {
        return
                "CircleListResponse{" +
                        "circles = '" + circles + '\'' +
                        "}";
    }
}
