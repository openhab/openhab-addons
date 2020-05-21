/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

    public short temperature_fek;
    public short temperature_fek_setpoint;
    public short humidity_ffk;
    public short dewpoint_ffk;
    public short temperature_outdoor;
    public short temperature_hk1;
    public short temperature_hk1_setpoint;
    public short temperature_supply;
    public short temperature_return;
    public short temperature_source;
    public short temperature_water;
    public short temperature_water_setpoint;
}
