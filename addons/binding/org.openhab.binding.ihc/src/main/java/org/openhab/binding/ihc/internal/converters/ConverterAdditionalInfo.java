/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import java.util.ArrayList;

import org.openhab.binding.ihc.ws.projectfile.IhcEnumValue;

/**
 * Class to hold additional information which is needed for data conversion.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ConverterAdditionalInfo {
    private ArrayList<IhcEnumValue> enumValues;
    private Boolean inverted;

    public ConverterAdditionalInfo(ArrayList<IhcEnumValue> enumValues, Boolean inverted) {
        this.enumValues = enumValues;
        this.inverted = inverted;
    }

    public ArrayList<IhcEnumValue> getEnumValues() {
        return enumValues;
    }

    public Boolean getInverted() {
        return inverted;
    }
}
