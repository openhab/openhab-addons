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
 * Control class for SG Ready register polling
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SgReadyEnergyManagementControl {
    public enum SgReadyEnMgmtFeatureKeys {
        EN_MGMT_SETTINGS,
        EN_MGMT_SETTINGS_INPUT1,
        EN_MGMT_SETTINGS_INPUT2,
        EN_MGMT_SYS_INFO
    }

    public boolean featureAvailable[] = new boolean[SgReadyEnMgmtFeatureKeys.values().length];
    public boolean featureReported[] = new boolean[SgReadyEnMgmtFeatureKeys.values().length];

    public boolean featureAvailable(SgReadyEnMgmtFeatureKeys key) {
        return featureAvailable[key.ordinal()];
    }

    public boolean setFeatureAvailable(SgReadyEnMgmtFeatureKeys key, boolean available) {
        featureAvailable[key.ordinal()] = available;
        return featureAvailable[key.ordinal()];
    }

    public boolean featureReported(SgReadyEnMgmtFeatureKeys key) {
        return featureReported[key.ordinal()];
    }

    public boolean setFeatureReported(SgReadyEnMgmtFeatureKeys key, boolean reported) {
        featureReported[key.ordinal()] = reported;
        return featureReported[key.ordinal()];
    }

    public SgReadyEnergyManagementControl() {
        for (int i = 0; i < featureAvailable.length; i++) {
            featureAvailable[i] = true;
            featureReported[i] = false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SgReadyEnergyManagementControl {");
        sb.append("\n  featureAvailable=[");
        for (int i = 0; i < featureAvailable.length; i++) {
            sb.append(SgReadyEnMgmtFeatureKeys.values()[i]).append("=").append(featureAvailable[i]);
            if (i < featureAvailable.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n  featureReported=[");
        for (int i = 0; i < featureReported.length; i++) {
            sb.append(SgReadyEnMgmtFeatureKeys.values()[i]).append("=").append(featureReported[i]);
            if (i < featureReported.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n}");
        return sb.toString();
    }
}
