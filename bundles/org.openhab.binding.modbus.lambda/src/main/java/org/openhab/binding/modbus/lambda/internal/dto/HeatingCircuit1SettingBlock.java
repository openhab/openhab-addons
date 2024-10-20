package org.openhab.binding.modbus.lambda.internal.dto;

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

/**
 * Dto class for the HeatingCircuit1Setting Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */

public class HeatingCircuit1SettingBlock {
    public int heatingcircuit1OffsetFlowLineTemperature;
    public int heatingcircuit1RoomHeatingTemperature;
    public int heatingcircuit1RoomCoolingTemperature;
}
