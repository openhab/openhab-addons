/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

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
         * Unknown category
         */
        UNDEFINED
    }

    private CategoryType type;

    /**
     * Create a {@link LxCategory} object
     *
     * @param uuid
     *            UUID of this category object on the Miniserver
     * @param name
     *            name of the category
     * @param type
     *            type of the category, as retrieved from the Miniserver
     */
    public LxCategory(LxUuid uuid, String name, String type) {
        super(uuid, name);
        setType(type);
    }

    /**
     * Obtain the type of this category
     *
     * @return
     *         type of category
     */
    public CategoryType getType() {
        return type;
    }

    /**
     * Set the type of this category
     *
     * @param type
     *            new type to set as received from Miniserver
     */
    void setType(String type) {
        String tl = type.toLowerCase();
        if (tl.equals("lights")) {
            this.type = CategoryType.LIGHTS;
        } else if (tl.equals("shading")) {
            this.type = CategoryType.SHADING;
        } else {
            this.type = CategoryType.UNDEFINED;
        }
        getUuid().setUpdate(true);
    }
}
