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
package org.openhab.binding.ecovacs.internal.handler;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.commands.GetBatteryInfoCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetChargeStateCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetChildLockCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetComponentLifeSpanCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetCuttingHeightCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetMoveUpWarningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetMowerStateCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetNetworkInfoCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetSafeProtectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetTotalStatsCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetTotalStatsCommand.TotalStats;
import org.openhab.binding.ecovacs.internal.api.commands.GetTrueDetectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetVolumeCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GoChargingCommand;
import org.openhab.binding.ecovacs.internal.api.commands.IotDeviceCommand;
import org.openhab.binding.ecovacs.internal.api.commands.MowerCleanCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetChildLockCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetMoveUpWarningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetSafeProtectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetTrueDetectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetVolumeCommand;
import org.openhab.binding.ecovacs.internal.api.commands.StartEdgeCutCommand;
import org.openhab.binding.ecovacs.internal.api.commands.ZoneMowingCommand;
import org.openhab.binding.ecovacs.internal.api.model.ChargeMode;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.Component;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.model.NetworkInfo;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EcovacsMowerHandler} is responsible for handling data and commands from/to GOAT mowers.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class EcovacsMowerHandler extends AbstractEcovacsDeviceHandler implements EcovacsDevice.EventListener {

    private volatile long mowingStartTimeMs;

    public EcovacsMowerHandler(Thing thing, TranslationProvider i18Provider, LocaleProvider localeProvider) {
        super(thing, i18Provider, localeProvider);
    }

    @Override
    protected String getLogPrefix() {
        return "mower";
    }

    @Override
    protected void afterDeviceFound(EcovacsDevice device) {
        removeUnsupportedChannels(device);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final EcovacsDevice device = this.device;
        if (device == null) {
            logger.debug("{}: Ignoring command {}, no active connection", serialNumber, command);
            return;
        }
        String channel = channelUID.getId();

        try {
            if (channel.equals(CHANNEL_ID_COMMAND) && command instanceof StringType) {
                handleMowerCommand(device, command.toString());
                // Schedule a quick poll to pick up state change in case MQTT event is delayed
                scheduleNextPoll(5);
                return;
            } else if (channel.equals(CHANNEL_ID_VOICE_VOLUME) && command instanceof DecimalType volume) {
                // Device uses 0-10 scale, openHAB uses 0-100 percent. Add 5 for rounding before integer division.
                int volumePercent = volume.intValue();
                device.sendCommand(new SetVolumeCommand((volumePercent + 5) / 10));
                return;
            } else if (channel.equals(CHANNEL_ID_TRUE_DETECT_3D) && command instanceof OnOffType) {
                device.sendCommand(new SetTrueDetectCommand(command == OnOffType.ON));
                return;
            } else if (channel.equals(CHANNEL_ID_SAFE_PROTECT) && command instanceof OnOffType) {
                device.sendCommand(new SetSafeProtectCommand(command == OnOffType.ON));
                return;
            } else if (channel.equals(CHANNEL_ID_CHILD_LOCK) && command instanceof OnOffType) {
                device.sendCommand(new SetChildLockCommand(command == OnOffType.ON));
                return;
            } else if (channel.equals(CHANNEL_ID_MOVEUP_WARNING) && command instanceof OnOffType) {
                device.sendCommand(new SetMoveUpWarningCommand(command == OnOffType.ON));
                return;
            }
            logger.debug("{}: Ignoring unsupported command {} for channel {}", serialNumber, command, channel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Handling device command {} failed", serialNumber, command, e);
        }
    }

    private void handleMowerCommand(EcovacsDevice device, String command)
            throws EcovacsApiException, InterruptedException {
        switch (command) {
            case CMD_MOW:
                // If mower is paused, send resume instead of start (mower ignores start while paused).
                // If mower is not paused, send start (mower ignores resume while idle).
                if (lastCleanMode == CleanMode.PAUSE) {
                    device.sendCommand(new MowerCleanCommand("resume"));
                } else {
                    device.sendCommand(new MowerCleanCommand("start"));
                }
                break;
            case CMD_PAUSE:
                device.sendCommand(new MowerCleanCommand("pause"));
                break;
            case CMD_RESUME:
                device.sendCommand(new MowerCleanCommand("resume"));
                break;
            case CMD_STOP:
                device.sendCommand(new MowerCleanCommand("stop"));
                break;
            case CMD_DOCK:
                device.sendCommand(new GoChargingCommand());
                break;
            case CMD_EDGE_CUT:
                device.sendCommand(new StartEdgeCutCommand());
                break;
            default:
                if (command.startsWith("zone:") && device.hasCapability(DeviceCapability.ZONE_MOWING)) {
                    String zones = command.substring(5);
                    if (zones.isEmpty()) {
                        logger.debug("{}: Zone mowing command requires zone IDs", serialNumber);
                        return;
                    }
                    device.sendCommand(new ZoneMowingCommand(zones));
                } else {
                    logger.debug("{}: Unknown mower command: {}", serialNumber, command);
                }
                break;
        }
    }

    // --- EcovacsDevice.EventListener implementation ---

    @Override
    public void onChargingStateUpdated(EcovacsDevice device, boolean charging) {
        lastWasCharging = charging;
        updateMowerStateChannel();
    }

    @Override
    public void onCleaningModeUpdated(EcovacsDevice device, CleanMode newMode, Optional<String> areaDefinition) {
        lastCleanMode = newMode;
        updateMowerStateChannel();
        if (newMode == CleanMode.RETURNING) {
            scheduleNextPoll(30);
        } else if (newMode.isIdle()) {
            mowingStartTimeMs = 0;
            updateState(CHANNEL_ID_MOWED_AREA, UnDefType.UNDEF);
            updateState(CHANNEL_ID_MOWING_TIME, UnDefType.UNDEF);
        } else if (!newMode.isIdle() && mowingStartTimeMs == 0) {
            // Mowing started
            mowingStartTimeMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onCleaningStatsUpdated(EcovacsDevice device, int cleanedArea, int cleaningTimeSeconds) {
    }

    @Override
    public void onMowingStatsUpdated(EcovacsDevice device, int mowedAreaSqCm, int timeSeconds) {
        if (mowedAreaSqCm > 0) {
            updateState(CHANNEL_ID_MOWED_AREA, new QuantityType<>(mowedAreaSqCm / 10000.0, SIUnits.SQUARE_METRE));
        }
        long startMs = mowingStartTimeMs;
        if (startMs > 0) {
            long elapsedSeconds = (System.currentTimeMillis() - startMs) / 1000;
            updateState(CHANNEL_ID_MOWING_TIME, new QuantityType<>(elapsedSeconds, Units.SECOND));
        }
    }

    @Override
    public void onWaterSystemPresentUpdated(EcovacsDevice device, boolean present) {
        // Not relevant for mowers, ignore
    }

    @Override
    public void onErrorReported(EcovacsDevice device, int errorCode) {
        // Error code 200 is sent on mow start and means "no error" — ignore it
        if (errorCode == 200) {
            return;
        }
        updateState(CHANNEL_ID_ERROR_CODE, new DecimalType(errorCode));
        final Locale locale = localeProvider.getLocale();
        String errorDesc = i18Provider.getText(bundle, "ecovacs.mower.error-code." + errorCode, null, locale);
        if (errorDesc == null) {
            errorDesc = i18Provider.getText(bundle, "ecovacs.mower.error-code.unknown", "", locale, errorCode);
        }
        updateState(CHANNEL_ID_ERROR_DESCRIPTION, new StringType(errorDesc));
    }

    @Override
    public void onMowingSessionFinished(EcovacsDevice device, long startTimestamp, int durationSeconds, int areaSqCm) {
        if (startTimestamp > 0) {
            Instant startInstant = Instant.ofEpochSecond(startTimestamp);
            updateState(CHANNEL_ID_LAST_MOW_START, new DateTimeType(startInstant));
        }
        updateState(CHANNEL_ID_LAST_MOW_DURATION, new QuantityType<>(durationSeconds, Units.SECOND));
        updateState(CHANNEL_ID_LAST_MOW_AREA, new QuantityType<>(areaSqCm / 10000.0, SIUnits.SQUARE_METRE));
    }

    // --- State management ---

    private void updateMowerStateChannel() {
        Boolean charging = this.lastWasCharging;
        CleanMode cleanMode = this.lastCleanMode;
        if (charging == null || cleanMode == null) {
            return;
        }
        String state = determineMowerState(charging, cleanMode);
        updateState(CHANNEL_ID_MOWER_STATE, StringType.valueOf(state));
    }

    private String determineMowerState(boolean charging, CleanMode cleanMode) {
        if (charging && cleanMode != CleanMode.RETURNING) {
            return "charging";
        }
        if (cleanMode == CleanMode.RETURNING) {
            return "returning";
        }
        if (cleanMode.isActive()) {
            if (cleanMode == CleanMode.EDGE) {
                return "edgeCutting";
            }
            return "mowing";
        }
        if (cleanMode == CleanMode.PAUSE) {
            return "paused";
        }
        if (charging) {
            return "docked";
        }
        return "idle";
    }

    // --- Connectivity ---

    @Override
    protected void connectToDevice() {
        doWithDevice(device -> {
            device.connect(this, scheduler);
            // Fetch initial status
            Integer batteryPercent = device.sendCommand(new GetBatteryInfoCommand());
            onBatteryLevelUpdated(device, batteryPercent);
            // Fetch initial charge and mowing state
            try {
                lastWasCharging = device.sendCommand(new GetChargeStateCommand()) == ChargeMode.CHARGING;
                CleanMode mode = device.sendCommand(new GetMowerStateCommand());
                lastCleanMode = mode;
                updateMowerStateChannel();
            } catch (EcovacsApiException e) {
                logger.debug("{}: Could not fetch initial state: {}", serialNumber, e.getMessage());
            }
            scheduleNextPoll(-1);
            logger.debug("{}: Mower connected", serialNumber);
            updateStatus(ThingStatus.ONLINE);
        });
    }

    // --- Polling ---

    @Override
    protected void pollData() {
        logger.debug("{}: Polling mower data", serialNumber);
        final EcovacsDevice device = this.device;
        if (device == null) {
            return;
        }

        Integer batteryPercent = pollCommand(device, new GetBatteryInfoCommand(), "battery");
        if (batteryPercent == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        onBatteryLevelUpdated(device, batteryPercent);

        ChargeMode chargeMode = pollCommand(device, new GetChargeStateCommand(), "charge state");
        if (chargeMode == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        lastWasCharging = chargeMode == ChargeMode.CHARGING;

        CleanMode mode = pollCommand(device, new GetMowerStateCommand(), "mower state");
        if (mode == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        lastCleanMode = mode;
        updateMowerStateChannel();

        TotalStats totalStats = pollCommand(device, new GetTotalStatsCommand(), "total stats");
        if (totalStats == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        updateState(CHANNEL_ID_TOTAL_MOWING_TIME, new QuantityType<>(totalStats.totalRuntime / 3600.0, Units.HOUR));
        updateState(CHANNEL_ID_TOTAL_MOWED_AREA, new QuantityType<>(totalStats.totalArea, SIUnits.SQUARE_METRE));
        updateState(CHANNEL_ID_TOTAL_MOW_RUNS, new DecimalType(totalStats.cleanRuns));

        if (device.hasCapability(DeviceCapability.READ_NETWORK_INFO)) {
            NetworkInfo netInfo = pollCommand(device, new GetNetworkInfoCommand(), "network info");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (netInfo != null && netInfo.wifiRssi != 0) {
                updateState(CHANNEL_ID_WIFI_RSSI, new QuantityType<>(netInfo.wifiRssi, Units.DECIBEL_MILLIWATTS));
            }
        }

        Integer bladePercent = pollCommand(device, new GetComponentLifeSpanCommand(Component.BLADE), "blade lifespan");
        if (bladePercent == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        updateState(CHANNEL_ID_BLADE_LIFETIME, new QuantityType<>(bladePercent, Units.PERCENT));

        if (device.hasCapability(DeviceCapability.CUTTING_HEIGHT)) {
            Integer heightMm = pollCommand(device, new GetCuttingHeightCommand(), "cutting height");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (heightMm != null) {
                updateState(CHANNEL_ID_CUTTING_HEIGHT, new QuantityType<>(heightMm, MetricPrefix.MILLI(SIUnits.METRE)));
            }
        }

        if (device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
            Integer level = pollCommand(device, new GetVolumeCommand(), "volume");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (level != null) {
                updateState(CHANNEL_ID_VOICE_VOLUME, new PercentType(level * 10));
            }
        }

        if (device.hasCapability(DeviceCapability.TRUE_DETECT_3D)) {
            Boolean trueDetectEnabled = pollCommand(device, new GetTrueDetectCommand(), "TrueDetect state");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (trueDetectEnabled != null) {
                updateState(CHANNEL_ID_TRUE_DETECT_3D, OnOffType.from(trueDetectEnabled));
            }
        }

        if (device.hasCapability(DeviceCapability.SAFE_PROTECT)) {
            Boolean safeProtect = pollCommand(device, new GetSafeProtectCommand(), "SafeProtect state");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (safeProtect != null) {
                updateState(CHANNEL_ID_SAFE_PROTECT, OnOffType.from(safeProtect));
            }
        }

        if (device.hasCapability(DeviceCapability.CHILD_LOCK)) {
            Boolean childLock = pollCommand(device, new GetChildLockCommand(), "child lock state");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (childLock != null) {
                updateState(CHANNEL_ID_CHILD_LOCK, OnOffType.from(childLock));
            }
        }

        if (device.hasCapability(DeviceCapability.MOVEUP_WARNING)) {
            Boolean moveUpWarning = pollCommand(device, new GetMoveUpWarningCommand(), "move-up warning state");
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (moveUpWarning != null) {
                updateState(CHANNEL_ID_MOVEUP_WARNING, OnOffType.from(moveUpWarning));
            }
        }

        lastSuccessfulPollTimestamp = System.currentTimeMillis();
        // Poll more frequently while mower is active (every 30s) to compensate for
        // potentially dropped MQTT event subscriptions
        CleanMode currentMode = lastCleanMode;
        if (currentMode != null && (currentMode.isActive() || currentMode == CleanMode.RETURNING)) {
            scheduleNextPoll(30);
        } else {
            scheduleNextPoll(-1);
        }
        logger.debug("{}: Mower data polling completed", serialNumber);
    }

    private <T> @Nullable T pollCommand(EcovacsDevice device, IotDeviceCommand<T> command, String name) {
        try {
            return device.sendCommand(command);
        } catch (EcovacsApiException e) {
            logger.debug("{}: Could not get {}: {}", serialNumber, name, e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // --- Channel management ---

    private void removeUnsupportedChannels(EcovacsDevice device) {
        var builder = editThing();
        boolean hasChanges = false;

        if (!device.hasCapability(DeviceCapability.CUTTING_HEIGHT)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_CUTTING_HEIGHT);
        }
        if (!device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_VOICE_VOLUME);
        }
        if (!device.hasCapability(DeviceCapability.TRUE_DETECT_3D)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_TRUE_DETECT_3D);
        }
        if (!device.hasCapability(DeviceCapability.SAFE_PROTECT)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_SAFE_PROTECT);
        }
        if (!device.hasCapability(DeviceCapability.CHILD_LOCK)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_CHILD_LOCK);
        }
        if (!device.hasCapability(DeviceCapability.RAIN_DETECTION)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_RAIN_DELAY);
        }
        if (!device.hasCapability(DeviceCapability.MOVEUP_WARNING)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_MOVEUP_WARNING);
        }
        if (!device.hasCapability(DeviceCapability.READ_NETWORK_INFO)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_WIFI_RSSI);
        }

        if (hasChanges) {
            updateThing(builder.build());
        }
    }
}
