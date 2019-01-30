/**
 * Copyright (c) 2010-2018 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.converters;

import java.util.ArrayList;

import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;

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
