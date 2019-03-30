/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.protocol;

/**
 * Type of command in the Lutron integration protocol.
 *
 * @author Allan Tong - Initial contribution
 *
 */
public enum LutronCommandType {
    AREA,
    DEVICE,
    GROUP,
    MODE,
    MONITORING,
    OUTPUT,
    SYSTEM,
    TIMECLOCK,
}
