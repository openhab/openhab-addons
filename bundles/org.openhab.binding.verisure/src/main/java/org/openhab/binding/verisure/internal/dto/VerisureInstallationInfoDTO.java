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
package org.openhab.binding.verisure.internal.dto;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;

/**
 * Vacation mode and firmware status of an installation.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VerisureInstallationInfoDTO {

    private @Nullable Data data;

    public @Nullable VacationMode getVacationMode() {
        Installation installation = getInstallation();
        return installation != null ? installation.vacationMode : null;
    }

    public @Nullable String getLatestFirmware() {
        FirmwareStatus status = getFirmwareStatus();
        return status != null ? status.latestFirmware : null;
    }

    /**
     * @param deviceId device id of the gateway without spaces
     */
    public @Nullable String getRunningFirmware(String deviceId) {
        FirmwareStatus status = getFirmwareStatus();
        List<Gateway> gateways = status != null ? status.gateways : null;
        if (gateways != null) {
            for (Gateway gateway : gateways) {
                String deviceLabel = gateway.deviceLabel;
                if (deviceLabel != null && deviceId.equals(VerisureThingConfiguration.normalizeDeviceId(deviceLabel))) {
                    return gateway.reportedRunningFirmware;
                }
            }
        }
        return null;
    }

    private @Nullable Installation getInstallation() {
        Data localData = data;
        return localData != null ? localData.installation : null;
    }

    private @Nullable FirmwareStatus getFirmwareStatus() {
        Installation installation = getInstallation();
        Firmware firmware = installation != null ? installation.firmware : null;
        return firmware != null ? firmware.status : null;
    }

    private static class Data {
        private @Nullable Installation installation;
    }

    private static class Installation {
        private @Nullable VacationMode vacationMode;
        private @Nullable Firmware firmware;
    }

    private static class Firmware {
        private @Nullable FirmwareStatus status;
    }

    private static class FirmwareStatus {
        private @Nullable String latestFirmware;
        private @Nullable List<Gateway> gateways;
    }

    private static class Gateway {
        private @Nullable String deviceLabel;
        private @Nullable String reportedRunningFirmware;
    }

    public static class VacationMode {
        private boolean active;
        private @Nullable String fromDate;
        private @Nullable String toDate;

        public boolean isActive() {
            return active;
        }

        public @Nullable String getFromDate() {
            return fromDate;
        }

        public @Nullable String getToDate() {
            return toDate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(active, fromDate, toDate);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof VacationMode other && active == other.active
                    && Objects.equals(fromDate, other.fromDate) && Objects.equals(toDate, other.toDate);
        }
    }
}
