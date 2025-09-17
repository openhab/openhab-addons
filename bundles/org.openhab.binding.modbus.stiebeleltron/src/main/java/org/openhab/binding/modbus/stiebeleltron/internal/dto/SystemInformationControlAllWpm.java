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
        public boolean featureAvailable[] = new boolean[SysInfoHpFeaturelKeys.values().length];
        public boolean featureReported[] = new boolean[SysInfoHpFeaturelKeys.values().length];

        public boolean available(SysInfoHpFeaturelKeys key) {
            return featureAvailable[key.ordinal()];
        }

        public boolean setAvailable(SysInfoHpFeaturelKeys key, boolean available) {
            return this.featureAvailable[key.ordinal()] = available;
        }

        public boolean reported(SysInfoHpFeaturelKeys key) {
            return featureReported[key.ordinal()];
        }

        public boolean setReported(SysInfoHpFeaturelKeys key, boolean reported) {
            return this.featureReported[key.ordinal()] = reported;
        }

        public SysInfoHpFeature() {
            for (SysInfoHpFeaturelKeys key : SysInfoHpFeaturelKeys.values()) {
                featureAvailable[key.ordinal()] = true;
                featureReported[key.ordinal()] = false;
            }
        }
    }

    public SysInfoHpFeature hpSysInfoList[];

    public SystemInformationControlAllWpm(int heatpumpCount) {
        for (int i = 0; i < SysInfoFeatureKeys.values().length; i++) {
            featureAvailable[i] = true;
            featureReported[i] = false;
        }

        hpSysInfoList = new SysInfoHpFeature[heatpumpCount];
        for (int i = 0; i < heatpumpCount; i++) {
            hpSysInfoList[i] = new SysInfoHpFeature();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System Information Control {");
        sb.append("\n  featureAvailable=[");
        for (int i = 0; i < featureAvailable.length; i++) {
            sb.append(SysInfoFeatureKeys.values()[i]).append("=").append(featureAvailable[i]);
            if (i < featureAvailable.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n  featureReported=[");
        for (int i = 0; i < featureReported.length; i++) {
            sb.append(SysInfoFeatureKeys.values()[i]).append("=").append(featureReported[i]);
            if (i < featureReported.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n  hpSysInfoList {");
        for (int i = 0; i < hpSysInfoList.length; i++) {
            SysInfoHpFeature hpSysInfo = hpSysInfoList[i];
            sb.append("\n    hp").append(i + 1).append(" {");
            sb.append("\n      featureAvailable=[");
            for (int j = 0; j < hpSysInfo.featureAvailable.length; j++) {
                sb.append(SysInfoHpFeaturelKeys.values()[j]).append("=").append(hpSysInfo.featureAvailable[j]);
                if (j < hpSysInfo.featureAvailable.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n    featureReported=[");
            for (int j = 0; j < hpSysInfo.featureReported.length; j++) {
                sb.append(SysInfoHpFeaturelKeys.values()[j]).append("=").append(hpSysInfo.featureReported[j]);
                if (j < hpSysInfo.featureReported.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n    },");
        }
        sb.append("\n  }\n}");
        return sb.toString();
    }
}
