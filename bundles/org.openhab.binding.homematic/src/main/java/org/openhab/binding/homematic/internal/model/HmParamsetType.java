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
