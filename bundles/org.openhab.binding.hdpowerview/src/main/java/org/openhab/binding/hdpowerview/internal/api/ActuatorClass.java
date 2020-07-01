/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Actuator class; all shades have a PRIMARY class actuator, plus double action
 * shades also have a SECONDARY class actuator
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ActuatorClass {
    PRIMARY_ACTUATOR,
    SECONDARY_ACTUATOR;
}
