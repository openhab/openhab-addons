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
package org.openhab.binding.fronius.internal.handler;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.FroniusBatteryConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.action.FroniusBatteryActions;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl.BatterySettings;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusUnauthorizedException;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageController;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageDetails;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeResponse;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.firmware.types.SemverVersion;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusBatteryHandler} polls the GetStorageRealtimeData endpoint and
 * maps fields from the Controller node to channels and thing properties.
 * It also provides the battery settings channels and the battery control actions, which use the inverter's config API.
 *
 * @author Jimmy Tanagra - Initial contribution
 * @author Christian Jonak-Möchel - Add battery settings channels and battery control actions
 *
 * @see FroniusBatteryActions
 */
@NonNullByDefault
public class FroniusBatteryHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBatteryHandler.class);

    private @Nullable StorageController controller;
    private @Nullable FroniusBatteryConfiguration config;
    private @Nullable FroniusBatteryControl batteryControl;
    private @Nullable BatterySettings lastBatterySettings;
    private @Nullable Integer lastNightPreservationLimit;
    private @Nullable ScheduledFuture<?> batterySettingsRefreshJob;

    public FroniusBatteryHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Battery";
    }

    @Override
    public void initialize() {
        FroniusBatteryConfiguration config = this.config = getConfigAs(FroniusBatteryConfiguration.class);
        FroniusBridgeHandler bridgeHandler = getFroniusBridgeHandler();
        Bridge bridge = getBridge();
        if (bridge == null || bridgeHandler == null) {
            logger.warn("bridge ({}) or bridgeHandler ({}) is null in initialize(), this is a bug, please report it.",
                    bridge, bridgeHandler);
            return;
        }
        FroniusBridgeConfiguration bridgeConfig = bridge.getConfiguration().as(FroniusBridgeConfiguration.class);
        initializeBatteryControl(bridgeHandler, bridgeConfig.scheme, bridgeConfig.hostname, bridgeConfig.username,
                bridgeConfig.password);
        startBatterySettingsRefreshJob(config);
        super.initialize();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(FroniusBatteryActions.class);
    }

    @Override
    public void handleBridgeConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleBridgeConfigurationUpdate(configurationParameters);
        Bridge bridge = getBridge();
        FroniusBridgeHandler bridgeHandler = getFroniusBridgeHandler();
        FroniusBatteryConfiguration config = this.config;
        if (bridge == null || bridgeHandler == null) {
            logger.warn(
                    "bridge ({}) or bridgeHandler ({}) is null in handleBridgeConfigurationUpdate(), this is a bug, please report it.",
                    bridge, bridgeHandler);
            return;
        }
        if (config == null) {
            logger.warn("config is null in handleBridgeConfigurationUpdate(), this is a bug, please report it.");
            return;
        }
        FroniusBridgeConfiguration bridgeConfig = bridge.getConfiguration().as(FroniusBridgeConfiguration.class);
        initializeBatteryControl(bridgeHandler, bridgeConfig.scheme, bridgeConfig.hostname, bridgeConfig.username,
                bridgeConfig.password);
        startBatterySettingsRefreshJob(config);
    }

    @Override
    public void dispose() {
        cancelBatterySettingsRefreshJob();
        super.dispose();
    }

    private void initializeBatteryControl(FroniusBridgeHandler bridgeHandler, String scheme, String hostname,
            @Nullable String username, @Nullable String password) {
        if (username == null || password == null) {
            logger.info(
                    "Credentials are not configured in the bridge. Battery control is not available for Thing '{}'.",
                    thing.getUID());
            return;
        }

        String firmwareVersion = getFirmwareVersion(scheme, hostname);
        if (firmwareVersion == null) {
            logger.warn(
                    "The firmware version of the Fronius inverter could not be determined. Battery control is not available for Thing '{}'.",
                    thing.getUID());
            return;
        }
        int hyphenIndex = firmwareVersion.indexOf('-');
        String versionString = (hyphenIndex > 0) ? firmwareVersion.substring(0, hyphenIndex) : firmwareVersion;
        SemverVersion version = SemverVersion.fromString(versionString);
        batteryControl = new FroniusBatteryControl(bridgeHandler.getConfigApiClient(), version, scheme, hostname,
                username, password);
    }

    public @Nullable FroniusBatteryControl getBatteryControl() {
        if (batteryControl == null) {
            logger.warn("Battery control is not available for Thing '{}'. Check the bridge configuration.",
                    thing.getUID());
        }
        return batteryControl;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (FroniusBindingConstants.BATTERY_SETTINGS_CHANNELS.contains(channelId)) {
            FroniusBatteryControl control = getBatteryControl();
            if (control == null) {
                return;
            }
            try {
                if (command instanceof RefreshType) {
                    updateBatterySettingsChannels(control);
                    refreshNightPreservationLimit(control);
                    return;
                }
                switch (channelId) {
                    case FroniusBindingConstants.BATTERY_CALIBRATION_CHANNEL,
                            FroniusBindingConstants.BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL -> {
                        logger.debug("Channel {} is read-only, ignoring command {}", channelId, command);
                        return;
                    }
                    case FroniusBindingConstants.BATTERY_CHARGE_FROM_GRID_CHANNEL -> {
                        if (!(command instanceof OnOffType onOff)) {
                            logger.warn("Unsupported command {} for channel {}", command, channelId);
                            return;
                        }
                        boolean enabled = onOff == OnOffType.ON;
                        control.setChargeFromGrid(enabled);
                        applyUpdatedSettings(control, prev -> new BatterySettings(prev.minSoc(), prev.maxSoc(),
                                prev.backupReservedCapacity(), prev.backupCriticalSoc(), enabled, prev.calibrating()));
                    }
                    case FroniusBindingConstants.BATTERY_BACKUP_RESERVED_CHANNEL,
                            FroniusBindingConstants.BATTERY_BACKUP_CRITICAL_SOC_CHANNEL,
                            FroniusBindingConstants.BATTERY_SOC_MIN_CHANNEL,
                            FroniusBindingConstants.BATTERY_SOC_MAX_CHANNEL -> {
                        int value;
                        if (command instanceof QuantityType<?> quantity) {
                            QuantityType<?> percent = quantity.toUnit("%");
                            if (percent == null) {
                                logger.warn("Unsupported command {} for channel {}", command, channelId);
                                return;
                            }
                            value = percent.intValue();
                        } else if (command instanceof DecimalType decimal) {
                            value = decimal.intValue();
                        } else {
                            logger.warn("Unsupported command {} for channel {}", command, channelId);
                            return;
                        }
                        switch (channelId) {
                            case FroniusBindingConstants.BATTERY_BACKUP_RESERVED_CHANNEL -> {
                                control.setBackupReservedCapacity(value);
                                applyUpdatedSettings(control, prev -> new BatterySettings(prev.minSoc(), prev.maxSoc(),
                                        value, prev.backupCriticalSoc(), prev.chargeFromGrid(), prev.calibrating()));
                            }
                            case FroniusBindingConstants.BATTERY_BACKUP_CRITICAL_SOC_CHANNEL -> {
                                control.setBackupCriticalSoc(value);
                                applyUpdatedSettings(control,
                                        prev -> new BatterySettings(prev.minSoc(), prev.maxSoc(),
                                                prev.backupReservedCapacity(), value, prev.chargeFromGrid(),
                                                prev.calibrating()));
                            }
                            case FroniusBindingConstants.BATTERY_SOC_MIN_CHANNEL -> {
                                BatterySettings settings = control.getBatterySettings();
                                control.setSocLimits(value, settings.maxSoc());
                                applyBatterySettings(new BatterySettings(value, settings.maxSoc(),
                                        settings.backupReservedCapacity(), settings.backupCriticalSoc(),
                                        settings.chargeFromGrid(), settings.calibrating()));
                            }
                            case FroniusBindingConstants.BATTERY_SOC_MAX_CHANNEL -> {
                                BatterySettings settings = control.getBatterySettings();
                                control.setSocLimits(settings.minSoc(), value);
                                applyBatterySettings(new BatterySettings(settings.minSoc(), value,
                                        settings.backupReservedCapacity(), settings.backupCriticalSoc(),
                                        settings.chargeFromGrid(), settings.calibrating()));
                            }
                        }
                    }
                }
            } catch (FroniusCommunicationException | FroniusUnauthorizedException | IllegalArgumentException e) {
                logger.warn("Failed to handle command for channel {}: {}", channelId, e.getMessage());
            }
            return;
        }
        super.handleCommand(channelUID, command);
    }

    private void updateBatterySettingsChannels(FroniusBatteryControl control)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        applyBatterySettings(control.getBatterySettings());
    }

    private void applyBatterySettings(BatterySettings settings) {
        lastBatterySettings = settings;
        updateState(FroniusBindingConstants.BATTERY_SOC_MIN_CHANNEL,
                new QuantityType<>(settings.minSoc(), Units.PERCENT));
        updateState(FroniusBindingConstants.BATTERY_SOC_MAX_CHANNEL,
                new QuantityType<>(settings.maxSoc(), Units.PERCENT));
        updateState(FroniusBindingConstants.BATTERY_BACKUP_RESERVED_CHANNEL,
                new QuantityType<>(settings.backupReservedCapacity(), Units.PERCENT));
        updateState(FroniusBindingConstants.BATTERY_BACKUP_CRITICAL_SOC_CHANNEL,
                new QuantityType<>(settings.backupCriticalSoc(), Units.PERCENT));
        updateState(FroniusBindingConstants.BATTERY_CHARGE_FROM_GRID_CHANNEL,
                OnOffType.from(settings.chargeFromGrid()));
        updateState(FroniusBindingConstants.BATTERY_CALIBRATION_CHANNEL, OnOffType.from(settings.calibrating()));
    }

    /**
     * Updates the battery settings channels after a successful write: if the settings have been read before, the
     * updated settings are derived from the known state to avoid a costly re-read from the config API; otherwise the
     * settings are read from the inverter.
     */
    private void applyUpdatedSettings(FroniusBatteryControl control, UnaryOperator<BatterySettings> update)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        BatterySettings previous = lastBatterySettings;
        if (previous != null) {
            applyBatterySettings(update.apply(previous));
        } else {
            updateBatterySettingsChannels(control);
        }
    }

    private void refreshNightPreservationLimit(FroniusBatteryControl control) {
        if (!isLinked(FroniusBindingConstants.BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL)) {
            return;
        }
        try {
            int limit = control.getNightPreservationLimit();
            lastNightPreservationLimit = limit;
            updateState(FroniusBindingConstants.BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL,
                    new QuantityType<>(limit, Units.PERCENT));
        } catch (FroniusCommunicationException | FroniusUnauthorizedException e) {
            logger.debug("Failed to read night preservation limit: {}", e.getMessage());
        }
    }

    /**
     * Periodically reads the battery settings to populate the battery settings channels, since they are not part of
     * the cyclically polled Solar API data and reading them requires a costly login to the inverter's config API.
     * The slow periodic refresh also picks up changes made through the inverter's web UI.
     */
    private void startBatterySettingsRefreshJob(FroniusBatteryConfiguration config) {
        cancelBatterySettingsRefreshJob();
        if (config.batterySettingsRefreshInterval <= 0) {
            logger.debug("Battery settings refresh is disabled for Thing '{}'.", thing.getUID());
            return;
        }
        batterySettingsRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            FroniusBatteryControl control = batteryControl;
            if (control == null
                    || FroniusBindingConstants.BATTERY_SETTINGS_CHANNELS.stream().noneMatch(this::isLinked)) {
                return;
            }
            try {
                updateBatterySettingsChannels(control);
            } catch (FroniusUnauthorizedException e) {
                logger.warn("Failed to read battery settings: {}", e.getMessage());
            } catch (FroniusCommunicationException e) {
                // Expected to happen from time to time, e.g. when the inverter is unreachable, so only log at debug
                logger.debug("Failed to read battery settings: {}", e.getMessage());
            }
            refreshNightPreservationLimit(control);
        }, 0, config.batterySettingsRefreshInterval, TimeUnit.MINUTES);
    }

    private void cancelBatterySettingsRefreshJob() {
        ScheduledFuture<?> job = batterySettingsRefreshJob;
        if (job != null) {
            job.cancel(false);
            batterySettingsRefreshJob = null;
        }
    }

    @Override
    protected @Nullable State getValue(String channelId) {
        BatterySettings settings = lastBatterySettings;
        if (FroniusBindingConstants.BATTERY_SOC_MIN_CHANNEL.equals(channelId)) {
            return settings == null ? null : new QuantityType<>(settings.minSoc(), Units.PERCENT);
        }
        if (FroniusBindingConstants.BATTERY_SOC_MAX_CHANNEL.equals(channelId)) {
            return settings == null ? null : new QuantityType<>(settings.maxSoc(), Units.PERCENT);
        }
        if (FroniusBindingConstants.BATTERY_BACKUP_RESERVED_CHANNEL.equals(channelId)) {
            return settings == null ? null : new QuantityType<>(settings.backupReservedCapacity(), Units.PERCENT);
        }
        if (FroniusBindingConstants.BATTERY_BACKUP_CRITICAL_SOC_CHANNEL.equals(channelId)) {
            return settings == null ? null : new QuantityType<>(settings.backupCriticalSoc(), Units.PERCENT);
        }
        if (FroniusBindingConstants.BATTERY_CHARGE_FROM_GRID_CHANNEL.equals(channelId)) {
            return settings == null ? null : OnOffType.from(settings.chargeFromGrid());
        }
        if (FroniusBindingConstants.BATTERY_CALIBRATION_CHANNEL.equals(channelId)) {
            return settings == null ? null : OnOffType.from(settings.calibrating());
        }
        if (FroniusBindingConstants.BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL.equals(channelId)) {
            Integer limit = lastNightPreservationLimit;
            return limit == null ? null : new QuantityType<>(limit, Units.PERCENT);
        }

        StorageController local = controller;
        if (local == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        return switch (fieldName) {
            case FroniusBindingConstants.BATTERY_CAPACITY_MAXIMUM ->
                new QuantityType<>(local.getCapacityMaximum(), Units.WATT_HOUR);
            case FroniusBindingConstants.BATTERY_DESIGNED_CAPACITY ->
                new QuantityType<>(local.getDesignedCapacity(), Units.WATT_HOUR);
            case FroniusBindingConstants.BATTERY_CURRENT_DC -> new QuantityType<>(local.getCurrentDC(), Units.AMPERE);
            case FroniusBindingConstants.BATTERY_VOLTAGE_DC -> new QuantityType<>(local.getVoltageDC(), Units.VOLT);
            case FroniusBindingConstants.BATTERY_STATE_OF_CHARGE ->
                new QuantityType<>(local.getStateOfChargeRelative(), Units.PERCENT);
            case FroniusBindingConstants.BATTERY_ENABLE -> new DecimalType(local.getEnable());
            case FroniusBindingConstants.BATTERY_STATUS_BATTERY_CELL -> {
                String status = local.getStatusBatteryCell();
                if (status == null || status.isBlank()) {
                    yield UnDefType.UNDEF;
                } else {
                    yield new StringType(status);
                }
            }
            case FroniusBindingConstants.BATTERY_TEMPERATURE_CELL ->
                new QuantityType<>(local.getTemperatureCell(), SIUnits.CELSIUS);
            case FroniusBindingConstants.BATTERY_TIMESTAMP ->
                new DateTimeType(Instant.ofEpochSecond((long) local.getTimeStamp()));
            default -> null;
        };
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        FroniusBatteryConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in handleRefresh(), this is a bug, please report it.");
            return;
        }
        updateData(bridgeConfiguration, config);
        updateChannels();
        updateProperties();
    }

    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBatteryConfiguration config)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getBatteryDataUrl(bridgeConfiguration.scheme,
                bridgeConfiguration.hostname, config.deviceId);
        StorageRealtimeResponse response = collectDataFromUrl(StorageRealtimeResponse.class, location);

        this.controller = Optional.ofNullable(response) //
                .map(StorageRealtimeResponse::getBody) //
                .map(StorageRealtimeBody::getData) //
                .map(StorageRealtimeBodyData::getController) //
                .orElse(null);
    }

    private void updateProperties() {
        StorageController local = controller;
        if (local == null) {
            return;
        }
        StorageDetails details = local.getDetails();
        if (details == null) {
            return;
        }

        var properties = editProperties();
        if (details.getManufacturer() instanceof String manufacturer && !manufacturer.isBlank()) {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
        }
        if (details.getModel() instanceof String model && !model.isBlank()) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }
        if (details.getSerial() instanceof String serial && !serial.isBlank()) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serial);
        }
        if (!properties.isEmpty()) {
            updateProperties(properties);
        }
    }
}
