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
 * Dto class extending System Information Block to support all system information values of a WPM
 * compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SystemInformationBlockAllWpm extends SystemInformationBlock {

    public short temperatureFe7;
    public short temperatureFe7SetPoint;

    public short temperatureHc2;
    public short temperatureHc2SetPoint;

    public short temperatureFlowHp;
    public short temperatureFlowNhz;
    public short temperatureFlow;

    public short temperatureFixedSetPoint;

    public short temperatureBuffer;
    public short temperatureBufferSetPoint;

    public short pressureHeating;
    public short flowRate;

    public short temperatureFanCooling;
    public short temperatureFanCoolingSetPoint;
    public short temperatureAreaCooling;
    public short temperatureAreaCoolingSetPoint;

    // The following three values are only available for WPM3
    public short temperatureCollectorSolar;
    public short temperatureCylinderSolar;
    public int runtimeSolar;

    // The following three values are only available for WPMsystem and WPM3
    public short temperatureExtHeatSource;
    public short temperatureExtHeatSourceSetPoint;
    public int runtimeExtHeatSource;

    public short lowerHeatingLimit;
    public short lowerWaterLimit;

    public short temperatureSourceMin;
    public short pressureSource;

    // The following three values are only available for WPM3i
    public short temperatureHotgas;
    public short pressureHigh;
    public short pressureLow;

    public static class HeatPumpInfo {
        public short temperatureReturn;
        public short temperatureFlow;
        public short temperatureHotgas;
        public short pressureLow;
        public short pressureMean;
        public short pressureHigh;
        public short flowRate;
    }

    public HeatPumpInfo[] heatPumps;

    public SystemInformationBlockAllWpm(int nrOfHps) {
        super();
        heatPumps = new HeatPumpInfo[nrOfHps];
        for (int i = 0; i < heatPumps.length; i++) {
            heatPumps[i] = new HeatPumpInfo();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System Information Block {\n");
        sb.append("  temperatureFe7=").append(temperatureFe7).append(",\n");
        sb.append("  temperatureFe7SetPoint=").append(temperatureFe7SetPoint).append(",\n");
        sb.append("  temperatureHc2=").append(temperatureHc2).append(",\n");
        sb.append("  temperatureHc2SetPoint=").append(temperatureHc2SetPoint).append(",\n");
        sb.append("  temperatureFlowHp=").append(temperatureFlowHp).append(",\n");
        sb.append("  temperatureFlowNhz=").append(temperatureFlowNhz).append(",\n");
        sb.append("  temperatureFlow=").append(temperatureFlow).append(",\n");
        sb.append("  temperatureFixedSetPoint=").append(temperatureFixedSetPoint).append(",\n");
        sb.append("  temperatureBuffer=").append(temperatureBuffer).append(",\n");
        sb.append("  temperatureBufferSetPoint=").append(temperatureBufferSetPoint).append(",\n");
        sb.append("  pressureHeating=").append(pressureHeating).append(",\n");
        sb.append("  flowRate=").append(flowRate).append(",\n");
        sb.append("  temperatureFanCooling=").append(temperatureFanCooling).append(",\n");
        sb.append("  temperatureFanCoolingSetPoint=").append(temperatureFanCoolingSetPoint).append(",\n");
        sb.append("  temperatureAreaCooling=").append(temperatureAreaCooling).append(",\n");
        sb.append("  temperatureAreaCoolingSetPoint=").append(temperatureAreaCoolingSetPoint).append(",\n");
        sb.append("  temperatureCollectorSolar=").append(temperatureCollectorSolar).append(",\n");
        sb.append("  temperatureCylinderSolar=").append(temperatureCylinderSolar).append(",\n");
        sb.append("  runtimeSolar=").append(runtimeSolar).append(",\n");
        sb.append("  temperatureExtHeatSource=").append(temperatureExtHeatSource).append(",\n");
        sb.append("  temperatureExtHeatSourceSetPoint=").append(temperatureExtHeatSourceSetPoint).append(",\n");
        sb.append("  runtimeExtHeatSource=").append(runtimeExtHeatSource).append(",\n");
        sb.append("  lowerHeatingLimit=").append(lowerHeatingLimit).append(",\n");
        sb.append("  lowerWaterLimit=").append(lowerWaterLimit).append(",\n");
        sb.append("  temperatureSourceMin=").append(temperatureSourceMin).append(",\n");
        sb.append("  pressureSource=").append(pressureSource).append(",\n");
        sb.append("  temperatureHotgas=").append(temperatureHotgas).append(",\n");
        sb.append("  pressureHigh=").append(pressureHigh).append(",\n");
        sb.append("  pressureLow=").append(pressureLow).append(",\n");
        sb.append("  heatPumps=[\n");
        int idx = 1;
        for (HeatPumpInfo hp : heatPumps) {
            sb.append("    hp").append(idx++).append(" {\n      temperatureReturn=").append(hp.temperatureReturn)
                    .append(",\n      temperatureFlow=").append(hp.temperatureFlow)
                    .append(",\n      temperatureHotgas=").append(hp.temperatureHotgas).append(",\n      pressureLow=")
                    .append(hp.pressureLow).append(",\n      pressureMean=").append(hp.pressureMean)
                    .append(",\n      pressureHigh=").append(hp.pressureHigh).append(",\n      flowRate=")
                    .append(hp.flowRate).append("\n    },\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}
