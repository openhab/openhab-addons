/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openthermgateway.internal;

/**
 * The {@link CodeType} field is not part of OpenTherm specification, but added by OpenTherm Gateway.
 * It can be any of the following:
 *
 * T: Message received from the thermostat
 * B: Message received from the boiler
 * R: Request sent to the boiler
 * A: Response returned to the thermostat
 * E: Parity or stop bit error
 *
 * @author Arjen Korevaar - Initial contribution
 * @author James Melville - Introduced code filtering functionality
 */
public enum CodeType {
    /**
     * Message received from the thermostat
     */
    T,
    /**
     * Message received from the boiler
     */
    B,
    /**
     * Request sent to the boiler from OTGW, only present if value modified by OTGW
     */
    R,
    /**
     * Response returned to the thermostat from OTGW, only present if value modified by OTGW
     */
    A,
    /**
     * Parity or stop bit error
     */
    E
}
