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
package org.openhab.binding.smhi.internal.model;

/**
 * The {@link smhiGeometry} is the Java class used to map the JSON response to an SMHI
 * request.
 *
 * @author Michael Parment - Initial contribution
 */

public class SmhiGeometry {
    private String type;

    private String[][] coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String[][] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "ClassPojo [type = " + type + ", coordinates = " + coordinates + "]";
    }
}
