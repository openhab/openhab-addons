/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

import java.util.Arrays;

/**
 *
 * Indicates type of arm/disarm message returned for PRT3 module
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public enum ArmDisarmType {
    ARM("AA"),
    QUICK_ARM("AQ"),
    DISARM("AD"),
    UNKNOWN("");

    private String indicator;

    ArmDisarmType(String indicator) {
        this.indicator = indicator;
    }

    public static ArmDisarmType fromMessage(String indicator) {
        return Arrays.stream(ArmDisarmType.values()).filter(type -> type.indicator.equals(indicator)).findFirst()
                .orElse(UNKNOWN);
    }

}
