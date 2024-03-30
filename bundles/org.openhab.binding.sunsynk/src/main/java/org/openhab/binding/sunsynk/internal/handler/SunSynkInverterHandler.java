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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.sunsynk.internal.SunSynkInverter;
import org.openhab.binding.sunsynk.internal.classes.Battery;
import org.openhab.binding.sunsynk.internal.classes.Daytemps;
import org.openhab.binding.sunsynk.internal.classes.Daytempsreturn;
import org.openhab.binding.sunsynk.internal.classes.Grid;
import org.openhab.binding.sunsynk.internal.classes.RealTimeInData;
import org.openhab.binding.sunsynk.internal.classes.Settings;
import org.openhab.binding.sunsynk.internal.config.SunSynkInverterConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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
// @NonNullByDefault
public class SunSynkInverterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SunSynkInverterHandler.class);

    // private @Nullable InverterConfig config;
    // private /* @Nullable */ Inverter config;
    private SunSynkInverter inverter;
    private int refreshTime = 60;
    private ScheduledFuture<?> refreshTask;

    public SunSynkInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {

        if (command instanceof RefreshType) {
            refreshStateAndUpdate();
        } else { // if (channelUID.getId().equals(SunSynkBindingConstants.CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE)) {}
            Settings inverterChargeSettings = inverter.getBatteryChargeSettings();
            if (inverterChargeSettings == null) {
                return;
            }
            switch (channelUID.getIdWithoutGroup()) {
                // Grid Charge
                case CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 1);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 1);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 2);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 2);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 3);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 3);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 4);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 4);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 5);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 5);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGridTimerOn(true, 6);
                    } else {
                        inverterChargeSettings.setIntervalGridTimerOn(false, 6);
                    }
                    break;
                // Gen Charge
                case CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 1);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 1);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 2);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 2);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 3);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 3);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 4);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 4);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 5);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 5);
                    }
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE:
                    if (command.equals(OnOffType.ON)) {
                        inverterChargeSettings.setIntervalGenTimerOn(true, 6);
                    } else {
                        inverterChargeSettings.setIntervalGenTimerOn(false, 6);
                    }
                    break;
                // Interval Time
                case CHANNEL_BATTERY_INTERVAL_1_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 1);
                    break;
                case CHANNEL_BATTERY_INTERVAL_2_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 2);
                    break;
                case CHANNEL_BATTERY_INTERVAL_3_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 3);
                    break;
                case CHANNEL_BATTERY_INTERVAL_4_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 4);
                    break;
                case CHANNEL_BATTERY_INTERVAL_5_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 5);
                    break;
                case CHANNEL_BATTERY_INTERVAL_6_TIME:
                    inverterChargeSettings.setIntervalTime(command.toString(), 6);
                    break;
            }
            // may need to detect something has changes rather than just always doing this?
            sendAPICommandToInverter(inverterChargeSettings);
        }
    }

    private void sendAPICommandToInverter(Settings inverterChargeSettings) {
        logger.debug("Ok - will handle command for CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE");
        try {

            // Settings inverterChargeSettings = inverter.getBatteryChargeSettings();
            String body = inverterChargeSettings.buildBody();
            String token = inverterChargeSettings.getToken();
            String response = inverter.sendCommandToSunSynk(body, token);
            logger.debug("Sent command to Sun Account : {}", response);

        } catch (Exception e) {

            // TO DO
            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");

        }
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
        logger.debug("Will boot up the invert binding!");
        SunSynkInverterConfig config = getThing().getConfiguration().as(SunSynkInverterConfig.class);
        logger.debug("Inverter Config: {}", config);

        if (config.getRefresh() < refreshTime) {
            logger.warn(
                    "Refresh time [{}] is not valid. Refresh time must be at least 60 seconds.  Setting to minimum of 60 sec",
                    config.getRefresh());
            config.setRefresh(60);
        } else {
            refreshTime = config.getRefresh();
        }

        inverter = new SunSynkInverter(config);
        startAutomaticRefresh();

        // Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        // LC below is default code do I need it?
        /*
         * ------ LC
         * // Example for background initialization:
         * scheduler.execute(() -> {
         * boolean thingReachable = true; // <background task with long running initialization here>
         * // when done do:
         * if (thingReachable) {
         * updateStatus(ThingStatus.ONLINE);
         * } else {
         * updateStatus(ThingStatus.OFFLINE);
         * }
         * });
         * LC ------
         */

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public void refreshStateAndUpdate() {
        if (inverter != null) {
            try {
                boolean autheticated = inverter.sendGetState();
                if (!autheticated) {
                    BridgeHandler bridge = getBridge().getHandler();
                    bridge.initialize();
                }
                updateStatus(ThingStatus.ONLINE);
                publishChannels();
            } catch (Exception e) {
                logger.debug("Error when refreshing state.", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void startAutomaticRefresh() {
        Runnable refresher = () -> refreshStateAndUpdate();

        this.refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime, TimeUnit.SECONDS);
        logger.debug("Start automatic refresh at {} seconds", refreshTime);
    }

    private void publishChannels() {
        logger.debug("Updating Channels");

        Settings inverterChargeSettings = inverter.getBatteryChargeSettings();
        if (inverterChargeSettings == null) {
            return;
        }
        updateProperty(Thing.PROPERTY_VENDOR, "SunSynk");
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, inverterChargeSettings.getsn());
        // updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, neatoState.getMeta().getFirmware());

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

        Grid inverterGrid = inverter.getRealTimeGridStatus();
        if (inverterGrid == null) {
            return;
        }

        updateState(CHANNEL_INVERTER_GRID_POWER, new DecimalType(inverterGrid.getGridPower()));
        updateState(CHANNEL_INVERTER_GRID_VOLTAGE, new DecimalType(inverterGrid.getGridVoltage()));
        updateState(CHANNEL_INVERTER_GRID_CURRENT, new DecimalType(inverterGrid.getGridCurrent()));

        Battery batteryState = inverter.getRealTimeBatteryState();
        if (batteryState == null) {
            return;
        }

        updateState(CHANNEL_BATTERY_VOLTAGE, new DecimalType(batteryState.getBatteryVoltage()));
        updateState(CHANNEL_BATTERY_CURRENT, new DecimalType(batteryState.getBatteryCurrent()));
        updateState(CHANNEL_BATTERY_POWER, new DecimalType(batteryState.getBatteryPower()));
        updateState(CHANNEL_BATTERY_SOC, new DecimalType(batteryState.getBatterySOC()));
        updateState(CHANNEL_BATTERY_TEMPERATURE, new DecimalType(batteryState.getBatteryTemperature()));

        Daytemps batteryTempHist = inverter.getInverterTemperatureHistory();
        Daytempsreturn batteryTemps = batteryTempHist.InverterTemperatures();
        if (batteryTemps == null) {
            return;
        }

        if (!batteryTemps.getStatus().equals("okay")) {
            return;
        }
        updateState(CHANNEL_INVERTER_AC_TEMPERATURE, new DecimalType(batteryTemps.getDCTemperature()));
        updateState(CHANNEL_INVERTER_DC_TEMPERATURE, new DecimalType(batteryTemps.getACTemperature()));

        RealTimeInData solar = inverter.getRealtimeSolarStatus();
        if (solar == null) {
            return;
        }

        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TODAY, new DecimalType(solar.getetoday()));
        updateState(CHANNEL_INVERTER_SOLAR_ENERGY_TOTAL, new DecimalType(solar.getetotal()));
        updateState(CHANNEL_INVERTER_SOLAR_POWER_NOW, new DecimalType(solar.getPVIV()));
    }
}
