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
package org.openhab.binding.mybmw.internal.dto.properties;

/**
 * The {@link TireStatus} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class TireStatus {
    public double currentPressure;// ": 220,
    public String localizedCurrentPressure;// ": "2.2 bar",
    public String localizedTargetPressure;// ": "2.3 bar",
    public double targetPressure;// ": 230
}
