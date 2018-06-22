/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhab.binding.ihc.ws.projectfile.IhcEnumValue;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class EnumDictionary {

    private HashMap<Integer, ArrayList<IhcEnumValue>> enumDictionary = new HashMap<Integer, ArrayList<IhcEnumValue>>();

    public EnumDictionary(HashMap<Integer, ArrayList<IhcEnumValue>> enums) {
        this.enumDictionary = enums;
    }

    /**
     * Returns all possible enumerated values for corresponding enum type.
     *
     * @param typedefId
     *            Enum type definition identifier.
     * @return list of enum values.
     */
    public ArrayList<IhcEnumValue> getEnumValues(int typedefId) {
        return enumDictionary.get(typedefId);
    }
}
