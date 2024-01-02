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
package org.openhab.binding.ecovacs.internal.handler;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.EcovacsDynamicStateDescriptionProvider;
import org.openhab.binding.ecovacs.internal.action.EcovacsVacuumActions;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
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
import org.openhab.binding.ecovacs.internal.api.commands.SetContinuousCleaningCommand;
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
import org.openhab.binding.ecovacs.internal.api.util.SchedulerTask;
import org.openhab.binding.ecovacs.internal.config.EcovacsVacuumConfiguration;
import org.openhab.binding.ecovacs.internal.util.StateOptionEntry;
import org.openhab.binding.ecovacs.internal.util.StateOptionMapping;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcovacsVacuumHandler} is responsible for handling data and commands from/to vacuum cleaners.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsVacuumHandler extends BaseThingHandler implements EcovacsDevice.EventListener {

    private final Logger logger = LoggerFactory.getLogger(EcovacsVacuumHandler.class);

    private final TranslationProvider i18Provider;
    private final LocaleProvider localeProvider;
    private final EcovacsDynamicStateDescriptionProvider stateDescriptionProvider;
    private final Bundle bundle;

    private final SchedulerTask initTask;
    private final SchedulerTask reconnectTask;
    private final SchedulerTask pollTask;
    private @Nullable EcovacsDevice device;

    private @Nullable Boolean lastWasCharging;
    private @Nullable CleanMode lastCleanMode;
    private @Nullable CleanMode lastActiveCleanMode;
    private Optional<String> lastDownloadedCleanMapUrl = Optional.empty();
    private long lastSuccessfulPollTimestamp;
    private int lastDefaultCleaningPasses = 1;
    private String serialNumber = "<unset>";

    public EcovacsVacuumHandler(Thing thing, TranslationProvider i18Provider, LocaleProvider localeProvider,
            EcovacsDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.i18Provider = i18Provider;
        this.localeProvider = localeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        bundle = FrameworkUtil.getBundle(getClass());

        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        reconnectTask = new SchedulerTask(scheduler, logger, "Connection", this::connectToDevice);
        pollTask = new SchedulerTask(scheduler, logger, "Poll", this::pollData);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EcovacsVacuumActions.class);
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
                lastDefaultCleaningPasses = passes; // if we get here, the command was executed successfully
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
    public void initialize() {
        serialNumber = getConfigAs(EcovacsVacuumConfiguration.class).serialNumber;
        if (serialNumber.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.config-error-no-serial");
        } else {
            logger.debug("{}: Initializing handler", serialNumber);
            updateStatus(ThingStatus.UNKNOWN);
            initTask.setNamePrefix(serialNumber);
            reconnectTask.setNamePrefix(serialNumber);
            pollTask.setNamePrefix(serialNumber);
            initTask.submit();
        }
    }

    @Override
    public void dispose() {
        logger.debug("{}: Disposing handler", serialNumber);
        teardown(false);
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
                    scheduleNextPoll(5); // add some delay in case multiple channels are linked at once
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Fetching initial data for channel {} failed", serialNumber, channelUID.getId(), e);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("{}: Bridge status changed to {}", serialNumber, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initTask.submit();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            teardown(false);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void onBatteryLevelUpdated(EcovacsDevice device, int newLevelPercent) {
        // Some devices report weird values (> 100%), so better clamp to supported range
        int actualPercent = Math.max(0, Math.min(newLevelPercent, 100));
        updateState(CHANNEL_ID_BATTERY_LEVEL, new DecimalType(actualPercent));
    }

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
                // Map indices back to letters as shown in the app
                def = Arrays.stream(def.split(",")).map(item -> {
                    try {
                        int index = Integer.parseInt(item);
                        return String.valueOf((char) ('A' + index));
                    } catch (NumberFormatException e) {
                        return item;
                    }
                }).collect(Collectors.joining(";"));
            } else if (newMode == CleanMode.CUSTOM_AREA) {
                // Map the separator from comma to semicolon to allow using the output as command input
                def = def.replace(',', ';');
            }
            return new StringType(def);
        });
        updateState(CHANNEL_ID_CLEANING_SPOT_DEFINITION, areaDefState.orElse(UnDefType.UNDEF));
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
    public void onEventStreamFailure(final EcovacsDevice device, Throwable error) {
        logger.debug("{}: Device connection failed, reconnecting", serialNumber, error);
        teardownAndScheduleReconnection();
    }

    @Override
    public void onFirmwareVersionChanged(EcovacsDevice device, String fwVersion) {
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fwVersion);
    }

    public void playSound(PlaySoundCommand command) {
        doWithDevice(device -> {
            if (device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
                device.sendCommand(command);
            } else {
                logger.info("{}: Device does not support voice reporting, ignoring sound action", serialNumber);
            }
        });
    }

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

    private void removeUnsupportedChannels(EcovacsDevice device) {
        ThingBuilder builder = editThing();
        boolean hasChanges = false;

        if (!device.hasCapability(DeviceCapability.MOPPING_SYSTEM)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_WATER_AMOUNT);
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_WATER_PLATE_PRESENT);
        }
        if (!device.hasCapability(DeviceCapability.CLEAN_SPEED_CONTROL)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_SUCTION_POWER);
        }
        if (!device.hasCapability(DeviceCapability.MAIN_BRUSH)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_MAIN_BRUSH_LIFETIME);
        }
        if (!device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_VOICE_VOLUME);
        }
        if (!device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_LAST_CLEAN_MODE);
        }
        if (!device.hasCapability(DeviceCapability.MAPPING)
                || !device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_LAST_CLEAN_MAP);
        }
        if (!device.hasCapability(DeviceCapability.READ_NETWORK_INFO)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_WIFI_RSSI);
        }
        if (!device.hasCapability(DeviceCapability.AUTO_EMPTY_STATION)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_AUTO_EMPTY);
        }
        if (!device.hasCapability(DeviceCapability.TRUE_DETECT_3D)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_TRUE_DETECT_3D);
        }
        if (!device.hasCapability(DeviceCapability.DEFAULT_CLEAN_COUNT_SETTING)) {
            hasChanges |= removeUnsupportedChannel(builder, CHANNEL_ID_CLEANING_PASSES);
        }

        if (hasChanges) {
            updateThing(builder.build());
        }
    }

    private boolean removeUnsupportedChannel(ThingBuilder builder, String channelId) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        if (getThing().getChannel(channelUID) == null) {
            return false;
        }
        logger.debug("{}: Removing unsupported channel {}", serialNumber, channelId);
        builder.withoutChannel(channelUID);
        return true;
    }

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
        return Arrays.stream(values).map(v -> Optional.ofNullable(mapping.get(v)))
                // ensure we have a mapping (should always be the case)
                .filter(Optional::isPresent).map(opt -> opt.get())
                // apply supplied filter
                .filter(mv -> filter == null || filter.test(mv))
                // apply capability filter
                .filter(mv -> mv.capability.isEmpty() || device.hasCapability(mv.capability.get()))
                // map to actual option
                .map(mv -> new StateOption(mv.value, mv.value)).collect(Collectors.toList());
    }

    private synchronized void scheduleNextPoll(long initialDelaySeconds) {
        final EcovacsVacuumConfiguration config = getConfigAs(EcovacsVacuumConfiguration.class);
        final long delayUntilNextPoll;
        if (initialDelaySeconds < 0) {
            long intervalSeconds = config.refresh * 60;
            long secondsSinceLastPoll = (System.currentTimeMillis() - lastSuccessfulPollTimestamp) / 1000;
            long deltaRemaining = intervalSeconds - secondsSinceLastPoll;
            delayUntilNextPoll = Math.max(0, deltaRemaining);
        } else {
            delayUntilNextPoll = initialDelaySeconds;
        }
        logger.debug("{}: Scheduling next poll in {}s, refresh interval {}min", serialNumber, delayUntilNextPoll,
                config.refresh);
        pollTask.cancel();
        pollTask.schedule(delayUntilNextPoll);
    }

    private void initDevice() {
        final EcovacsApiHandler handler = getApiHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        try {
            final EcovacsApi api = handler.createApiForDevice(serialNumber);
            api.loginAndGetAccessToken();
            Optional<EcovacsDevice> deviceOpt = api.getDevices().stream()
                    .filter(d -> serialNumber.equals(d.getSerialNumber())).findFirst();
            if (deviceOpt.isPresent()) {
                EcovacsDevice device = deviceOpt.get();
                this.device = device;
                updateProperty(Thing.PROPERTY_MODEL_ID, device.getModelName());
                updateProperty(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
                updateStateOptions(device);
                removeUnsupportedChannels(device);
                connectToDevice();
            } else {
                logger.info("{}: Device not found in device list, setting offline", serialNumber);
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        } catch (EcovacsApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void teardownAndScheduleReconnection() {
        teardown(true);
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        EcovacsDevice device = this.device;
        if (device != null) {
            device.disconnect(scheduler);
        }

        pollTask.cancel();

        reconnectTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            SchedulerTask connectTask = device != null ? reconnectTask : initTask;
            connectTask.schedule(5);
        }
    }

    private void connectToDevice() {
        doWithDevice(device -> {
            device.connect(this, scheduler);
            fetchInitialBatteryStatus(device);
            fetchInitialStateAndCommandValues(device);
            fetchInitialWaterSystemPresentState(device); // nop if unsupported
            fetchInitialErrorCode(device);
            scheduleNextPoll(-1);
            logger.debug("{}: Device connected", serialNumber);
            updateStatus(ThingStatus.ONLINE);
        });
    }

    private void pollData() {
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

                updateState(CHANNEL_ID_LAST_CLEAN_START,
                        new DateTimeType(record.timestamp.toInstant().atZone(ZoneId.systemDefault())));
                updateState(CHANNEL_ID_LAST_CLEAN_DURATION, new QuantityType<>(record.cleaningDuration, Units.SECOND));
                updateState(CHANNEL_ID_LAST_CLEAN_AREA, new QuantityType<>(record.cleanedArea, SIUnits.SQUARE_METRE));
                if (device.hasCapability(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD)) {
                    StateOptionEntry<CleanMode> mode = CLEAN_MODE_MAPPING.get(record.mode);
                    updateState(CHANNEL_ID_LAST_CLEAN_MODE, stringToState(mode != null ? mode.value : null));

                    if (device.hasCapability(DeviceCapability.MAPPING)
                            && !lastDownloadedCleanMapUrl.equals(record.mapImageUrl)) {
                        updateState(CHANNEL_ID_LAST_CLEAN_MAP, record.mapImageUrl.flatMap(url -> {
                            // HttpUtil expects the server to return the correct MIME type, but Ecovacs' server sends
                            // 'application/octet-stream', so we have to set the correct MIME type by ourselves
                            @Nullable
                            RawType mapData = HttpUtil.downloadData(url, null, false, -1);
                            if (mapData != null) {
                                mapData = new RawType(mapData.getBytes(), "image/png");
                                lastDownloadedCleanMapUrl = record.mapImageUrl;
                            } else {
                                logger.debug("{}: Downloading cleaning map {} failed", serialNumber, url);
                            }
                            return Optional.ofNullable((State) mapData);
                        }).orElse(UnDefType.NULL));
                    }
                }
            }

            if (device.hasCapability(DeviceCapability.CLEAN_SPEED_CONTROL)) {
                SuctionPower power = device.sendCommand(new GetSuctionPowerCommand());
                updateState(CHANNEL_ID_SUCTION_POWER, new StringType(SUCTION_POWER_MAPPING.getMappedValue(power)));
            }

            if (device.hasCapability(DeviceCapability.MOPPING_SYSTEM)) {
                MoppingWaterAmount waterAmount = device.sendCommand(new GetMoppingWaterAmountCommand());
                updateState(CHANNEL_ID_WATER_AMOUNT, new StringType(WATER_AMOUNT_MAPPING.getMappedValue(waterAmount)));
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
            if (device.hasCapability(DeviceCapability.VOICE_REPORTING)) {
                int level = device.sendCommand(new GetVolumeCommand());
                updateState(CHANNEL_ID_VOICE_VOLUME, new PercentType(level * 10));
            }

            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);
        });
        logger.debug("{}: Data polling completed", serialNumber);
    }

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
            // Some devices already report charging state while returning to charging station, make sure to not report
            // charging in that case. The same applies for models with pad washing/drying station, as those states imply
            // the device being charging.
            if (cleanMode != CleanMode.RETURNING && cleanMode != CleanMode.WASHING && cleanMode != CleanMode.DRYING) {
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
        switch (cleanMode) {
            case AUTO:
                return CMD_AUTO_CLEAN;
            case SPOT_AREA:
                return CMD_SPOT_AREA;
            case PAUSE:
                return CMD_PAUSE;
            case STOP:
                return CMD_STOP;
            case RETURNING:
                return CMD_CHARGE;
            default:
                break;
        }
        return null;
    }

    private State stringToState(@Nullable String value) {
        Optional<State> stateOpt = Optional.ofNullable(value).map(v -> StringType.valueOf(v));
        return stateOpt.orElse(UnDefType.UNDEF);
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
                    // We let the user pass in letters as in Ecovacs' app, but the API wants indices
                    if (id.length() == 1 && id.charAt(0) >= 'A' && id.charAt(0) <= 'Z') {
                        roomIds.add(String.valueOf(id.charAt(0) - 'A'));
                    } else {
                        logger.info("{}: Found invalid spot area room ID '{}', ignoring.", serialNumber, id);
                    }
                }
                if (!roomIds.isEmpty()) {
                    return new SpotAreaCleaningCommand(roomIds, passes);
                }
            } else {
                logger.info("{}: spotArea command needs to have the form spotArea:<room1>[;<room2>][;<...roomX>][:x2]",
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
            logger.info("{}: customArea command needs to have the form customArea:<x1>;<y1>;<x2>;<y2>[:x2]",
                    serialNumber);
        }

        return null;
    }

    private interface WithDeviceAction {
        void run(EcovacsDevice device) throws EcovacsApiException, InterruptedException;
    }

    private void doWithDevice(WithDeviceAction action) {
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
                // Drop our device instance to make sure we run a full init cycle,
                // including an API re-login, on reconnection
                device.disconnect(scheduler);
                this.device = null;
            }
            teardownAndScheduleReconnection();
        }
    }

    private @Nullable EcovacsApiHandler getApiHandler() {
        final Bridge bridge = getBridge();
        return bridge != null ? (EcovacsApiHandler) bridge.getHandler() : null;
    }
}
