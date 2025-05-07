/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.lambda.internal.dto;

/**
 * Dto class for the HeatingCircuit Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
public class HeatingCircuitBlock {

    public int heating_circuitErrorNumber;
    public int heating_circuitOperatingState;
    public int heating_circuitFlowLineTemperature;
    public int heating_circuitReturnLineTemperature;
    public int heating_circuitRoomDeviceTemperature;
    public int heating_circuitSetpointFlowLineTemperature;
    public int heating_circuitOperatingMode;
}
