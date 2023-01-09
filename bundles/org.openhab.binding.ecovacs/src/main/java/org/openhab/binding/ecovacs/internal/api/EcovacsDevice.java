/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.ecovacs.internal.api.model.SuctionPower;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public interface EcovacsDevice {
    public interface EventListener {
        void onFirmwareVersionChanged(EcovacsDevice device, String fwVersion);

        void onBatteryLevelUpdated(EcovacsDevice device, int newLevelPercent);

        void onChargingStateUpdated(EcovacsDevice device, boolean charging);

        void onCleaningModeUpdated(EcovacsDevice device, CleanMode newMode, Optional<String> areaDefinition);

        void onCleaningPowerUpdated(EcovacsDevice device, SuctionPower newPower);

        void onCleaningStatsUpdated(EcovacsDevice device, int cleanedArea, int cleaningTimeSeconds);

        void onWaterSystemPresentUpdated(EcovacsDevice device, boolean present);

        void onErrorReported(EcovacsDevice device, int errorCode);

        void onEventStreamFailure(EcovacsDevice device, Throwable error);
    }

    public String getSerialNumber();

    public String getModelName();

    public boolean hasCapability(DeviceCapability cap);

    public void connect(EventListener listener, ScheduledExecutorService scheduler)
            throws EcovacsApiException, InterruptedException;

    public void disconnect(ScheduledExecutorService scheduler);

    public <T> T sendCommand(IotDeviceCommand<T> command) throws EcovacsApiException, InterruptedException;

    public List<CleanLogRecord> getCleanLogs() throws EcovacsApiException, InterruptedException;
}
