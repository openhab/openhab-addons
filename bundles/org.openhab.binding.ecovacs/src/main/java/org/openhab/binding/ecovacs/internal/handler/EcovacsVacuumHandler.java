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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.EcovacsDynamicStateDescriptionProvider;
import org.openhab.binding.ecovacs.internal.action.EcovacsVacuumActions;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.commands.AbstractNoResponseCommand;
import org.openhab.binding.ecovacs.internal.api.commands.CustomAreaCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.EmptyDustbinCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetBatteryInfoCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetChargeStateCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetCleanStateCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetComponentLifeSpanCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetContinuousCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetCustomMoppingWaterAmountCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetDefaultCleanPassesCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetDustbinAutoEmptyCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetErrorCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetMoppingWaterAmountCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetNetworkInfoCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetSuctionPowerCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetTotalStatsCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetTotalStatsCommand.TotalStats;
import org.openhab.binding.ecovacs.internal.api.commands.GetTrueDetectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetVolumeCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GetWaterSystemPresentCommand;
import org.openhab.binding.ecovacs.internal.api.commands.GoChargingCommand;
import org.openhab.binding.ecovacs.internal.api.commands.PauseCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.PlaySoundCommand;
import org.openhab.binding.ecovacs.internal.api.commands.ResumeCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SceneCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetContinuousCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetCustomMoppingWaterAmountCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetDefaultCleanPassesCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetDustbinAutoEmptyCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetMoppingWaterAmountCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetSuctionPowerCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetTrueDetectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetVolumeCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SpotAreaCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.StartAutoCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.commands.StopCleaningCommand;
import org.openhab.binding.ecovacs.internal.api.model.ChargeMode;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.Component;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.model.MoppingWaterAmount;
import org.openhab.binding.ecovacs.internal.api.model.NetworkInfo;
import org.openhab.binding.ecovacs.internal.api.model.SuctionPower;
import org.openhab.binding.ecovacs.internal.util.StateOptionEntry;
import org.openhab.binding.ecovacs.internal.util.StateOptionMapping;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EcovacsVacuumHandler} is responsible for handling data and commands from/to vacuum cleaners.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsVacuumHandler extends AbstractEcovacsDeviceHandler implements EcovacsDevice.EventListener {

    private final EcovacsDynamicStateDescriptionProvider stateDescriptionProvider;

    private @Nullable CleanMode lastActiveCleanMode;
    private Optional<String> lastDownloadedCleanMapUrl = Optional.empty();
    private int lastDefaultCleaningPasses = 1;

    public EcovacsVacuumHandler(Thing thing, TranslationProvider i18Provider, LocaleProvider localeProvider,
            EcovacsDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing, i18Provider, localeProvider);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected String getLogPrefix() {
        return "vacuum";
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcovacsVacuumActions.class);
    }

    @Override
    protected void afterDeviceFound(EcovacsDevice device) {
        updateStateOptions(device);
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
                AbstractNoResponseCommand cmd = determineDeviceCommand(device, command.toString());
                if (cmd != null) {
                    device.sendCommand(cmd);
                    return;
                }
            } else if (channel.equals(CHANNEL_ID_VOICE_VOLUME) && command instanceof DecimalType volume) {
                int volumePercent = volume.intValue();
                device.sendCommand(new SetVolumeCommand((volumePercent + 5) / 10));
                return;
            } else if (channel.equals(CHANNEL_ID_SUCTION_POWER) && command instanceof StringType) {
                Optional<SuctionPower> power = SUCTION_POWER_MAPPING.findMappedEnumValue(command.toString());
                if (power.isPresent()) {
                    device.sendCommand(new SetSuctionPowerCommand(power.get()));
                    return;
                }
            } else if (channel.equals(CHANNEL_ID_WATER_AMOUNT) && command instanceof StringType) {
                Optional<MoppingWaterAmount> amount = WATER_AMOUNT_MAPPING.findMappedEnumValue(command.toString());
                if (amount.isPresent()) {
                    device.sendCommand(new SetMoppingWaterAmountCommand(amount.get()));
                    return;
                }
            } else if (channel.equals(CHANNEL_ID_WATER_AMOUNT_PERCENT) && command instanceof PercentType percent) {
                device.sendCommand(new SetCustomMoppingWaterAmountCommand(percent.intValue()));
                return;
            } else if (channel.equals(CHANNEL_ID_AUTO_EMPTY)) {
                if (command instanceof OnOffType) {
                    device.sendCommand(new SetDustbinAutoEmptyCommand(command == OnOffType.ON));
                    return;
                } else if (command instanceof StringType && "trigger".equals(command.toString())) {
                    device.sendCommand(new EmptyDustbinCommand());
                    return;
                }
            } else if (channel.equals(CHANNEL_ID_TRUE_DETECT_3D) && command instanceof OnOffType) {
                device.sendCommand(new SetTrueDetectCommand(command == OnOffType.ON));
                return;
            } else if (channel.equals(CHANNEL_ID_CONTINUOUS_CLEANING) && command instanceof OnOffType) {
                device.sendCommand(new SetContinuousCleaningCommand(command == OnOffType.ON));
                return;
            } else if (channel.equals(CHANNEL_ID_CLEANING_PASSES) && command instanceof DecimalType type) {
                int passes = type.intValue();
                device.sendCommand(new SetDefaultCleanPassesCommand(passes));
                lastDefaultCleaningPasses = passes;
                return;
            }
            logger.debug("{}: Ignoring unsupported device command {} for channel {}", serialNumber, command, channel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Handling device command {} failed", serialNumber, command, e);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        EcovacsDevice device = this.device;
        if (device == null) {
            return;
        }

        try {
            switch (channelUID.getId()) {
                case CHANNEL_ID_BATTERY_LEVEL:
                    fetchInitialBatteryStatus(device);
                    break;
                case CHANNEL_ID_STATE:
                case CHANNEL_ID_COMMAND:
                case CHANNEL_ID_CLEANING_MODE:
                    fetchInitialStateAndCommandValues(device);
                    break;
                case CHANNEL_ID_WATER_PLATE_PRESENT:
                    fetchInitialWaterSystemPresentState(device);
                    break;
                case CHANNEL_ID_ERROR_CODE:
                case CHANNEL_ID_ERROR_DESCRIPTION:
                    fetchInitialErrorCode(device);
                default:
                    scheduleNextPoll(5);
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Fetching initial data for channel {} failed", serialNumber, channelUID.getId(), e);
        }
    }

    // --- EcovacsDevice.EventListener callbacks ---

    @Override
    public void onChargingStateUpdated(EcovacsDevice device, boolean charging) {
        lastWasCharging = charging;
        updateStateAndCommandChannels();
    }

    @Override
    public void onCleaningModeUpdated(EcovacsDevice device, CleanMode newMode, Optional<String> areaDefinition) {
        lastCleanMode = newMode;
        if (newMode.isActive()) {
            lastActiveCleanMode = newMode;
        } else if (newMode.isIdle()) {
            lastActiveCleanMode = null;
        }
        updateStateAndCommandChannels();
        Optional<State> areaDefState = areaDefinition.map(def -> {
            if (newMode == CleanMode.SPOT_AREA) {
                def = Arrays.stream(def.split(",")).map(item -> {
                    try {
                        int index = Integer.parseInt(item);
                        return String.valueOf((char) ('A' + index));
                    } catch (NumberFormatException e) {
                        return item;
                    }
                }).collect(Collectors.joining(";"));
            } else if (newMode == CleanMode.CUSTOM_AREA) {
                def = def.replace(',', ';');
            }
            return new StringType(def);
        });
        updateState(CHANNEL_ID_CLEANING_SPOT_DEFINITION, Objects.requireNonNull(areaDefState.orElse(UnDefType.UNDEF)));
        if (newMode == CleanMode.RETURNING) {
            scheduleNextPoll(30);
        } else if (newMode.isIdle()) {
            updateState(CHANNEL_ID_CLEANED_AREA, UnDefType.UNDEF);
            updateState(CHANNEL_ID_CLEANING_TIME, UnDefType.UNDEF);
        }
    }

    @Override
    public void onCleaningStatsUpdated(EcovacsDevice device, int cleanedArea, int cleaningTimeSeconds) {
        updateState(CHANNEL_ID_CLEANED_AREA, new QuantityType<>(cleanedArea, SIUnits.SQUARE_METRE));
        updateState(CHANNEL_ID_CLEANING_TIME, new QuantityType<>(cleaningTimeSeconds, Units.SECOND));
    }

    @Override
    public void onMowingStatsUpdated(EcovacsDevice device, int mowedAreaSqCm, int timeSeconds) {
    }

    @Override
    public void onWaterSystemPresentUpdated(EcovacsDevice device, boolean present) {
        updateState(CHANNEL_ID_WATER_PLATE_PRESENT, OnOffType.from(present));
    }

    @Override
    public void onErrorReported(EcovacsDevice device, int errorCode) {
        updateState(CHANNEL_ID_ERROR_CODE, new DecimalType(errorCode));
        final Locale locale = localeProvider.getLocale();
        String errorDesc = i18Provider.getText(bundle, "ecovacs.vacuum.error-code." + errorCode, null, locale);
        if (errorDesc == null) {
            errorDesc = i18Provider.getText(bundle, "ecovacs.vacuum.error-code.unknown", "", locale, errorCode);
        }
        updateState(CHANNEL_ID_ERROR_DESCRIPTION, new StringType(errorDesc));
    }

    @Override
    public void onMowingSessionFinished(EcovacsDevice device, long startTimestamp, int durationSeconds, int areaSqCm) {
    }

    // --- playSound action ---

    public void playSound(PlaySoundCommand command) {
        doWithDevice(device -> {
            if (device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
                device.sendCommand(command);
            } else {
                logger.info("{}: Device does not support voice reporting, ignoring sound action", serialNumber);
            }
        });
    }

    // --- doWithDevice override with auth failure handling ---

    @Override
    protected void doWithDevice(DeviceAction action) {
        EcovacsDevice device = this.device;
        if (device == null) {
            return;
        }
        try {
            action.run(device);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Failed communicating to device, reconnecting", serialNumber, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            if (e.isAuthFailure) {
                EcovacsApiHandler apiHandler = getApiHandler();
                if (apiHandler != null) {
                    apiHandler.onLoginExpired();
                }
                device.disconnect(scheduler);
                this.device = null;
            }
            teardownAndScheduleReconnection();
        }
    }

    // --- Connectivity ---

    @Override
    protected void connectToDevice() {
        doWithDevice(device -> {
            device.connect(this, scheduler);
            fetchInitialBatteryStatus(device);
            fetchInitialStateAndCommandValues(device);
            fetchInitialWaterSystemPresentState(device);
            fetchInitialErrorCode(device);
            scheduleNextPoll(-1);
            logger.debug("{}: Device connected", serialNumber);
            updateStatus(ThingStatus.ONLINE);
        });
    }

    // --- Fetch methods ---

    private void fetchInitialBatteryStatus(EcovacsDevice device) throws EcovacsApiException, InterruptedException {
        Integer batteryPercent = device.sendCommand(new GetBatteryInfoCommand());
        onBatteryLevelUpdated(device, batteryPercent);
    }

    private void fetchInitialStateAndCommandValues(EcovacsDevice device)
            throws EcovacsApiException, InterruptedException {
        lastWasCharging = device.sendCommand(new GetChargeStateCommand()) == ChargeMode.CHARGING;
        CleanMode mode = device.sendCommand(new GetCleanStateCommand());
        if (mode.isActive()) {
            lastActiveCleanMode = mode;
        }
        lastCleanMode = mode;
        updateStateAndCommandChannels();
    }

    private void fetchInitialWaterSystemPresentState(EcovacsDevice device)
            throws EcovacsApiException, InterruptedException {
        if (!device.hasCapability(DeviceCapability.MOPPING_SYSTEM)) {
            return;
        }
        boolean present = device.sendCommand(new GetWaterSystemPresentCommand());
        onWaterSystemPresentUpdated(device, present);
    }

    private void fetchInitialErrorCode(EcovacsDevice device) throws EcovacsApiException, InterruptedException {
        Optional<Integer> errorOpt = device.sendCommand(new GetErrorCommand());
        if (errorOpt.isPresent()) {
            onErrorReported(device, errorOpt.get());
        }
    }

    // --- Polling ---

    @Override
    protected void pollData() {
        logger.debug("{}: Polling data", serialNumber);
        doWithDevice(device -> {
            TotalStats totalStats = device.sendCommand(new GetTotalStatsCommand());
            updateState(CHANNEL_ID_TOTAL_CLEANED_AREA, new QuantityType<>(totalStats.totalArea, SIUnits.SQUARE_METRE));
            updateState(CHANNEL_ID_TOTAL_CLEANING_TIME, new QuantityType<>(totalStats.totalRuntime, Units.SECOND));
            updateState(CHANNEL_ID_TOTAL_CLEAN_RUNS, new DecimalType(totalStats.cleanRuns));

            boolean continuousCleaningEnabled = device.sendCommand(new GetContinuousCleaningCommand());
            updateState(CHANNEL_ID_CONTINUOUS_CLEANING, OnOffType.from(continuousCleaningEnabled));

            List<CleanLogRecord> cleanLogRecords = device.getCleanLogs();
            if (!cleanLogRecords.isEmpty()) {
                CleanLogRecord record = cleanLogRecords.get(0);

                updateState(CHANNEL_ID_LAST_CLEAN_START, new DateTimeType(record.timestamp.toInstant()));
                updateState(CHANNEL_ID_LAST_CLEAN_DURATION, new QuantityType<>(record.cleaningDuration, Units.SECOND));
                updateState(CHANNEL_ID_LAST_CLEAN_AREA, new QuantityType<>(record.cleanedArea, SIUnits.SQUARE_METRE));
                if (device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
                    StateOptionEntry<CleanMode> mode = CLEAN_MODE_MAPPING.get(record.mode);
                    updateState(CHANNEL_ID_LAST_CLEAN_MODE, stringToState(mode != null ? mode.value : null));

                    if (device.hasCapability(DeviceCapability.MAPPING)
                            && !lastDownloadedCleanMapUrl.equals(record.mapImageUrl)) {
                        Optional<State> content = device.downloadCleanMapImage(record).map(bytes -> {
                            lastDownloadedCleanMapUrl = record.mapImageUrl;
                            return new RawType(bytes, "image/png");
                        });
                        updateState(CHANNEL_ID_LAST_CLEAN_MAP, Objects.requireNonNull(content.orElse(UnDefType.NULL)));
                    }
                }
            }

            if (device.hasCapability(DeviceCapability.CLEAN_SPEED_CONTROL)) {
                SuctionPower power = device.sendCommand(new GetSuctionPowerCommand());
                updateState(CHANNEL_ID_SUCTION_POWER, new StringType(SUCTION_POWER_MAPPING.getMappedValue(power)));
            }

            if (device.hasCapability(DeviceCapability.MOPPING_SYSTEM)) {
                if (device.hasCapability(DeviceCapability.CUSTOM_WATER_AMOUNT)) {
                    Integer waterAmount = device.sendCommand(new GetCustomMoppingWaterAmountCommand());
                    updateState(CHANNEL_ID_WATER_AMOUNT_PERCENT, new PercentType(waterAmount));
                } else {
                    MoppingWaterAmount waterAmount = device.sendCommand(new GetMoppingWaterAmountCommand());
                    updateState(CHANNEL_ID_WATER_AMOUNT,
                            new StringType(WATER_AMOUNT_MAPPING.getMappedValue(waterAmount)));
                }
            }

            if (device.hasCapability(DeviceCapability.READ_NETWORK_INFO)) {
                NetworkInfo netInfo = device.sendCommand(new GetNetworkInfoCommand());
                if (netInfo.wifiRssi != 0) {
                    updateState(CHANNEL_ID_WIFI_RSSI, new QuantityType<>(netInfo.wifiRssi, Units.DECIBEL_MILLIWATTS));
                }
            }

            if (device.hasCapability(DeviceCapability.AUTO_EMPTY_STATION)) {
                boolean autoEmptyEnabled = device.sendCommand(new GetDustbinAutoEmptyCommand());
                updateState(CHANNEL_ID_AUTO_EMPTY, OnOffType.from(autoEmptyEnabled));
            }
            if (device.hasCapability(DeviceCapability.TRUE_DETECT_3D)) {
                boolean trueDetectEnabled = device.sendCommand(new GetTrueDetectCommand());
                updateState(CHANNEL_ID_TRUE_DETECT_3D, OnOffType.from(trueDetectEnabled));
            }
            if (device.hasCapability(DeviceCapability.DEFAULT_CLEAN_COUNT_SETTING)) {
                lastDefaultCleaningPasses = device.sendCommand(new GetDefaultCleanPassesCommand());
                updateState(CHANNEL_ID_CLEANING_PASSES, new DecimalType(lastDefaultCleaningPasses));
            }

            int sideBrushPercent = device.sendCommand(new GetComponentLifeSpanCommand(Component.SIDE_BRUSH));
            updateState(CHANNEL_ID_SIDE_BRUSH_LIFETIME, new QuantityType<>(sideBrushPercent, Units.PERCENT));
            int filterPercent = device.sendCommand(new GetComponentLifeSpanCommand(Component.DUST_CASE_HEAP));
            updateState(CHANNEL_ID_DUST_FILTER_LIFETIME, new QuantityType<>(filterPercent, Units.PERCENT));

            if (device.hasCapability(DeviceCapability.MAIN_BRUSH)) {
                int mainBrushPercent = device.sendCommand(new GetComponentLifeSpanCommand(Component.BRUSH));
                updateState(CHANNEL_ID_MAIN_BRUSH_LIFETIME, new QuantityType<>(mainBrushPercent, Units.PERCENT));
            }
            if (device.hasCapability(DeviceCapability.UNIT_CARE_LIFESPAN)) {
                int unitCarePercent = device.sendCommand(new GetComponentLifeSpanCommand(Component.UNIT_CARE));
                updateState(CHANNEL_ID_OTHER_COMPONENT_LIFETIME, new QuantityType<>(unitCarePercent, Units.PERCENT));
            }
            if (device.hasCapability(DeviceCapability.ROUND_MOP_LIFESPAN)) {
                int roundMopPercent = device.sendCommand(new GetComponentLifeSpanCommand(Component.ROUND_MOP));
                updateState(CHANNEL_ID_ROUND_MOP_LIFETIME, new QuantityType<>(roundMopPercent, Units.PERCENT));
            }
            if (device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
                int level = device.sendCommand(new GetVolumeCommand());
                updateState(CHANNEL_ID_VOICE_VOLUME, new PercentType(level * 10));
            }

            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);
        });
        logger.debug("{}: Data polling completed", serialNumber);
    }

    // --- State and command channel helpers ---

    private void updateStateAndCommandChannels() {
        Boolean charging = this.lastWasCharging;
        CleanMode cleanMode = this.lastCleanMode;
        if (charging == null || cleanMode == null) {
            return;
        }
        String commandState = determineCommandChannelValue(charging, cleanMode);
        String currentMode = determineCleaningModeChannelValue(cleanMode.isActive() ? cleanMode : lastActiveCleanMode);
        updateState(CHANNEL_ID_STATE, StringType.valueOf(determineStateChannelValue(charging, cleanMode)));
        updateState(CHANNEL_ID_CLEANING_MODE, stringToState(currentMode));
        updateState(CHANNEL_ID_COMMAND, stringToState(commandState));
    }

    private String determineStateChannelValue(boolean charging, CleanMode cleanMode) {
        if (charging) {
            if (cleanMode != CleanMode.RETURNING && cleanMode != CleanMode.WASHING && cleanMode != CleanMode.DRYING
                    && cleanMode != CleanMode.EMPTYING) {
                return "charging";
            }
        }
        if (cleanMode.isActive()) {
            return "cleaning";
        }
        StateOptionEntry<CleanMode> result = CLEAN_MODE_MAPPING.get(cleanMode);
        return result != null ? result.value : "idle";
    }

    private @Nullable String determineCleaningModeChannelValue(@Nullable CleanMode activeCleanMode) {
        StateOptionEntry<CleanMode> result = activeCleanMode != null ? CLEAN_MODE_MAPPING.get(activeCleanMode) : null;
        return result != null ? result.value : null;
    }

    private @Nullable String determineCommandChannelValue(boolean charging, CleanMode cleanMode) {
        if (charging) {
            return CMD_CHARGE;
        }
        return switch (cleanMode) {
            case AUTO -> CMD_AUTO_CLEAN;
            case SPOT_AREA -> CMD_SPOT_AREA;
            case SCENE_CLEAN -> CMD_SCENE_CLEAN;
            case PAUSE -> CMD_PAUSE;
            case STOP -> CMD_STOP;
            case RETURNING -> CMD_CHARGE;
            default -> null;
        };
    }

    private State stringToState(@Nullable String value) {
        Optional<State> stateOpt = Optional.ofNullable(value).map(v -> StringType.valueOf(v));
        return Objects.requireNonNull(stateOpt.orElse(UnDefType.UNDEF));
    }

    private @Nullable AbstractNoResponseCommand determineDeviceCommand(EcovacsDevice device, String command) {
        CleanMode mode = lastActiveCleanMode;

        switch (command) {
            case CMD_AUTO_CLEAN:
                return new StartAutoCleaningCommand();
            case CMD_PAUSE:
                if (mode != null) {
                    return new PauseCleaningCommand(mode);
                }
                break;
            case CMD_RESUME:
                if (mode != null) {
                    return new ResumeCleaningCommand(mode);
                }
                break;
            case CMD_STOP:
                return new StopCleaningCommand();
            case CMD_CHARGE:
                return new GoChargingCommand();
        }

        if (command.startsWith(CMD_SPOT_AREA) && device.hasCapability(DeviceCapability.SPOT_AREA_CLEANING)) {
            String[] splitted = command.split(":");
            if (splitted.length == 2 || splitted.length == 3) {
                int passes = splitted.length == 3 && "x2".equals(splitted[2]) ? 2 : lastDefaultCleaningPasses;
                List<String> roomIds = new ArrayList<>();
                for (String id : splitted[1].split(";")) {
                    if (id.length() == 1 && id.charAt(0) >= 'A' && id.charAt(0) <= 'Z') {
                        roomIds.add(String.valueOf(id.charAt(0) - 'A'));
                    } else {
                        logger.info("{}: Found invalid spot area room ID '{}', ignoring.", serialNumber, id);
                    }
                }
                if (!roomIds.isEmpty()) {
                    return new SpotAreaCleaningCommand(roomIds, passes,
                            device.hasCapability(DeviceCapability.FREE_CLEAN_FOR_SPOT_AREA));
                }
            } else {
                logger.warn("{}: spotArea command needs to have the form spotArea:<room1>[;<room2>][;<...roomX>][:x2]",
                        serialNumber);
            }
        }
        if (command.startsWith(CMD_CUSTOM_AREA) && device.hasCapability(DeviceCapability.CUSTOM_AREA_CLEANING)) {
            String[] splitted = command.split(":");
            if (splitted.length == 2 || splitted.length == 3) {
                String coords = splitted[1];
                int passes = splitted.length == 3 && "x2".equals(splitted[2]) ? 2 : lastDefaultCleaningPasses;
                String[] splittedAreaDef = coords.split(";");
                if (splittedAreaDef.length == 4) {
                    return new CustomAreaCleaningCommand(String.join(",", splittedAreaDef), passes);
                }
            }
            logger.warn("{}: customArea command needs to have the form customArea:<x1>;<y1>;<x2>;<y2>[:x2]",
                    serialNumber);
        }
        if (command.startsWith(CMD_SCENE_CLEAN) && device.hasCapability(DeviceCapability.SCENARIO_CLEANING)) {
            String[] splitted = command.split(":");
            if (splitted.length == 2) {
                String scenarioId = splitted[1];
                return new SceneCleaningCommand(scenarioId, 1);
            }
            logger.warn("{}: {} command needs to have the form {}:<scenarioId>", serialNumber, CMD_SCENE_CLEAN,
                    CMD_SCENE_CLEAN);
        }

        return null;
    }

    // --- State options ---

    private void updateStateOptions(EcovacsDevice device) {
        List<StateOption> modeChannelOptions = createChannelOptions(device, CleanMode.values(), CLEAN_MODE_MAPPING,
                m -> m.enumValue.isActive());
        ThingUID thingUID = getThing().getUID();

        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, CHANNEL_ID_CLEANING_MODE),
                modeChannelOptions);
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, CHANNEL_ID_LAST_CLEAN_MODE),
                modeChannelOptions);
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, CHANNEL_ID_SUCTION_POWER),
                createChannelOptions(device, SuctionPower.values(), SUCTION_POWER_MAPPING, null));
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, CHANNEL_ID_WATER_AMOUNT),
                createChannelOptions(device, MoppingWaterAmount.values(), WATER_AMOUNT_MAPPING, null));
    }

    private <T extends Enum<T>> List<StateOption> createChannelOptions(EcovacsDevice device, T[] values,
            StateOptionMapping<T> mapping, @Nullable Predicate<StateOptionEntry<T>> filter) {
        return Arrays.stream(values).map(v -> Optional.ofNullable(mapping.get(v))).filter(Optional::isPresent)
                .map(opt -> opt.get()).filter(mv -> filter == null || filter.test(mv))
                .filter(mv -> mv.capability.isEmpty() || device.hasCapability(mv.capability.get()))
                .map(mv -> new StateOption(mv.value, mv.value)).collect(Collectors.toList());
    }

    // --- Channel management ---

    private void removeUnsupportedChannels(EcovacsDevice device) {
        ThingBuilder builder = editThing();
        boolean hasChanges = false;

        if (!device.hasCapability(DeviceCapability.MOPPING_SYSTEM)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_WATER_AMOUNT);
            hasChanges |= removeChannel(builder, CHANNEL_ID_WATER_AMOUNT_PERCENT);
            hasChanges |= removeChannel(builder, CHANNEL_ID_WATER_PLATE_PRESENT);
        } else if (device.hasCapability(DeviceCapability.CUSTOM_WATER_AMOUNT)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_WATER_AMOUNT);
        } else {
            hasChanges |= removeChannel(builder, CHANNEL_ID_WATER_AMOUNT_PERCENT);
        }
        if (!device.hasCapability(DeviceCapability.CLEAN_SPEED_CONTROL)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_SUCTION_POWER);
        }
        if (!device.hasCapability(DeviceCapability.MAIN_BRUSH)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_MAIN_BRUSH_LIFETIME);
        }
        if (!device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_VOICE_VOLUME);
        }
        if (!device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_LAST_CLEAN_MODE);
        }
        if (!device.hasCapability(DeviceCapability.MAPPING)
                || !device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_LAST_CLEAN_MAP);
        }
        if (!device.hasCapability(DeviceCapability.READ_NETWORK_INFO)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_WIFI_RSSI);
        }
        if (!device.hasCapability(DeviceCapability.AUTO_EMPTY_STATION)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_AUTO_EMPTY);
        }
        if (!device.hasCapability(DeviceCapability.TRUE_DETECT_3D)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_TRUE_DETECT_3D);
        }
        if (!device.hasCapability(DeviceCapability.DEFAULT_CLEAN_COUNT_SETTING)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_CLEANING_PASSES);
        }
        if (!device.hasCapability(DeviceCapability.UNIT_CARE_LIFESPAN)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_OTHER_COMPONENT_LIFETIME);
        }
        if (!device.hasCapability(DeviceCapability.ROUND_MOP_LIFESPAN)) {
            hasChanges |= removeChannel(builder, CHANNEL_ID_ROUND_MOP_LIFETIME);
        }

        if (hasChanges) {
            updateThing(builder.build());
        }
    }
}
