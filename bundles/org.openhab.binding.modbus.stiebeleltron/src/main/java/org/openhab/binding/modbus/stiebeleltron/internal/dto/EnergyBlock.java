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
 * Dto class for the Energy Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
public class EnergyBlock {

    public int production_heat_today;
    public int production_heat_total_low;
    public int production_heat_total_high;
    public int production_water_today;
    public int production_water_total_low;
    public int production_water_total_high;

    public int consumption_heat_today;
    public int consumption_heat_total_low;
    public int consumption_heat_total_high;
    public int consumption_water_today;
    public int consumption_water_total_low;
    public int consumption_water_total_high;
}
