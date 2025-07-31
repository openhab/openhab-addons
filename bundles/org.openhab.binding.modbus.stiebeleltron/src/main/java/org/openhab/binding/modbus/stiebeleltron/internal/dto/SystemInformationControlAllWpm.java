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
 * This class holds controls for availability and reporting of System Information of a WPM compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SystemInformationControlAllWpm {
    public enum SysInfoFeatureKeys {
        FE7,
        FEK,
        HC2,
        TEMP_FLOW,
        TEMP_RETURN,
        FAN_COOLING,
        AREA_COOLING,
        SOLAR_THERMAL,
        EXTERNAL_HEATING,
        LOWER_LIMITS,
        SOURCE_VALUES,
        HOTGAS
    }

    public enum SysInfoHpFeaturelKeys {
        HP_TEMPERATURE_RETURN,
        HP_TEMPERATURE_FLOW,
        HP_TEMPERATURE_HOTGAS,
        HP_PRESSURE_LOW,
        HP_PRESSURE_MEAN,
        HP_PRESSURE_HIGH,
        HP_FLOW_RATE
    }

    public boolean featureAvailable[] = new boolean[SysInfoFeatureKeys.values().length];

    public boolean featureAvailable(SysInfoFeatureKeys key) {
        return featureAvailable[key.ordinal()];
    }

    public boolean featureReported[] = new boolean[SysInfoFeatureKeys.values().length];

    public boolean featureReported(SysInfoFeatureKeys key) {
        return featureReported[key.ordinal()];
    }

    public boolean setFeatureAvailable(SysInfoFeatureKeys key, boolean available) {
        return featureAvailable[key.ordinal()] = available;
    }

    public boolean setFeatureReported(SysInfoFeatureKeys key, boolean reported) {
        return featureReported[key.ordinal()] = reported;
    }

    public class SysInfoHpFeature {
        public boolean available[] = new boolean[SysInfoHpFeaturelKeys.values().length];
        public boolean reported[] = new boolean[SysInfoHpFeaturelKeys.values().length];

        public boolean available(SysInfoHpFeaturelKeys key) {
            return available[key.ordinal()];
        }

        public boolean setAvailable(SysInfoHpFeaturelKeys key, boolean available) {
            return this.available[key.ordinal()] = available;
        }

        public boolean reported(SysInfoHpFeaturelKeys key) {
            return reported[key.ordinal()];
        }

        public boolean setReported(SysInfoHpFeaturelKeys key, boolean reported) {
            return this.reported[key.ordinal()] = reported;
        }

        public SysInfoHpFeature() {
            for (SysInfoHpFeaturelKeys key : SysInfoHpFeaturelKeys.values()) {
                available[key.ordinal()] = true;
                reported[key.ordinal()] = false;
            }
        }
    }

    public SysInfoHpFeature hpSysInfoList[];

    public SystemInformationControlAllWpm(int nrOfHps) {
        for (int i = 0; i < SysInfoFeatureKeys.values().length; i++) {
            featureAvailable[i] = true;
            featureReported[i] = false;
        }

        hpSysInfoList = new SysInfoHpFeature[nrOfHps];
        for (int i = 0; i < nrOfHps; i++) {
            hpSysInfoList[i] = new SysInfoHpFeature();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System Information Control {");
        sb.append("\n  featureAvailable: ");
        for (int i = 0; i < featureAvailable.length; i++) {
            sb.append(SysInfoFeatureKeys.values()[i]).append("=").append(featureAvailable[i]).append(", ");
        }
        sb.append("\n  featureReported: ");
        for (int i = 0; i < featureReported.length; i++) {
            sb.append(SysInfoFeatureKeys.values()[i]).append("=").append(featureReported[i]).append(", ");
        }
        sb.append("\n  hpSysInfoList: [");
        for (int i = 0; i < hpSysInfoList.length; i++) {
            sb.append("\n    HP ").append(i).append(": {");
            for (SysInfoHpFeaturelKeys key : SysInfoHpFeaturelKeys.values()) {
                sb.append("\n      ").append(key).append(" {available=").append(hpSysInfoList[i].available(key))
                        .append(", reported=").append(hpSysInfoList[i].reported(key)).append("},");
            }
            sb.append("\n    },");
        }
        sb.append("\n  ]");
        sb.append("\n}");
        return sb.toString();
    }
}
