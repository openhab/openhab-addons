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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the Energy Block
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Extending by values for NHZ of a WPM compatible heat pump
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

    public int productionNhzHeatingTotalLow;
    public int productionNhzHeatingTotalHigh;

    public int productionNhzHotwaterTotalLow;
    public int productionNhzHotwaterTotalHigh;

    @Override
    public String toString() {
        return "EnergyBlock {\n" + "  productionHeatToday=" + productionHeatToday + "\n,  productionHeatTotalLow="
                + productionHeatTotalLow + "\n,  productionHeatTotalHigh=" + productionHeatTotalHigh
                + "\n,  productionWaterToday=" + productionWaterToday + "\n,  productionWaterTotalLow="
                + productionWaterTotalLow + "\n,  productionWaterTotalHigh=" + productionWaterTotalHigh
                + "\n,  consumptionHeatToday=" + consumptionHeatToday + "\n,  consumptionHeatTotalLow="
                + consumptionHeatTotalLow + "\n,  consumptionHeatTotalHigh=" + consumptionHeatTotalHigh
                + "\n,  consumptionWaterToday=" + consumptionWaterToday + "\n,  consumptionWaterTotalLow="
                + consumptionWaterTotalLow + "\n,  consumptionWaterTotalHigh=" + consumptionWaterTotalHigh
                + "\n,  productionNhzHeatingTotalLow=" + productionNhzHeatingTotalLow
                + "\n,  productionNhzHeatingTotalHigh=" + productionNhzHeatingTotalHigh
                + "\n,  productionNhzHotwaterTotalLow=" + productionNhzHotwaterTotalLow
                + "\n,  productionNhzHotwaterTotalHigh=" + productionNhzHotwaterTotalHigh + "\n}";
    }
}
