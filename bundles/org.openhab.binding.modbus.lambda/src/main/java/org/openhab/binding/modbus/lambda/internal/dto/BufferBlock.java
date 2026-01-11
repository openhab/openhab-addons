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
package org.openhab.binding.modbus.lambda.internal.dto;

/**
 * Dto class for the Buffer Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
public class BufferBlock {
    public int bufferErrorNumber;
    public int bufferOperatingState;
    public int bufferActualHighTemperature;
    public int bufferActualLowTemperature;
    public int bufferActualModbusTemperature;
    public int bufferRequestType;
    public int bufferrequestFlowLineTemperature;
    public int bufferrequestReturnLineTemperature;
    public int bufferrequestHeatSinkTemperature;
    public int bufferrequestHeatingCapacity;
}
