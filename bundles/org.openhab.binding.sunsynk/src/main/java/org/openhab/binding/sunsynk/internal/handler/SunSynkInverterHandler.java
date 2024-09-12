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
package org.openhab.binding.sunsynk.internal.handler;

import static org.openhab.binding.sunsynk.internal.SunSynkBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.DeviceController;
import org.openhab.binding.sunsynk.internal.api.dto.Battery;
import org.openhab.binding.sunsynk.internal.api.dto.Daytemps;
import org.openhab.binding.sunsynk.internal.api.dto.Daytempsreturn;
import org.openhab.binding.sunsynk.internal.api.dto.Grid;
import org.openhab.binding.sunsynk.internal.api.dto.RealTimeInData;
import org.openhab.binding.sunsynk.internal.api.dto.Settings;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkDeviceControllerException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkGetStatusException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkSendCommandException;
import org.openhab.binding.sunsynk.internal.config.SunSynkInverterConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link SunSynkInverterHandler} is responsible for handling commands, which are
 * sent to one of the Inverter channels.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkInverterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SunSynkInverterHandler.class);
    private @Nullable ZonedDateTime lockoutTimer = null;
    private DeviceController inverter = new DeviceController();
    private @Nullable ScheduledFuture<?> refreshTask;
    private boolean batterySettingsUpdated = false;
    private SunSynkInverterConfig config = new SunSynkInverterConfig();
    public Settings tempInverterChargeSettings = inverter.tempInverterChargeSettings; // Holds modified
                                                                                      // battery settings.

    public SunSynkInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Optional<SunSynkAccountHandler> checkBridge = getSafeBridge();
        if (!checkBridge.isPresent()) {
            logger.debug("Failed to find associated SunSynk Bridge.");
            return;
        }

        if (command instanceof RefreshType) {
            refreshStateAndUpdate();
        } else {
            this.tempInverterChargeSettings = inverter.getBatteryChargeSettings();
            switch (channelUID.getIdWithoutGroup()) {
                // Grid charge
                case CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGridTimerOn(command.equals(OnOffType.ON), 6);
                    break;
                // Gen charge
                case CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE:
                    this.tempInverterChargeSettings.setIntervalGenTimerOn(command.equals(OnOffType.ON), 6);
                    break;
                // Interval time
                case CHANNEL_BATTERY_INTERVAL_1_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_TIME:
                    this.tempInverterChargeSettings.setIntervalTime(command.toString(), 6);
                    break;
                // Charge target
                case CHANNEL_BATTERY_INTERVAL_1_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_CAPACITY:
                    this.tempInverterChargeSettings.setIntervalBatteryCapacity(Integer.valueOf(command.toString()), 6);
                    break;
                // Battery charging power limit
                case CHANNEL_BATTERY_INTERVAL_1_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_POWER_LIMIT:
                    this.tempInverterChargeSettings.setIntervalBatteryPowerLimit(Integer.valueOf(command.toString()),
                            6);
                    break;
                // Inverter control
                case CHANNEL_INVERTER_CONTROL_TIMER:
                    this.tempInverterChargeSettings.setPeakAndValley(Integer.valueOf(command.toString()));
                    break;
                case CHANNEL_INVERTER_CONTROL_ENERGY_PATTERN:
                    this.tempInverterChargeSettings.setEnergyMode(Integer.valueOf(command.toString()));
                    break;
                case CHANNEL_INVERTER_CONTROL_WORK_MODE:
                    this.tempInverterChargeSettings.setSysWorkMode(Integer.valueOf(command.toString()));
                    break;
            }

            this.batterySettingsUpdated = true; // true = update battery settings from API at next interval
        }
    }

    private void sendSettingsCommandToInverter() {
        try {
            inverter.sendSettings(this.tempInverterChargeSettings);
        } catch (SunSynkSendCommandException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not send command to inverter " + config.getAlias() + ". Authentication Failure !");
            return;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask != null) {
            refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getThing().getConfiguration().as(SunSynkInverterConfig.class);
        logger.debug("Inverter Config: {}", config);

        if (config.getRefresh() < 60) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh time " + config.getRefresh() + " is not valid. Refresh time must be at least 60 seconds.");
            return;
        }
        this.batterySettingsUpdated = false;
        inverter = new DeviceController(config);
        startAutomaticRefresh();
    }

    private Optional<SunSynkAccountHandler> getSafeBridge() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof SunSynkAccountHandler bridgeHandler) {
            return Optional.of(bridgeHandler);
        }
        return Optional.empty();
    }

    public void refreshStateAndUpdate() {
        ZonedDateTime lockoutTimer = this.lockoutTimer;

        if (lockoutTimer != null && lockoutTimer.isAfter(ZonedDateTime.now())) { // lockout calls that come
                                                                                 // too fast
            logger.debug("API call too frequent, ignored {} ", lockoutTimer);
            return;
        }
        lockoutTimer = ZonedDateTime.now().plusMinutes(1); // lockout time 1 min

        Optional<SunSynkAccountHandler> checkBridge = getSafeBridge();
        if (!checkBridge.isPresent()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No SunSynk Account");
            return;
        }
        SunSynkAccountHandler bridgeHandler = checkBridge.get();
        try {
            bridgeHandler.refreshAccount(); // check account token
        } catch (SunSynkAuthenticateException e) {
            if (logger.isDebugEnabled()) {
                String message = Objects.requireNonNullElse(e.getMessage(), "unkown error message");
                Throwable cause = e.getCause();
                String causeMessage = cause != null ? Objects.requireNonNullElse(cause.getMessage(), "unkown cause")
                        : "unkown cause";
                logger.debug("Sun Synk account refresh failed: Msg = {}. Cause = {}.", message, causeMessage);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Sun Synk account refresh failed");
            bridgeHandler.setBridgeOffline();
            return;
        }
        if (this.batterySettingsUpdated) { // have the settings been modified locally
            sendSettingsCommandToInverter(); // update the battery settings
        }
        try {
            inverter.sendGetState(this.batterySettingsUpdated); // get inverter settings
        } catch (SunSynkGetStatusException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not get state of inverter " + config.getAlias() + ". Authentication Failure !");
            return;
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not get state of inverter " + config.getAlias() + ". JSON parsing Failure !");
            return;
        } catch (SunSynkDeviceControllerException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not get state of inverter " + config.getAlias() + ". DeviceController Failure !");
            return;
        }
        logger.debug("Retrieved state of inverter {}.", config.getAlias());
        this.batterySettingsUpdated = false; // set to get settings from API
        bridgeHandler.setBridgeOnline();
        updateStatus(ThingStatus.ONLINE);
        publishChannels();
    }

    private void startAutomaticRefresh() {
        this.refreshTask = scheduler.scheduleWithFixedDelay(this::refreshStateAndUpdate, 0, config.getRefresh(),
                TimeUnit.SECONDS);
    }

    private void publishChannels() {
        logger.debug("Updating Channels");
        updateSettings();
        updateGrid();
        updateBattery();
        updateTemperature();
        updateSolar();
    }

    private void updateSettings() {
        Settings inverterChargeSettings = inverter.getBatteryChargeSettings();
        updateState(CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(0)));
        updateState(CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(1)));
        updateState(CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(2)));
        updateState(CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(3)));
        updateState(CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(4)));
        updateState(CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGridTimerOn().get(5)));
        updateState(CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(0)));
        updateState(CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(1)));
        updateState(CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(2)));
        updateState(CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(3)));
        updateState(CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(4)));
        updateState(CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE,
                OnOffType.from(inverterChargeSettings.getIntervalGenTimerOn().get(5)));
        updateState(CHANNEL_BATTERY_INTERVAL_1_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(0)));
        updateState(CHANNEL_BATTERY_INTERVAL_2_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(1)));
        updateState(CHANNEL_BATTERY_INTERVAL_3_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(2)));
        updateState(CHANNEL_BATTERY_INTERVAL_4_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(3)));
        updateState(CHANNEL_BATTERY_INTERVAL_5_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(4)));
        updateState(CHANNEL_BATTERY_INTERVAL_6_CAPACITY,
                new DecimalType(inverterChargeSettings.getIntervalBatteryCapacity().get(5)));
        updateState(CHANNEL_BATTERY_INTERVAL_1_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(0)));
        updateState(CHANNEL_BATTERY_INTERVAL_2_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(1)));
        updateState(CHANNEL_BATTERY_INTERVAL_3_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(2)));
        updateState(CHANNEL_BATTERY_INTERVAL_4_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(3)));
        updateState(CHANNEL_BATTERY_INTERVAL_5_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(4)));
        updateState(CHANNEL_BATTERY_INTERVAL_6_TIME, new StringType(inverterChargeSettings.getIntervalTime().get(5)));
        updateState(CHANNEL_BATTERY_INTERVAL_1_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(0)));
        updateState(CHANNEL_BATTERY_INTERVAL_2_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(1)));
        updateState(CHANNEL_BATTERY_INTERVAL_3_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(2)));
        updateState(CHANNEL_BATTERY_INTERVAL_4_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(3)));
        updateState(CHANNEL_BATTERY_INTERVAL_5_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(4)));
        updateState(CHANNEL_BATTERY_INTERVAL_6_POWER_LIMIT,
                new DecimalType(inverterChargeSettings.getIntervalBatteryPowerLimit().get(5)));
        updateState(CHANNEL_INVERTER_CONTROL_TIMER, new DecimalType(inverterChargeSettings.getPeakAndValley()));
        updateState(CHANNEL_INVERTER_CONTROL_ENERGY_PATTERN, new StringType(inverterChargeSettings.getEnergyMode()));
        updateState(CHANNEL_INVERTER_CONTROL_WORK_MODE, new StringType(inverterChargeSettings.getSysWorkMode()));
    }

    private void updateGrid() {
        Grid inverterGrid = inverter.getRealTimeGridStatus();
        updateState(CHANNEL_INVERTER_GRID_POWER, new DecimalType(inverterGrid.getGridPower()));
        updateState(CHANNEL_INVERTER_GRID_VOLTAGE, new DecimalType(inverterGrid.getGridVoltage()));
        updateState(CHANNEL_INVERTER_GRID_CURRENT, new DecimalType(inverterGrid.getGridCurrent()));
    }

    private void updateBattery() {
        Battery batteryState = inverter.getRealTimeBatteryState();
        updateState(CHANNEL_BATTERY_VOLTAGE, new DecimalType(batteryState.getBatteryVoltage()));
        updateState(CHANNEL_BATTERY_CURRENT, new DecimalType(batteryState.getBatteryCurrent()));
        updateState(CHANNEL_BATTERY_POWER, new DecimalType(batteryState.getBatteryPower()));
        updateState(CHANNEL_BATTERY_SOC, new DecimalType(batteryState.getBatterySOC()));
        updateState(CHANNEL_BATTERY_TEMPERATURE, new DecimalType(batteryState.getBatteryTemperature()));
    }

    private void updateTemperature() {
        Daytemps batteryTempHist = inverter.getInverterTemperatureHistory();
        Daytempsreturn batteryTemps = batteryTempHist.inverterTemperatures();
        if (!batteryTemps.getStatus()) {
            logger.debug("Failed to get inverter and battery temperatures");
            return;
        }
        updateState(CHANNEL_INVERTER_AC_TEMPERATURE, new DecimalType(batteryTemps.getDCTemperature()));
        updateState(CHANNEL_INVERTER_DC_TEMPERATURE, new DecimalType(batteryTemps.getACTemperature()));
    }

    private void updateSolar() {
        RealTimeInData solar = inverter.getRealtimeSolarStatus();
        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TODAY, new DecimalType(solar.getetoday()));
        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TOTAL, new DecimalType(solar.getetotal()));
        updateState(CHANNEL_INVERTER_SOLAR_POWER_NOW, new DecimalType(solar.getPVIV()));
    }
}
