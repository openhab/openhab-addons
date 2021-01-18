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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link Register} is a DTO class representing a register entry of a meter.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Register {

    // {
    // "identifier":"01",
    // "rate":"STANDARD",
    // "is_settlement_register":true
    // }

    public String identifer = OctopusEnergyBindingConstants.UNDEFINED_STRING;

    @Nullable
    public String rate;

    @Nullable
    public Boolean isSettlementRegister;
}
