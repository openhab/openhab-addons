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
 * Dto class for the Heatpump Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
public class HeatpumpBlock {
    public int heatpumpErrorState;
    public int heatpumpErrorNumber;
    public int heatpumpState;
    public int heatpumpOperatingState;
    public int heatpumpTFlow;
    public int heatpumpTReturn;
    public int heatpumpVolSink;
    public int heatpumpTEQin;
    public int heatpumpTEQout;
    public int heatpumpVolSource;
    public int heatpumpCompressorRating;
    public int heatpumpQpHeating;
    public int heatpumpFIPowerConsumption;
    public int heatpumpCOP;
    public int heatpumpRequestPassword;
    public int heatpumpRequestType;
    public int heatpumpRequestTFlow;
    public int heatpumpRequestTReturn;
    public int heatpumpRequestHeatSink;
    public int heatpumpRelaisState;
    public long heatpumpVdAE;
    public long heatpumpVdAQ;
    public long heatpumpVdAEswap;
    public long heatpumpVdAQswap;
}
