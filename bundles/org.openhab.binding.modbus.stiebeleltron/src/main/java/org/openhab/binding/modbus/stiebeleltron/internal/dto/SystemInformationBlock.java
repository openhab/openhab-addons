/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the System Information Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
public class SystemInformationBlock {

    public short temperatureFek;
    public short temperatureFekSetPoint;
    public short humidityFek;
    public short dewpointFek;
    public short temperatureOutdoor;
    public short temperatureHk1;
    public short temperatureHk1SetPoint;
    public short temperatureSupply;
    public short temperatureReturn;
    public short temperatureSource;
    public short temperatureWater;
    public short temperatureWaterSetPoint;
}
