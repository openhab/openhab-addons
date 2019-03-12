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
package org.openhab.binding.ihc.internal;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;

/**
 * Class for holding enumerations from IHC / ELKO project file.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class EnumDictionary {

    private HashMap<Integer, ArrayList<IhcEnumValue>> enumDictionary = new HashMap<>();

    public EnumDictionary(HashMap<Integer, ArrayList<IhcEnumValue>> enums) {
        this.enumDictionary = enums;
    }

    /**
     * Returns all possible enumerated values for corresponding enum type.
     *
     * @param typeDefinititonId Enum type definition identifier.
     * @return list of enum values.
     */
    public ArrayList<IhcEnumValue> getEnumValues(int typeDefinititonId) {
        return enumDictionary.get(typeDefinititonId);
    }
}
