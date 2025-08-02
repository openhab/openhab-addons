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
 * This class holds controls for availability and reporting of System State and Control of a WPM compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */

/**
 * Dto class for the System State Block Control Flags
 *
 * @author Thomas Burri - Initial contribution
 */
public class SystemStateControlAllWpm {
    public enum SystemStateFeatureKeys {
        SILENT_MODES,
        OPERATING_STATUS,
        DEFROST_INITIATED
    }

    public boolean featureAvailable[] = new boolean[SystemStateFeatureKeys.values().length];
    public boolean featureReported[] = new boolean[SystemStateFeatureKeys.values().length];

    public boolean featureAvailable(SystemStateFeatureKeys key) {
        return featureAvailable[key.ordinal()];
    }

    public boolean setFeatureAvailable(SystemStateFeatureKeys key, boolean available) {
        featureAvailable[key.ordinal()] = available;
        return featureAvailable[key.ordinal()];
    }

    public boolean featureReported(SystemStateFeatureKeys key) {
        return featureReported[key.ordinal()];
    }

    public boolean setFeatureReported(SystemStateFeatureKeys key, boolean reported) {
        featureReported[key.ordinal()] = reported;
        return featureReported[key.ordinal()];
    }

    public SystemStateControlAllWpm() {
        for (int i = 0; i < featureAvailable.length; i++) {
            featureAvailable[i] = true;
            featureReported[i] = false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System State Control {");
        sb.append("\n  featureAvailable=[");
        for (int i = 0; i < featureAvailable.length; i++) {
            sb.append(SystemStateFeatureKeys.values()[i]).append("=").append(featureAvailable[i]);
            if (i < featureAvailable.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n  featureReported=[");
        for (int i = 0; i < featureReported.length; i++) {
            sb.append(SystemStateFeatureKeys.values()[i]).append("=").append(featureReported[i]);
            if (i < featureReported.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]}");
        return sb.toString();
    }
}
