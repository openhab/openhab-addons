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
 * Dto class for the Heatpump1 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
public class Heatpump1Block {
    public int heatpump1ErrorState;
    public int heatpump1ErrorNumber;
    public int heatpump1State;
    public int heatpump1OperatingState;
    public int heatpump1TFlow;
    public int heatpump1TReturn;
    public int heatpump1VolSink;
    public int heatpump1TEQin;
    public int heatpump1TEQout;
    public int heatpump1VolSource;
    public int heatpump1CompressorRating;
    public int heatpump1QpHeating;
    public int heatpump1FIPowerConsumption;
    public int heatpump1COP;
}
