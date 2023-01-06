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
package org.openhab.binding.max.internal.device;

import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

/**
 * This enumeration represents the different mode types of a MAX! heating thermostat.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 * @author Marcel Verpaalen - OH2 update
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
