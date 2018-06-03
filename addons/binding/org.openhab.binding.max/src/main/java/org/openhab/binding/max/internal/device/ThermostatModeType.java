/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 * This enumeration represents the different mode types of a MAX! heating thermostat.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - OH2 update
 * @since 1.4.0
 */
public enum ThermostatModeType implements PrimitiveType, State, Command {
    AUTOMATIC,
    MANUAL,
    VACATION,
    BOOST;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toFullString() {
        return toString();
    }
}
