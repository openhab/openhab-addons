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
 * This class holds controls for availability and reporting of Energy and Runtime data of a WPM compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class EnergyRuntimeControlAllWpm {
    public enum EnergyRuntimeFeatureKeys {
        COMMON_RUNTIMES,
        COMMON_COOLING_RUNTIME
    }

    public enum EnergyRuntimeHpFeatureKeys {
        RUNTIMES,
        COOLING_RUNTIME,
        NHZ_RUNTIMES
    }

    public boolean featureAvailable[] = new boolean[EnergyRuntimeFeatureKeys.values().length];
    public boolean featureReported[] = new boolean[EnergyRuntimeFeatureKeys.values().length];

    public boolean featureAvailable(EnergyRuntimeFeatureKeys key) {
        return featureAvailable[key.ordinal()];
    }

    public boolean setFeatureAvailable(EnergyRuntimeFeatureKeys key, boolean available) {
        featureAvailable[key.ordinal()] = available;
        return featureAvailable[key.ordinal()];
    }

    public boolean featureReported(EnergyRuntimeFeatureKeys key) {
        return featureReported[key.ordinal()];
    }

    public boolean setFeatureReported(EnergyRuntimeFeatureKeys key, boolean reported) {
        featureReported[key.ordinal()] = reported;
        return featureReported[key.ordinal()];
    }

    public class EnergyRuntimeHpFeature {
        public boolean featureAvailable[] = new boolean[EnergyRuntimeHpFeatureKeys.values().length];
        public boolean featureReported[] = new boolean[EnergyRuntimeHpFeatureKeys.values().length];

        public boolean featureAvailable(EnergyRuntimeHpFeatureKeys key) {
            return featureAvailable[key.ordinal()];
        }

        public boolean setFeatureAvailable(EnergyRuntimeHpFeatureKeys key, boolean available) {
            featureAvailable[key.ordinal()] = available;
            return featureAvailable[key.ordinal()];
        }

        public boolean featureReported(EnergyRuntimeHpFeatureKeys key) {
            return featureReported[key.ordinal()];
        }

        public boolean setFeatureReported(EnergyRuntimeHpFeatureKeys key, boolean reported) {
            featureReported[key.ordinal()] = reported;
            return featureReported[key.ordinal()];
        }
    }

    public EnergyRuntimeHpFeature hpEgRtList[];

    public EnergyRuntimeControlAllWpm(int nrOfHps) {
        for (int i = 0; i < EnergyRuntimeFeatureKeys.values().length; i++) {
            featureAvailable[i] = true;
            featureReported[i] = false;
        }

        hpEgRtList = new EnergyRuntimeHpFeature[nrOfHps];
        for (int i = 0; i < nrOfHps; i++) {
            hpEgRtList[i] = new EnergyRuntimeHpFeature();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Energy Runtime Control { ");
        sb.append("\nfeatureAvailable=").append(java.util.Arrays.toString(featureAvailable)).append(", ");
        sb.append("\nfeatureReported=").append(java.util.Arrays.toString(featureReported)).append(", ");
        sb.append("\nhpEgRtList=[");
        for (int i = 0; i < hpEgRtList.length; i++) {
            sb.append("\nHpFeature ").append(i).append(": { ");
            sb.append("featureAvailable=").append(java.util.Arrays.toString(hpEgRtList[i].featureAvailable))
                    .append(", ");
            sb.append(" / featureReported=").append(java.util.Arrays.toString(hpEgRtList[i].featureReported));
            sb.append(" }");
            if (i < hpEgRtList.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        sb.append(" }");
        return sb.toString();
    }
}
