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

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.action.FroniusSymoInverterActions;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl.BatterySettings;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusHttpUtil;
import org.openhab.binding.fronius.internal.api.FroniusUnauthorizedException;
import org.openhab.binding.fronius.internal.api.dto.ValueUnit;
import org.openhab.binding.fronius.internal.api.dto.inverter.*;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeInverter;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeSite;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.firmware.types.SemverVersion;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FroniusSymoInverterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Jimmy Tanagra - Add powerflow autonomy, self consumption channels
 * @author Florian Hotze - Add battery control actions
 */
@NonNullByDefault
public class FroniusSymoInverterHandler extends FroniusBaseThingHandler {

    private static final int BATTERY_SETTINGS_REFRESH_PERIOD_MINUTES = 5;
    private static final Set<String> BATTERY_SETTINGS_CHANNELS = Set.of(FroniusBindingConstants.BATTERY_SOC_MIN_CHANNEL,
            FroniusBindingConstants.BATTERY_SOC_MAX_CHANNEL, FroniusBindingConstants.BATTERY_BACKUP_RESERVED_CHANNEL,
            FroniusBindingConstants.BATTERY_BACKUP_CRITICAL_SOC_CHANNEL,
            FroniusBindingConstants.BATTERY_CHARGE_FROM_GRID_CHANNEL,
            FroniusBindingConstants.BATTERY_CALIBRATION_CHANNEL,
            FroniusBindingConstants.BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL);

    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterHandler.class);
    private final HttpClient httpClient;

    private @Nullable InverterRealtimeResponse inverterRealtimeResponse;
    private @Nullable PowerFlowRealtimeResponse powerFlowResponse;
    private @Nullable FroniusBaseDeviceConfiguration config;
    private @Nullable InverterInfo inverterInfo;
    private @Nullable FroniusBatteryControl batteryControl;
    private @Nullable BatterySettings lastBatterySettings;
    private @Nullable Integer lastNightPreservationLimit;
    private @Nullable ScheduledFuture<?> batterySettingsRefreshJob;

    public FroniusSymoInverterHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    protected String getDescription() {
        return "Fronius Symo Inverter";
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        FroniusBaseDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in handleRefresh(), this is a bug, please report it.");
            return;
        }
        updateData(bridgeConfiguration, config);
        updateChannels();
    }

    private void initializeBatteryControl(FroniusHttpUtil httpUtil, String scheme, String hostname,
            @Nullable String username, @Nullable String password) {
        if (username == null || password == null) {
            logger.info(
                    "Credentials are not configured in the bridge. Battery control is not available for Thing '{}'.",
                    thing.getUID());
            return;
        }

        InverterInfo localInverterInfo = inverterInfo;
        if (localInverterInfo != null) {
            String firmwareVersion = localInverterInfo.firmware();
            if (firmwareVersion != null) {
                int hyphenIndex = firmwareVersion.indexOf('-');
                String versionString = (hyphenIndex > 0) ? firmwareVersion.substring(0, hyphenIndex) : firmwareVersion;
                SemverVersion version = SemverVersion.fromString(versionString);
                batteryControl = new FroniusBatteryControl(httpUtil, httpClient, version, scheme, hostname, username,
                        password);
                return;
            }
        }
        logger.warn(
                "The firmware version of the Fronius inverter could not be determined. Battery control is not available for Thing '{}'.",
                thing.getUID());
    }

    private void updateProperties() {
        InverterInfo localInverterInfo = inverterInfo;
        if (localInverterInfo == null) {
            return;
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, localInverterInfo.serial());
        String firmware = localInverterInfo.firmware();
        if (firmware != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmware);
        }
        updateProperties(properties);
    }

    @Override
    public void initialize() {
        FroniusBaseDeviceConfiguration config = this.config = getConfigAs(FroniusBaseDeviceConfiguration.class);
        FroniusBridgeHandler bridgeHandler = getFroniusBridgeHandler();
        Bridge bridge = getBridge();
        if (bridge == null || bridgeHandler == null) {
            logger.warn("bridge ({}) or bridgeHandler ({}) is null in initialize(), this is a bug, please report it.",
                    bridge, bridgeHandler);
            return;
        }
        FroniusBridgeConfiguration bridgeConfig = bridge.getConfiguration().as(FroniusBridgeConfiguration.class);
        inverterInfo = getInverterInfo(bridgeConfig.scheme, bridgeConfig.hostname, config.deviceId);
        updateProperties();
        initializeBatteryControl(bridgeHandler.getHttpUtil(), bridgeConfig.scheme, bridgeConfig.hostname,
                bridgeConfig.username, bridgeConfig.password);
        startBatterySettingsRefreshJob();
        super.initialize();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(FroniusSymoInverterActions.class);
    }

    @Override
    public void handleBridgeConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleBridgeConfigurationUpdate(configurationParameters);
        Bridge bridge = getBridge();
        FroniusBridgeHandler bridgeHandler = getFroniusBridgeHandler();
        FroniusBaseDeviceConfiguration config = this.config;
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
        inverterInfo = getInverterInfo(bridgeConfig.scheme, bridgeConfig.hostname, config.deviceId);
        updateProperties();
        initializeBatteryControl(bridgeHandler.getHttpUtil(), bridgeConfig.scheme, bridgeConfig.hostname,
                bridgeConfig.username, bridgeConfig.password);
        startBatterySettingsRefreshJob();
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
        if (BATTERY_SETTINGS_CHANNELS.contains(channelId)) {
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
                        logger.warn("Channel {} is read-only, ignoring command {}", channelId, command);
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
                    default -> {
                        int value;
                        if (command instanceof QuantityType<?> quantity) {
                            value = quantity.intValue();
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
                            default -> {
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
    private void startBatterySettingsRefreshJob() {
        cancelBatterySettingsRefreshJob();
        batterySettingsRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            FroniusBatteryControl control = batteryControl;
            if (control == null || BATTERY_SETTINGS_CHANNELS.stream().noneMatch(this::isLinked)) {
                return;
            }
            try {
                updateBatterySettingsChannels(control);
            } catch (FroniusCommunicationException | FroniusUnauthorizedException e) {
                logger.warn("Failed to read battery settings: {}", e.getMessage());
            }
            refreshNightPreservationLimit(control);
        }, 0, BATTERY_SETTINGS_REFRESH_PERIOD_MINUTES, TimeUnit.MINUTES);
    }

    private void cancelBatterySettingsRefreshJob() {
        ScheduledFuture<?> job = batterySettingsRefreshJob;
        if (job != null) {
            job.cancel(false);
            batterySettingsRefreshJob = null;
        }
    }

    @Override
    public void dispose() {
        cancelBatterySettingsRefreshJob();
        super.dispose();
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @return the last retrieved data
     */
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

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        InverterRealtimeBodyData inverterData = getInverterData();
        if (inverterData == null) {
            return null;
        }

        FroniusBaseDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in getValue(String channelId), this is a bug, please report it.");
            return null;
        }

        InverterDeviceStatus deviceStatus;
        switch (fieldName) {
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PAC:
                return getQuantityOrZero(inverterData.getPac(), Units.WATT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_FAC:
                return getQuantityOrZero(inverterData.getFac(), Units.HERTZ);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IAC:
                return getQuantityOrZero(inverterData.getIac(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC:
                return getQuantityOrZero(inverterData.getIdc(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC2:
                return getQuantityOrZero(inverterData.getIdc2(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC3:
                return getQuantityOrZero(inverterData.getIdc3(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UAC:
                return getQuantityOrZero(inverterData.getUac(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC:
                return getQuantityOrZero(inverterData.getUdc(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC2:
                return getQuantityOrZero(inverterData.getUdc2(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC3:
                return getQuantityOrZero(inverterData.getUdc3(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC:
                return calculatePower(inverterData.getUdc(), inverterData.getIdc());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC2:
                return calculatePower(inverterData.getUdc2(), inverterData.getIdc2());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC3:
                return calculatePower(inverterData.getUdc3(), inverterData.getIdc3());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DAY_ENERGY:
                // Convert the unit to kWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getDayEnergy(), Units.KILOWATT_HOUR).toUnit("kWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_TOTAL:
                // Convert the unit to MWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getTotalEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_YEAR:
                // Convert the unit to MWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getYearEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE:
                deviceStatus = inverterData.getDeviceStatus();
                if (deviceStatus == null) {
                    return null;
                }
                return new DecimalType(deviceStatus.getErrorCode());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE:
                deviceStatus = inverterData.getDeviceStatus();
                if (deviceStatus == null) {
                    return null;
                }
                return new DecimalType(deviceStatus.getStatusCode());
            default:
                break;
        }

        PowerFlowRealtimeBodyData powerFlowData = getPowerFlowRealtimeData();
        if (powerFlowData == null) {
            return null;
        }
        PowerFlowRealtimeSite site = powerFlowData.getSite();
        if (site == null) {
            return null;
        }

        return switch (fieldName) {
            case FroniusBindingConstants.POWER_FLOW_P_GRID -> new QuantityType<>(site.getPgrid(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_LOAD -> new QuantityType<>(site.getPload(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_AKKU -> new QuantityType<>(site.getPakku(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_PV -> new QuantityType<>(site.getPpv(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_AUTONOMY ->
                new QuantityType<>(site.getRelAutonomy(), Units.PERCENT);
            case FroniusBindingConstants.POWER_FLOW_SELF_CONSUMPTION ->
                new QuantityType<>(site.getRelSelfConsumption(), Units.PERCENT);
            case FroniusBindingConstants.POWER_FLOW_BACKUP_MODE -> {
                Boolean backupMode = site.getBackupMode();
                yield backupMode == null ? null : OnOffType.from(backupMode);
            }
            case FroniusBindingConstants.POWER_FLOW_BATTERY_STANDBY -> {
                Boolean batteryStandby = site.getBatteryStandby();
                yield batteryStandby == null ? null : OnOffType.from(batteryStandby);
            }
            case FroniusBindingConstants.POWER_FLOW_INVERTER_POWER -> {
                PowerFlowRealtimeInverter inverter = getInverter(config.deviceId);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getP(), Units.WATT);
            }
            case FroniusBindingConstants.POWER_FLOW_INVERTER_SOC -> {
                PowerFlowRealtimeInverter inverter = getInverter(config.deviceId);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getSoc(), Units.PERCENT);
            }
            // Kept for backwards compatibility
            case FroniusBindingConstants.POWER_FLOW_INVERTER_1_POWER -> {
                PowerFlowRealtimeInverter inverter = getInverter(1);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getP(), Units.WATT);
            }
            case FroniusBindingConstants.POWER_FLOW_INVERTER_1_SOC -> {
                PowerFlowRealtimeInverter inverter = getInverter(1);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getSoc(), Units.PERCENT);
            }

            default -> null;
        };
    }

    private @Nullable InverterRealtimeBodyData getInverterData() {
        InverterRealtimeResponse localInverterRealtimeResponse = inverterRealtimeResponse;
        if (localInverterRealtimeResponse == null) {
            return null;
        }
        InverterRealtimeBody inverterBody = localInverterRealtimeResponse.getBody();
        return (inverterBody != null) ? inverterBody.getData() : null;
    }

    private @Nullable PowerFlowRealtimeBodyData getPowerFlowRealtimeData() {
        PowerFlowRealtimeResponse localPowerFlowResponse = powerFlowResponse;
        if (localPowerFlowResponse == null) {
            return null;
        }
        PowerFlowRealtimeBody powerFlowBody = localPowerFlowResponse.getBody();
        return (powerFlowBody != null) ? powerFlowBody.getData() : null;
    }

    /**
     * get flow data for a specific inverter.
     *
     * @param number The inverter object of the given index
     * @return a PowerFlowRealtimeInverter object.
     */
    private @Nullable PowerFlowRealtimeInverter getInverter(final int number) {
        PowerFlowRealtimeBodyData powerFlowData = getPowerFlowRealtimeData();
        if (powerFlowData == null) {
            return null;
        }
        return powerFlowData.getInverters().get(Integer.toString(number));
    }

    /**
     * Return the value as QuantityType with the unit extracted from ValueUnit
     * or a zero QuantityType with the given unit argument when value is null
     *
     * @param value The ValueUnit data
     * @param unit The default unit to use when value is null
     * @return a QuantityType from the given value
     */
    private QuantityType<?> getQuantityOrZero(@Nullable ValueUnit value, Unit<?> unit) {
        QuantityType<?> val = null;
        if (value != null) {
            val = value.asQuantityType().toUnit(unit);
        }
        if (val == null) {
            return new QuantityType<>(0, unit);
        }
        return val;
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        inverterRealtimeResponse = getRealtimeData(bridgeConfiguration.scheme, bridgeConfiguration.hostname,
                config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(bridgeConfiguration.scheme, bridgeConfiguration.hostname);
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param scheme http or https
     * @param ip address of the device
     * @return {PowerFlowRealtimeResponse} the object representation of the json response
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String scheme, String ip)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getPowerFlowDataUrl(scheme, ip);
        return collectDataFromUrl(PowerFlowRealtimeResponse.class, location);
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param scheme http or https
     * @param ip address of the device
     * @param deviceId of the device
     * @return {InverterRealtimeResponse} the object representation of the json response
     */
    private InverterRealtimeResponse getRealtimeData(String scheme, String ip, int deviceId)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getInverterDataUrl(scheme, ip, deviceId);
        return collectDataFromUrl(InverterRealtimeResponse.class, location);
    }

    /**
     * Make the InverterInfoRequest
     *
     * @param scheme http or https
     * @param ip address of the device
     * @return {InverterInfoResponse} the object representation of the json response
     */
    private InverterInfoResponse getInverterInfoData(String scheme, String ip) throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getInverterInfoUrl(scheme, ip);
        return collectDataFromUrl(InverterInfoResponse.class, location, false);
    }

    /**
     * Calculate the power value from the given voltage and current channels
     *
     * @param voltage the voltage ValueUnit
     * @param current the current ValueUnit
     * @return {QuantityType<>} the power value calculated by multiplying voltage and current
     */
    private @Nullable QuantityType<?> calculatePower(ValueUnit voltage, ValueUnit current) {
        QuantityType<?> qtyVoltage = getQuantityOrZero(voltage, Units.VOLT);
        QuantityType<?> qtyCurrent = getQuantityOrZero(current, Units.AMPERE);
        return qtyVoltage.multiply(qtyCurrent).toUnit(Units.WATT);
    }

    /**
     * Get the version information of the inverter
     *
     * @param scheme http or https
     * @param ip address of the device
     * @param deviceId of the device
     * @return InverterInfoBody containing serial number and firmware version, or null if not available
     */
    private @Nullable InverterInfo getInverterInfo(String scheme, String ip, int deviceId) {
        InverterInfoResponse inverterInfoResponse = null;
        try {
            inverterInfoResponse = getInverterInfoData(scheme, ip);
        } catch (FroniusCommunicationException e) {
            logger.warn("Failed to get InverterInfo from Fronius inverter at {}: {}", ip, e.getMessage());
            return null;
        }
        if (inverterInfoResponse.getBody() == null) {
            return null;
        }
        InverterInfoBody inverterInfoBody = inverterInfoResponse.getBody();
        if (inverterInfoBody == null) {
            return null;
        }
        Map<Integer, InverterInfoBodyData> inverterInfoBodyData = inverterInfoBody.getData();
        if (inverterInfoBodyData == null) {
            return null;
        }
        InverterInfoBodyData data = inverterInfoBodyData.get(deviceId);
        if (data == null) {
            return null;
        }

        final String serial = String.valueOf(data.getUniqueID());

        final String host = scheme + "://" + ip;
        final String versionPath = "/status/version";
        FroniusHttpUtil httpUtil;
        try {
            httpUtil = getHttpUtil();
        } catch (FroniusCommunicationException e) {
            logger.warn("Failed to get bridge HTTP utility for Fronius inverter at {}: {}", ip, e.getMessage());
            return null;
        }

        String url = host + "/api" + versionPath; // try the new API path first
        String response;
        try {
            response = httpUtil.executeUrl(HttpMethod.GET, url, API_TIMEOUT);
        } catch (FroniusCommunicationException e) {
            url = host + versionPath; // fallback to the old API path
            try {
                response = httpUtil.executeUrl(HttpMethod.GET, url, API_TIMEOUT);
            } catch (FroniusCommunicationException ex) {
                logger.debug("Failed to get version info from Fronius inverter at {}: {}", ip, ex.getMessage());
                return new InverterInfo(serial, null);
            }
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(response);
            if (!jsonElement.isJsonObject()) {
                logger.debug("Invalid JSON response for version info from Fronius inverter at {}: {}", ip, response);
                return new InverterInfo(serial, null);
            }
            try {
                String firmware = jsonElement.getAsJsonObject().get("swrevisions").getAsJsonObject().get("GEN24")
                        .getAsString();
                return new InverterInfo(serial, firmware);
            } catch (IllegalStateException | UnsupportedOperationException e) {
                logger.debug("Failed to parse version info from Fronius inverter at {}: {}", ip, e.getMessage());
                return new InverterInfo(serial, null);
            }
        } catch (JsonSyntaxException e) {
            // 404 errors go here, as the response is not valid JSON
            logger.debug("Invalid JSON response for version info from Fronius inverter at {}: {}", ip, response, e);
        }
        return new InverterInfo(serial, null);
    }

    private record InverterInfo(String serial, @Nullable String firmware) {
    }
}
