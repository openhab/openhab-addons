/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

/**
 * Definition of the Homematic paramset types.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum HmParamsetType {
    VALUES,
    MASTER;

    /**
     * Parses the string and returns the HmParamsetType object.
     */
    public static HmParamsetType parse(String type) {
        if (type != null) {
            if (type.equals(VALUES.toString()) || type.equals(VALUES.getId())) {
                return VALUES;
            } else if (type.equals(MASTER.toString()) || type.equals(MASTER.getId())) {
                return MASTER;
            }
        }
        throw new RuntimeException("Unknown HmParamsetType " + type);
    }

    /**
     * Returns the first letter as id of the paramset type.
     */
    public String getId() {
        return name().substring(0, 1);
    }
}
