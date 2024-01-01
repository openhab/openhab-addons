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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the Energy Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
public class EnergyBlock {

    public int productionHeatToday;
    public int productionHeatTotalLow;
    public int productionHeatTotalHigh;
    public int productionWaterToday;
    public int productionWaterTotalLow;
    public int productionWaterTotalHigh;

    public int consumptionHeatToday;
    public int consumptionHeatTotalLow;
    public int consumptionHeatTotalHigh;
    public int consumptionWaterToday;
    public int consumptionWaterTotalLow;
    public int consumptionWaterTotalHigh;
}
