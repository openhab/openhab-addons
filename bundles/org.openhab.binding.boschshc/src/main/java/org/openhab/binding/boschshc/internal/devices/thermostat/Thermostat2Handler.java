/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.thermostat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * Handler for Thermostat II devices (including Thermostat II [+M] with Matter support).
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class Thermostat2Handler extends AbstractThermostatHandler {
    public Thermostat2Handler(Thing thing) {
        super(thing);
    }
}
