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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class extending the Energy Runtime Block of a WPM compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class EnergyRuntimeBlockAllWpm extends EnergyBlock {

    // Runtime common
    public int runtimeCompressorHeating;
    public int runtimeCompressorHotwater;
    public int runtimeCompressorCooling;
    public int runtimeNhz1;
    public int runtimeNhz2;
    public int runtimeNhz12;

    public static class HeatPumpEnergyRuntimeInfo {

        // Production
        public int productionHeatToday;
        public int productionHeatTotalLow;
        public int productionHeatTotalHigh;
        public int productionWaterToday;
        public int productionWaterTotalLow;
        public int productionWaterTotalHigh;
        public int productionNhzHeatingTotalLow;
        public int productionNhzHeatingTotalHigh;
        public int productionNhzHotwaterTotalLow;
        public int productionNhzHotwaterTotalHigh;

        // Consumption
        public int consumptionHeatToday;
        public int consumptionHeatTotalLow;
        public int consumptionHeatTotalHigh;
        public int consumptionWaterToday;
        public int consumptionWaterTotalLow;
        public int consumptionWaterTotalHigh;

        // Runtime WPMsystem/WPM3
        public int runtimeCompressor1Heating;
        public int runtimeCompressor2Heating;
        public int runtimeCompressor12Heating;
        public int runtimeCompressor1Hotwater;
        public int runtimeCompressor2Hotwater;
        public int runtimeCompressor12Hotwater;
        public int runtimeCompressorCooling;
        public int runtimeNhz1;
        public int runtimeNhz2;
        public int runtimeNhz12;
    }

    public HeatPumpEnergyRuntimeInfo[] heatPumps;

    public EnergyRuntimeBlockAllWpm(int nrOfHps) {
        super();
        this.heatPumps = new HeatPumpEnergyRuntimeInfo[nrOfHps];
        for (int i = 0; i < nrOfHps; i++) {
            this.heatPumps[i] = new HeatPumpEnergyRuntimeInfo();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Energy Runtime Block {\n");
        sb.append("runtimeCompressorHeating=").append(runtimeCompressorHeating).append("\n");
        sb.append("runtimeCompressorHotwater=").append(runtimeCompressorHotwater).append("\n");
        sb.append("runtimeCompressorCooling=").append(runtimeCompressorCooling).append("\n");
        sb.append("runtimeNhz1=").append(runtimeNhz1).append("\n");
        sb.append("runtimeNhz2=").append(runtimeNhz2).append("\n");
        sb.append("runtimeNhz12=").append(runtimeNhz12).append("\n");
        sb.append("heatPumps=[\n");
        int idx = 1;
        for (HeatPumpEnergyRuntimeInfo hp : heatPumps) {
            sb.append("  {hp").append(idx++).append("\n");
            sb.append("    productionHeatToday=").append(hp.productionHeatToday).append("\n");
            sb.append("    productionHeatTotalLow=").append(hp.productionHeatTotalLow).append("\n");
            sb.append("    productionHeatTotalHigh=").append(hp.productionHeatTotalHigh).append("\n");
            sb.append("    productionWaterToday=").append(hp.productionWaterToday).append("\n");
            sb.append("    productionWaterTotalLow=").append(hp.productionWaterTotalLow).append("\n");
            sb.append("    productionWaterTotalHigh=").append(hp.productionWaterTotalHigh).append("\n");
            sb.append("    productionNhzHeatingTotalLow=").append(hp.productionNhzHeatingTotalLow).append("\n");
            sb.append("    productionNhzHeatingTotalHigh=").append(hp.productionNhzHeatingTotalHigh).append("\n");
            sb.append("    productionNhzHotwaterTotalLow=").append(hp.productionNhzHotwaterTotalLow).append("\n");
            sb.append("    productionNhzHotwaterTotalHigh=").append(hp.productionNhzHotwaterTotalHigh).append("\n");
            sb.append("    consumptionHeatToday=").append(hp.consumptionHeatToday).append("\n");
            sb.append("    consumptionHeatTotalLow=").append(hp.consumptionHeatTotalLow).append("\n");
            sb.append("    consumptionHeatTotalHigh=").append(hp.consumptionHeatTotalHigh).append("\n");
            sb.append("    consumptionWaterToday=").append(hp.consumptionWaterToday).append("\n");
            sb.append("    consumptionWaterTotalLow=").append(hp.consumptionWaterTotalLow).append("\n");
            sb.append("    consumptionWaterTotalHigh=").append(hp.consumptionWaterTotalHigh).append("\n");
            sb.append("    runtimeCompressor1Heating=").append(hp.runtimeCompressor1Heating).append("\n");
            sb.append("    runtimeCompressor2Heating=").append(hp.runtimeCompressor2Heating).append("\n");
            sb.append("    runtimeCompressor12Heating=").append(hp.runtimeCompressor12Heating).append("\n");
            sb.append("    runtimeCompressor1Hotwater=").append(hp.runtimeCompressor1Hotwater).append("\n");
            sb.append("    runtimeCompressor2Hotwater=").append(hp.runtimeCompressor2Hotwater).append("\n");
            sb.append("    runtimeCompressor12Hotwater=").append(hp.runtimeCompressor12Hotwater).append("\n");
            sb.append("    runtimeCompressorCooling=").append(hp.runtimeCompressorCooling).append("\n");
            sb.append("    runtimeNhz1=").append(hp.runtimeNhz1).append("\n");
            sb.append("    runtimeNhz2=").append(hp.runtimeNhz2).append("\n");
            sb.append("    runtimeNhz12=").append(hp.runtimeNhz12).append("\n");
            sb.append("}\n");
        }
        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }
}
