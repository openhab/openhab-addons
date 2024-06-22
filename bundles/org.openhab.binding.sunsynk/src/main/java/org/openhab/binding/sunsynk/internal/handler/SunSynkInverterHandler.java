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
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int refreshTime = 60;
    private @Nullable ScheduledFuture<?> refreshTask;
    private boolean batterySettingsUpdated = false;
    public @Nullable Settings tempInverterChargeSettings = inverter.tempInverterChargeSettings; // Holds modified
                                                                                                // battery settings.

    public SunSynkInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshStateAndUpdate();
        } else {
            this.tempInverterChargeSettings = inverter.getBatteryChargeSettings();
            switch (channelUID.getIdWithoutGroup()) {
                // Grid charge
                case CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 1);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 1);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 2);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 2);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 3);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 3);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 4);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 4);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 5);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 5);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(true, 6);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGridTimerOn(false, 6);
                    }
                    break;
                // Gen charge
                case CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 1);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 1);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 2);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 2);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 3);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 3);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 4);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 4);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 5);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 5);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(true, 6);
                    } else {
                        this.tempInverterChargeSettings.setIntervalGenTimerOn(false, 6);
                    }
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

            Optional<SunSynkAccountHandler> checkBridge = getSafeBridge();
            if (!checkBridge.isPresent()) {
                logger.debug("Failed to find associated SunSynk Bridge.");
                return;
            }
            this.batterySettingsUpdated = true; // true = update battery settings from API at next interval
        }
    }

    private void sendSettingsCommandToInverter() {
        logger.debug("Ok - will handle command for CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE");
        SunSynkInverterConfig config = getThing().getConfiguration().as(SunSynkInverterConfig.class);
        try {
            inverter.sendSettings(this.tempInverterChargeSettings);
        } catch (SunSynkSendCommandException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not send command to inverter " + config.getAlias() + ". Authentication Failure !");
            logger.debug("Could not send command to inverter {}.", config.getAlias());
            return;
        }
        logger.debug("Sent command: to inverter {}.", config.getAlias());
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (this.refreshTask != null) {
            this.refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    @Override
    public void initialize() {
        // config = getConfigAs(Inverter.class);
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Will boot up the inverter binding!");
        SunSynkInverterConfig config = getThing().getConfiguration().as(SunSynkInverterConfig.class);
        logger.debug("Inverter Config: {}", config);

        if (config.getRefresh() < refreshTime) {
            logger.warn(
                    "Refresh time [{}] is not valid. Refresh time must be at least 60 seconds. Setting to minimum of 60 sec",
                    config.getRefresh());
            config.setRefresh(60);
        } else {
            refreshTime = config.getRefresh();
        }
        this.batterySettingsUpdated = false;
        inverter = new DeviceController(config);
        startAutomaticRefresh();
    }

    private Optional<SunSynkAccountHandler> getSafeBridge() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return Optional.empty();
        }
        ThingHandler handler = bridge.getHandler();
        SunSynkAccountHandler bridgeHandler = null;
        if (handler instanceof SunSynkAccountHandler) {
            bridgeHandler = (SunSynkAccountHandler) handler;
            return Optional.of(bridgeHandler);
        }
        return Optional.empty();
    }

    public void refreshStateAndUpdate() {
        if (this.lockoutTimer != null && this.lockoutTimer.isAfter(ZonedDateTime.now())) { // lockout calls that come
                                                                                           // too fast
            logger.debug("API call too frequent, ignored {} ", this.lockoutTimer);
            return;
        }
        this.lockoutTimer = ZonedDateTime.now().plusMinutes(1); // lockout time 1 min

        if (inverter != null) {
            Optional<SunSynkAccountHandler> checkBridge = getSafeBridge();
            if (!checkBridge.isPresent()) {
                logger.debug("Failed to find associated SunSynk account.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No SunSynk Account");
                return;
            }
            SunSynkAccountHandler bridgeHandler = checkBridge.get();
            try {
                bridgeHandler.refreshAccount(); // check account token
            } catch (SunSynkAuthenticateException e) {
                logger.debug("Sun Synk account refresh failed: Msg = {} Cause = {}.", e.getMessage().toString(),
                        e.getCause().toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Sun Synk account refresh failed");
                bridgeHandler.setBridgeOffline();
                return;
            }
            if (this.batterySettingsUpdated) { // have the settings been modified locally
                sendSettingsCommandToInverter(); // update the battery settings
            }
            SunSynkInverterConfig config = getThing().getConfiguration().as(SunSynkInverterConfig.class);
            try {
                inverter.sendGetState(this.batterySettingsUpdated); // get inverter settings
            } catch (SunSynkGetStatusException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not get state of inverter " + config.getAlias() + ". Authentication Failure !");
                logger.debug("Could not get state of inverter {}.", config.getAlias());
                return;
            }
            logger.debug("Retrieved state of inverter {}.", config.getAlias());
            this.batterySettingsUpdated = false; // set to get settings from API
            bridgeHandler.setBridgeOnline();
            updateStatus(ThingStatus.ONLINE);
            publishChannels();

        }
    }

    private void startAutomaticRefresh() {
        Runnable refresher = () -> refreshStateAndUpdate();
        this.refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime, TimeUnit.SECONDS);
        logger.debug("Start automatic refresh at {} seconds", refreshTime);
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
        if (inverterChargeSettings == null) {
            logger.debug("Failed to get battery charge settings");
            return;
        }
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
        if (inverterGrid == null) {
            logger.debug("Failed to get grid status");
            return;
        }
        updateState(CHANNEL_INVERTER_GRID_POWER, new DecimalType(inverterGrid.getGridPower()));
        updateState(CHANNEL_INVERTER_GRID_VOLTAGE, new DecimalType(inverterGrid.getGridVoltage()));
        updateState(CHANNEL_INVERTER_GRID_CURRENT, new DecimalType(inverterGrid.getGridCurrent()));
    }

    private void updateBattery() {
        Battery batteryState = inverter.getRealTimeBatteryState();
        if (batteryState == null) {
            logger.debug("Failed to get battery status");
            return;
        }
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
        if (solar == null) {
            logger.debug("Failed solar status");
            return;
        }
        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TODAY, new DecimalType(solar.getetoday()));
        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TOTAL, new DecimalType(solar.getetotal()));
        updateState(CHANNEL_INVERTER_SOLAR_POWER_NOW, new DecimalType(solar.getPVIV()));
    }
}
