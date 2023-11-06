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
package org.openhab.binding.ihc.internal.converters;

import java.util.List;
import java.util.Map;

import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;
import org.openhab.core.types.Command;

/**
 * Class to hold additional information which is needed for data conversion.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ConverterAdditionalInfo {
    private List<IhcEnumValue> enumValues;
    private Boolean inverted;
    private Map<Command, Object> commandLevels;

    public ConverterAdditionalInfo(List<IhcEnumValue> enumValues, Boolean inverted,
            Map<Command, Object> commandLevels) {
        this.enumValues = enumValues;
        this.inverted = inverted;
        this.commandLevels = commandLevels;
    }

    public List<IhcEnumValue> getEnumValues() {
        return enumValues;
    }

    public Boolean getInverted() {
        return inverted;
    }

    public Map<Command, Object> getCommandLevels() {
        return commandLevels;
    }
}
