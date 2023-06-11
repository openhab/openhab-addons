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
package org.openhab.binding.loxone.internal.types;

import org.openhab.binding.loxone.internal.controls.LxControl;

/**
 * Category of Loxone Miniserver's {@link LxControl} object.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxCategory extends LxContainer {

    /**
     * Various categories that Loxone Miniserver's control can belong to.
     *
     * @author Pawel Pieczul - initial contribution
     */
    public enum CategoryType {
        /**
         * Category for lights
         */
        LIGHTS,
        /**
         * Category for shading / rollershutter / blinds
         */
        SHADING,
        /**
         * Category for temperatures
         */
        TEMPERATURE,
        /**
         * Unknown category
         */
        UNDEFINED
    }

    private String type; // deserialized from JSON
    private CategoryType catType;

    /**
     * Obtain the type of this category
     *
     * @return type of category
     */
    public CategoryType getType() {
        if (catType == null && type != null) {
            String tl = type.toLowerCase();
            if ("lights".equals(tl)) {
                catType = CategoryType.LIGHTS;
            } else if ("shading".equals(tl)) {
                catType = CategoryType.SHADING;
            } else if ("indoortemperature".equals(tl)) {
                catType = CategoryType.TEMPERATURE;
            } else {
                catType = CategoryType.UNDEFINED;
            }
        }
        return catType;
    }
}
