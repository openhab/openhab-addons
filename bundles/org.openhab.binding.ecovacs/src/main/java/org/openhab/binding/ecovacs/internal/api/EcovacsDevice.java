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
package org.openhab.binding.ecovacs.internal.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.commands.IotDeviceCommand;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public interface EcovacsDevice {
    interface EventListener {
        void onFirmwareVersionChanged(EcovacsDevice device, String fwVersion);

        void onBatteryLevelUpdated(EcovacsDevice device, int newLevelPercent);

        void onChargingStateUpdated(EcovacsDevice device, boolean charging);

        void onCleaningModeUpdated(EcovacsDevice device, CleanMode newMode, Optional<String> areaDefinition);

        void onCleaningStatsUpdated(EcovacsDevice device, int cleanedArea, int cleaningTimeSeconds);

        void onWaterSystemPresentUpdated(EcovacsDevice device, boolean present);

        void onErrorReported(EcovacsDevice device, int errorCode);

        void onEventStreamFailure(EcovacsDevice device, Throwable error);
    }

    String getSerialNumber();

    String getModelName();

    boolean hasCapability(DeviceCapability cap);

    void connect(EventListener listener, ScheduledExecutorService scheduler)
            throws EcovacsApiException, InterruptedException;

    void disconnect(ScheduledExecutorService scheduler);

    <T> T sendCommand(IotDeviceCommand<T> command) throws EcovacsApiException, InterruptedException;

    List<CleanLogRecord> getCleanLogs() throws EcovacsApiException, InterruptedException;
}
