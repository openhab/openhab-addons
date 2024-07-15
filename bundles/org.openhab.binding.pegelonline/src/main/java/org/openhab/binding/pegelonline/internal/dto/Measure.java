/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pegelonline.internal.dto;

/**
 * {@link Measure} DTO for water level measurements
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Measure {
    public String timestamp; // "2021-07-31T19:00:00+02:00",
    public double value; // ":238.0,
    public int trend; // -1,
    public String stateMnwMhw; // "normal",
    public String stateNswHsw; // "unknown"
}
