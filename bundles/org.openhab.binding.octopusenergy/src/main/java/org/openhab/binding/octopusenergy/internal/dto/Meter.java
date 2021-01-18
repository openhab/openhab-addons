/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link ElectricityMeterPoint} is a DTO class representing a physical meter.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Meter {

    // {
    // "serial_number":"S81E028343",
    // "registers":[
    // ]
    // }

    public String serialNumber = OctopusEnergyBindingConstants.UNDEFINED_STRING;

    public ArrayList<Register> registers = new ArrayList<>();

    public List<Consumption> consumptionList = new ArrayList<>();
}
