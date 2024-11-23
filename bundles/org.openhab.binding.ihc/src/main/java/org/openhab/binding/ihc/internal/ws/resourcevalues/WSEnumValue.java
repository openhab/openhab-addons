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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSEnumValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSEnumValue extends WSResourceValue {

    public final int definitionTypeID;
    public final int enumValueID;
    public final String enumName;

    public WSEnumValue(int resourceID, int definitionTypeID, int enumValueID, String enumName) {
        super(resourceID);
        this.definitionTypeID = definitionTypeID;
        this.enumValueID = enumValueID;
        this.enumName = enumName;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, definitionTypeID=%d, enumValueID=%d, enumName=%s]", super.resourceID,
                definitionTypeID, enumValueID, enumName);
    }
}
