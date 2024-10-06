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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler.Request;
import org.openhab.binding.tesla.internal.protocol.ChargeState;
import org.openhab.binding.tesla.internal.protocol.ClimateState;
import org.openhab.binding.tesla.internal.protocol.DriveState;
import org.openhab.binding.tesla.internal.protocol.Event;
import org.openhab.binding.tesla.internal.protocol.GUIState;
import org.openhab.binding.tesla.internal.protocol.SoftwareUpdate;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleData;
import org.openhab.binding.tesla.internal.protocol.VehicleState;
import org.openhab.binding.tesla.internal.throttler.QueueChannelThrottler;
import org.openhab.binding.tesla.internal.throttler.Rate;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaVehicleHandler} is responsible for handling commands, which are sent
 * to one of the channels of a specific vehicle.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Refactored to use separate account handler and improved configuration options
 */
public class TeslaVehicleHandler extends BaseThingHandler {

    private static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    private static final int SLOW_STATUS_REFRESH_INTERVAL = 60000;
    private static final int API_SLEEP_INTERVAL_MINUTES = 20;
    private static final int MOVE_THRESHOLD_INTERVAL_MINUTES_DEFAULT = 5;
    private static final int THRESHOLD_INTERVAL_FOR_ADVANCED_MINUTES = 60;
    private static final int EVENT_MAXIMUM_ERRORS_IN_INTERVAL = 10;
    private static final int EVENT_ERROR_INTERVAL_SECONDS = 15;
    private static final int EVENT_STREAM_PAUSE = 3000;
    private static final int EVENT_TIMESTAMP_AGE_LIMIT = 3000;
    private static final int EVENT_TIMESTAMP_MAX_DELTA = 10000;
    private static final int EVENT_PING_INTERVAL = 10000;

    private final Logger logger = LoggerFactory.getLogger(TeslaVehicleHandler.class);

    // Vehicle state variables
    protected Vehicle vehicle;
    protected String vehicleJSON;
    protected DriveState driveState;
    protected GUIState guiState;
    protected VehicleState vehicleState;
    protected ChargeState chargeState;
    protected ClimateState climateState;
    protected SoftwareUpdate softwareUpdate;

    protected boolean allowWakeUp;
    protected boolean allowWakeUpForCommands;
    protected boolean enableEvents = false;
    protected boolean useDriveState = false;
    protected boolean useAdvancedStates = false;
    protected boolean lastValidDriveStateNotNull = true;

    protected long lastTimeStamp;
    protected long apiIntervalTimestamp;
    protected int apiIntervalErrors;
    protected long eventIntervalTimestamp;
    protected int eventIntervalErrors;
    protected int inactivity = MOVE_THRESHOLD_INTERVAL_MINUTES_DEFAULT;
    protected ReentrantLock lock;

    protected double lastLongitude;
    protected double lastLatitude;
    protected long lastLocationChangeTimestamp;
    protected long lastDriveStateChangeToNullTimestamp;
    protected long lastAdvModesTimestamp = System.currentTimeMillis();
    protected long lastStateTimestamp = System.currentTimeMillis();
    protected int backOffCounter = 0;

    protected String lastState = "";
    protected boolean isInactive = false;

    protected TeslaAccountHandler account;

    protected QueueChannelThrottler stateThrottler;
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    protected Thread eventThread;
    protected ScheduledFuture<?> stateJob;
    protected WebSocketFactory webSocketFactory;

    private final Gson gson = new Gson();

    public TeslaVehicleHandler(Thing thing, WebSocketFactory webSocketFactory) {
        super(thing);
        this.webSocketFactory = webSocketFactory;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.trace("Initializing the Tesla handler for {}", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        allowWakeUp = (boolean) getConfig().get(TeslaBindingConstants.CONFIG_ALLOWWAKEUP);
        allowWakeUpForCommands = (boolean) getConfig().get(TeslaBindingConstants.CONFIG_ALLOWWAKEUPFORCOMMANDS);
        enableEvents = (boolean) getConfig().get(TeslaBindingConstants.CONFIG_ENABLEEVENTS);
        Number inactivityParam = (Number) getConfig().get(TeslaBindingConstants.CONFIG_INACTIVITY);
        inactivity = inactivityParam == null ? MOVE_THRESHOLD_INTERVAL_MINUTES_DEFAULT : inactivityParam.intValue();
        Boolean useDriveStateParam = (boolean) getConfig().get(TeslaBindingConstants.CONFIG_USEDRIVESTATE);
        useDriveState = useDriveStateParam == null ? false : useDriveStateParam;
        Boolean useAdvancedStatesParam = (boolean) getConfig().get(TeslaBindingConstants.CONFIG_USEDADVANCEDSTATES);
        useAdvancedStates = useAdvancedStatesParam == null ? false : useAdvancedStatesParam;

        account = (TeslaAccountHandler) getBridge().getHandler();
        lock = new ReentrantLock();
        scheduler.execute(this::queryVehicleAndUpdate);

        lock.lock();
        try {
            Map<Object, Rate> channels = new HashMap<>();
            channels.put(DATA_THROTTLE, new Rate(1, 1, TimeUnit.SECONDS));
            channels.put(COMMAND_THROTTLE, new Rate(20, 1, TimeUnit.MINUTES));

            Rate firstRate = new Rate(20, 1, TimeUnit.MINUTES);
            Rate secondRate = new Rate(200, 10, TimeUnit.MINUTES);
            stateThrottler = new QueueChannelThrottler(firstRate, scheduler, channels);
            stateThrottler.addRate(secondRate);

            if (stateJob == null || stateJob.isCancelled()) {
                stateJob = scheduler.scheduleWithFixedDelay(stateRunnable, 0, SLOW_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            if (enableEvents) {
                if (eventThread == null) {
                    eventThread = new Thread(eventRunnable, "OH-binding-" + getThing().getUID() + "-events");
                    eventThread.start();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        logger.trace("Disposing the Tesla handler for {}", getThing().getUID());
        lock.lock();
        try {
            if (stateJob != null && !stateJob.isCancelled()) {
                stateJob.cancel(true);
                stateJob = null;
            }

            if (eventThread != null && !eventThread.isInterrupted()) {
                eventThread.interrupt();
                eventThread = null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the unique vehicle id this handler is associated with
     *
     * @return the vehicle id
     */
    public String getVehicleId() {
        if (vehicle != null) {
            return vehicle.id;
        } else {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} {}", channelUID, command);
        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

        if (command instanceof RefreshType) {
            if (!isAwake()) {
                logger.debug("Waking vehicle to refresh all data");
                wakeUp();
            }

            setActive();

            // Request the state of all known variables. This is sub-optimal, but the requests get scheduled and
            // throttled so we are safe not to break the Tesla SLA
            requestAllData();
        } else if (selector != null) {
            if (!isAwake() && allowWakeUpForCommands) {
                logger.debug("Waking vehicle to send command.");
                wakeUp();
                setActive();
            }
            try {
                switch (selector) {
                    case CHARGE_LIMIT_SOC: {
                        if (command instanceof PercentType percentCommand) {
                            setChargeLimit(percentCommand.intValue());
                        } else if (command instanceof OnOffType && command == OnOffType.ON) {
                            setChargeLimit(100);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            setChargeLimit(0);
                        } else if (command instanceof IncreaseDecreaseType
                                && command == IncreaseDecreaseType.INCREASE) {
                            setChargeLimit(Math.min(chargeState.charge_limit_soc + 1, 100));
                        } else if (command instanceof IncreaseDecreaseType
                                && command == IncreaseDecreaseType.DECREASE) {
                            setChargeLimit(Math.max(chargeState.charge_limit_soc - 1, 0));
                        }
                        break;
                    }
                    case CHARGE_AMPS:
                        Integer amps = null;
                        if (command instanceof DecimalType decimalCommand) {
                            amps = decimalCommand.intValue();
                        }
                        if (command instanceof QuantityType<?> quantityCommand) {
                            QuantityType<?> qamps = quantityCommand.toUnit(Units.AMPERE);
                            if (qamps != null) {
                                amps = qamps.intValue();
                            }
                        }
                        if (amps != null) {
                            if (amps > 32) {
                                logger.warn("Charging amps cannot be set higher than 32A, {}A was requested", amps);
                                return;
                            }
                            if (amps < 5) {
                                logger.info("Charging amps should be set higher than 5A to avoid excessive losses.");
                            }
                            setChargingAmps(amps);
                        }
                        break;
                    case COMBINED_TEMP: {
                        QuantityType<Temperature> quantity = commandToQuantityType(command);
                        if (quantity != null) {
                            setCombinedTemperature(quanityToRoundedFloat(quantity));
                        }
                        break;
                    }
                    case DRIVER_TEMP: {
                        QuantityType<Temperature> quantity = commandToQuantityType(command);
                        if (quantity != null) {
                            setDriverTemperature(quanityToRoundedFloat(quantity));
                        }
                        break;
                    }
                    case PASSENGER_TEMP: {
                        QuantityType<Temperature> quantity = commandToQuantityType(command);
                        if (quantity != null) {
                            setPassengerTemperature(quanityToRoundedFloat(quantity));
                        }
                        break;
                    }
                    case SENTRY_MODE: {
                        if (command instanceof OnOffType) {
                            setSentryMode(command == OnOffType.ON);
                        }
                        break;
                    }
                    case SUN_ROOF_STATE: {
                        if (command instanceof StringType) {
                            setSunroof(command.toString());
                        }
                        break;
                    }
                    case CHARGE_TO_MAX: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                setMaxRangeCharging(true);
                            } else {
                                setMaxRangeCharging(false);
                            }
                        }
                        break;
                    }
                    case CHARGE: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                charge(true);
                            } else {
                                charge(false);
                            }
                        }
                        break;
                    }
                    case FLASH: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                flashLights();
                            }
                        }
                        break;
                    }
                    case HONK_HORN: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                honkHorn();
                            }
                        }
                        break;
                    }
                    case CHARGEPORT: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                openChargePort();
                            }
                        }
                        break;
                    }
                    case DOOR_LOCK: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                lockDoors(true);
                            } else {
                                lockDoors(false);
                            }
                        }
                        break;
                    }
                    case AUTO_COND: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                autoConditioning(true);
                            } else {
                                autoConditioning(false);
                            }
                        }
                        break;
                    }
                    case WAKEUP: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                wakeUp();
                            }
                        }
                        break;
                    }
                    case FT: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                openFrunk();
                            }
                        }
                        break;
                    }
                    case RT: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                if (vehicleState.rt == 0) {
                                    openTrunk();
                                }
                            } else if (vehicleState.rt == 1) {
                                closeTrunk();
                            }
                        }
                        break;
                    }
                    case VALET_MODE: {
                        if (command instanceof OnOffType onOffCommand) {
                            int valetpin = ((BigDecimal) getConfig().get(VALETPIN)).intValue();
                            if (onOffCommand == OnOffType.ON) {
                                setValetMode(true, valetpin);
                            } else {
                                setValetMode(false, valetpin);
                            }
                        }
                        break;
                    }
                    case RESET_VALET_PIN: {
                        if (command instanceof OnOffType onOffCommand) {
                            if (onOffCommand == OnOffType.ON) {
                                resetValetPin();
                            }
                        }
                        break;
                    }
                    case STEERINGWHEEL_HEATER: {
                        if (command instanceof OnOffType onOffCommand) {
                            boolean commandBooleanValue = onOffCommand == OnOffType.ON ? true : false;
                            setSteeringWheelHeater(commandBooleanValue);
                        }
                        break;
                    }
                    default:
                        break;
                }
                return;
            } catch (IllegalArgumentException e) {
                logger.warn(
                        "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                        channelID, command.toString());
            }
        }
    }

    public void sendCommand(String command, String payLoad, WebTarget target) {
        if (COMMAND_WAKE_UP.equals(command) || isAwake() || allowWakeUpForCommands) {
            Request request = account.newRequest(this, command, payLoad, target, allowWakeUpForCommands);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void sendCommand(String command) {
        sendCommand(command, "{}");
    }

    public void sendCommand(String command, String payLoad) {
        if (COMMAND_WAKE_UP.equals(command) || isAwake() || allowWakeUpForCommands) {
            Request request = account.newRequest(this, command, payLoad, account.commandTarget, allowWakeUpForCommands);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void sendCommand(String command, WebTarget target) {
        if (COMMAND_WAKE_UP.equals(command) || isAwake() || allowWakeUpForCommands) {
            Request request = account.newRequest(this, command, "{}", target, allowWakeUpForCommands);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void requestData(String command, String payLoad) {
        if (COMMAND_WAKE_UP.equals(command) || isAwake()
                || (!"vehicleData".equals(command) && allowWakeUpForCommands)) {
            Request request = account.newRequest(this, command, payLoad, account.dataRequestTarget, false);
            if (stateThrottler != null) {
                stateThrottler.submit(DATA_THROTTLE, request);
            }
        }
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public void requestData(String command) {
        requestData(command, null);
    }

    public void queryVehicle(String parameter) {
        WebTarget target = account.vehicleTarget.path(parameter);
        sendCommand(parameter, null, target);
    }

    public void requestAllData() {
        requestData("vehicleData", null);
    }

    protected boolean isAwake() {
        return vehicle != null && "online".equals(vehicle.state) && vehicle.vehicle_id != null;
    }

    protected boolean isInMotion() {
        if (driveState != null) {
            if (driveState.speed != null && driveState.shift_state != null) {
                return !"Undefined".equals(driveState.speed)
                        && (!"P".equals(driveState.shift_state) || !"Undefined".equals(driveState.shift_state));
            }
        }
        return false;
    }

    protected boolean isInactive() {
        // vehicle is inactive in case
        // - it does not charge
        // - it has not moved or optionally stopped reporting drive state, in the observation period
        // - it is not in dog, camp, keep, sentry or any other mode that keeps it online
        return isInactive && !isCharging() && !notReadyForSleep();
    }

    protected boolean isCharging() {
        return chargeState != null && "Charging".equals(chargeState.charging_state);
    }

    protected boolean notReadyForSleep() {
        boolean status;
        int computedInactivityPeriod = inactivity;

        if (useAdvancedStates) {
            if (vehicleState.is_user_present && !isInMotion()) {
                logger.debug("Car is occupied but stationary.");
                if (lastAdvModesTimestamp < (System.currentTimeMillis()
                        - (THRESHOLD_INTERVAL_FOR_ADVANCED_MINUTES * 60 * 1000))) {
                    logger.debug("Ignoring after {} minutes.", THRESHOLD_INTERVAL_FOR_ADVANCED_MINUTES);
                } else {
                    return (backOffCounter++ % 6 == 0); // using 6 should make sure 1 out of 5 pollers get serviced,
                                                        // about every min.
                }
            } else if (vehicleState.sentry_mode) {
                logger.debug("Car is in sentry mode.");
                if (lastAdvModesTimestamp < (System.currentTimeMillis()
                        - (THRESHOLD_INTERVAL_FOR_ADVANCED_MINUTES * 60 * 1000))) {
                    logger.debug("Ignoring after {} minutes.", THRESHOLD_INTERVAL_FOR_ADVANCED_MINUTES);
                } else {
                    return (backOffCounter++ % 6 == 0);
                }
            } else if ((vehicleState.center_display_state != 0) && (!isInMotion())) {
                logger.debug("Car is in camp, climate keep, dog, or other mode preventing sleep. Mode {}",
                        vehicleState.center_display_state);
                return (backOffCounter++ % 6 == 0);
            } else {
                lastAdvModesTimestamp = System.currentTimeMillis();
            }
        }

        if (vehicleState != null && vehicleState.homelink_nearby) {
            computedInactivityPeriod = MOVE_THRESHOLD_INTERVAL_MINUTES_DEFAULT;
            logger.debug("Car is at home. Movement or drive state threshold is {} min.",
                    MOVE_THRESHOLD_INTERVAL_MINUTES_DEFAULT);
        }

        if (useDriveState) {
            if (driveState.shift_state != null) {
                logger.debug("Car drive state not null and not ready to sleep.");
                return true;
            } else {
                status = lastDriveStateChangeToNullTimestamp > (System.currentTimeMillis()
                        - (computedInactivityPeriod * 60 * 1000));
                if (status) {
                    logger.debug("Drivestate is null but has changed recently, therefore continuing to poll.");
                    return status;
                } else {
                    logger.debug("Drivestate has changed to null after interval {} min and can now be put to sleep.",
                            computedInactivityPeriod);
                    return status;
                }
            }
        } else {
            status = lastLocationChangeTimestamp > (System.currentTimeMillis()
                    - (computedInactivityPeriod * 60 * 1000));
            if (status) {
                logger.debug("Car has moved recently and can not sleep");
                return status;
            } else {
                logger.debug("Car has not moved in {} min, and can sleep", computedInactivityPeriod);
                return status;
            }
        }
    }

    protected boolean allowQuery() {
        return (isAwake() && !isInactive());
    }

    protected void setActive() {
        isInactive = false;
        lastLocationChangeTimestamp = System.currentTimeMillis();
        lastDriveStateChangeToNullTimestamp = System.currentTimeMillis();
        lastLatitude = 0;
        lastLongitude = 0;
    }

    protected boolean checkResponse(Response response, boolean immediatelyFail) {
        if (response != null && response.getStatus() == 200) {
            return true;
        } else if (response != null && response.getStatus() == 401) {
            logger.debug("The access token has expired, trying to get a new one.");
            account.authenticate();
        } else {
            apiIntervalErrors++;
            if (immediatelyFail || apiIntervalErrors >= TeslaAccountHandler.API_MAXIMUM_ERRORS_IN_INTERVAL) {
                if (immediatelyFail) {
                    logger.warn("Got an unsuccessful result, setting vehicle to offline and will try again");
                } else {
                    logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                            TeslaAccountHandler.API_MAXIMUM_ERRORS_IN_INTERVAL,
                            TeslaAccountHandler.API_ERROR_INTERVAL_SECONDS);
                }

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else if ((System.currentTimeMillis() - apiIntervalTimestamp) > 1000
                    * TeslaAccountHandler.API_ERROR_INTERVAL_SECONDS) {
                logger.trace("Resetting the error counter. ({} errors in the last interval)", apiIntervalErrors);
                apiIntervalTimestamp = System.currentTimeMillis();
                apiIntervalErrors = 0;
            }
        }

        return false;
    }

    public void setChargeLimit(int percent) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("percent", percent);
        sendCommand(COMMAND_SET_CHARGE_LIMIT, gson.toJson(payloadObject), account.commandTarget);
    }

    public void setChargingAmps(int amps) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("charging_amps", amps);
        sendCommand(COMMAND_SET_CHARGING_AMPS, gson.toJson(payloadObject), account.commandTarget);
    }

    public void setSentryMode(boolean b) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("on", b);
        sendCommand(COMMAND_SET_SENTRY_MODE, gson.toJson(payloadObject), account.commandTarget);
    }

    public void setSunroof(String state) {
        if ("vent".equals(state) || "close".equals(state)) {
            JsonObject payloadObject = new JsonObject();
            payloadObject.addProperty("state", state);
            sendCommand(COMMAND_SUN_ROOF, gson.toJson(payloadObject), account.commandTarget);
        } else {
            logger.warn("Ignoring invalid command '{}' for sunroof.", state);
        }
    }

    /**
     * Sets the driver and passenger temperatures.
     *
     * While setting different temperature values is supported by the API, in practice this does not always work
     * reliably, possibly if the the
     * only reliable method is to set the driver and passenger temperature to the same value
     *
     * @param driverTemperature in Celsius
     * @param passenegerTemperature in Celsius
     */
    public void setTemperature(float driverTemperature, float passenegerTemperature) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("driver_temp", driverTemperature);
        payloadObject.addProperty("passenger_temp", passenegerTemperature);
        sendCommand(COMMAND_SET_TEMP, gson.toJson(payloadObject), account.commandTarget);
    }

    public void setCombinedTemperature(float temperature) {
        setTemperature(temperature, temperature);
    }

    public void setDriverTemperature(float temperature) {
        setTemperature(temperature, climateState != null ? climateState.passenger_temp_setting : temperature);
    }

    public void setPassengerTemperature(float temperature) {
        setTemperature(climateState != null ? climateState.driver_temp_setting : temperature, temperature);
    }

    public void openFrunk() {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("which_trunk", "front");
        sendCommand(COMMAND_ACTUATE_TRUNK, gson.toJson(payloadObject), account.commandTarget);
    }

    public void openTrunk() {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("which_trunk", "rear");
        sendCommand(COMMAND_ACTUATE_TRUNK, gson.toJson(payloadObject), account.commandTarget);
    }

    public void closeTrunk() {
        openTrunk();
    }

    public void setValetMode(boolean b, Integer pin) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("on", b);
        if (pin != null) {
            payloadObject.addProperty("password", String.format("%04d", pin));
        }
        sendCommand(COMMAND_SET_VALET_MODE, gson.toJson(payloadObject), account.commandTarget);
    }

    public void resetValetPin() {
        sendCommand(COMMAND_RESET_VALET_PIN, account.commandTarget);
    }

    public void setMaxRangeCharging(boolean b) {
        sendCommand(b ? COMMAND_CHARGE_MAX : COMMAND_CHARGE_STD, account.commandTarget);
    }

    public void charge(boolean b) {
        sendCommand(b ? COMMAND_CHARGE_START : COMMAND_CHARGE_STOP, account.commandTarget);
    }

    public void flashLights() {
        sendCommand(COMMAND_FLASH_LIGHTS, account.commandTarget);
    }

    public void honkHorn() {
        sendCommand(COMMAND_HONK_HORN, account.commandTarget);
    }

    public void openChargePort() {
        sendCommand(COMMAND_OPEN_CHARGE_PORT, account.commandTarget);
    }

    public void lockDoors(boolean b) {
        sendCommand(b ? COMMAND_DOOR_LOCK : COMMAND_DOOR_UNLOCK, account.commandTarget);
    }

    public void autoConditioning(boolean b) {
        sendCommand(b ? COMMAND_AUTO_COND_START : COMMAND_AUTO_COND_STOP, account.commandTarget);
    }

    public void wakeUp() {
        sendCommand(COMMAND_WAKE_UP, account.wakeUpTarget);
    }

    public void setSteeringWheelHeater(boolean isOn) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("on", isOn);
        sendCommand(COMMAND_STEERING_WHEEL_HEATER, gson.toJson(payloadObject), account.commandTarget);
    }

    protected Vehicle queryVehicle() {
        String authHeader = account.getAuthHeader();

        if (authHeader != null) {
            try {
                // get a list of vehicles
                synchronized (account.productsTarget) {
                    Response response = account.productsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", authHeader).get();

                    logger.debug("Querying the vehicle, response : {}, {}", response.getStatus(),
                            response.getStatusInfo().getReasonPhrase());

                    if (!checkResponse(response, true)) {
                        logger.debug("An error occurred while querying the vehicle");
                        return null;
                    }

                    JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();
                    Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

                    for (Vehicle vehicle : vehicleArray) {
                        logger.debug("Querying the vehicle: VIN {}", vehicle.vin);
                        if (vehicle.vin.equals(getConfig().get(VIN))) {
                            vehicleJSON = gson.toJson(vehicle);
                            parseAndUpdate("queryVehicle", null, vehicleJSON);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Vehicle is id {}/vehicle_id {}/tokens {}", vehicle.id, vehicle.vehicle_id,
                                        vehicle.tokens);
                            }
                            return vehicle;
                        }
                    }

                }
            } catch (ProcessingException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    protected void queryVehicleAndUpdate() {
        vehicle = queryVehicle();
    }

    public void parseAndUpdate(String request, String payLoad, String result) {
        final double locationThreshold = .0000001;

        try {
            if (request != null && result != null && !"null".equals(result)) {
                updateStatus(ThingStatus.ONLINE);
                updateState(CHANNEL_EVENTSTAMP, new DateTimeType());
                // first, update state objects
                if ("queryVehicle".equals(request)) {
                    if (vehicle != null) {
                        logger.debug("Vehicle state is {}", vehicle.state);
                        updateState(TeslaChannelSelector.STATE.getChannelID(), new StringType(vehicle.state));
                    } else {
                        logger.debug("Vehicle state is initializing or unknown");
                        return;
                    }

                    if (vehicle != null && ("asleep".equals(vehicle.state) || "offline".equals(vehicle.state))) {
                        logger.debug("Vehicle is {}", vehicle.state);
                        return;
                    }

                    if (vehicle != null && !lastState.equals(vehicle.state)) {
                        lastState = vehicle.state;

                        // in case vehicle changed to awake, refresh all data
                        if (isAwake()) {
                            logger.debug("Vehicle is now awake, updating all data");
                            lastLocationChangeTimestamp = System.currentTimeMillis();
                            lastDriveStateChangeToNullTimestamp = System.currentTimeMillis();
                            requestAllData();
                        }

                        setActive();
                    }

                    // reset timestamp if elapsed and set inactive to false
                    if (isInactive && lastStateTimestamp + (API_SLEEP_INTERVAL_MINUTES * 60 * 1000) < System
                            .currentTimeMillis()) {
                        logger.debug("Vehicle did not fall asleep within sleep period, checking again");
                        setActive();
                    } else {
                        boolean wasInactive = isInactive;
                        isInactive = !isCharging() && !notReadyForSleep();

                        if (!wasInactive && isInactive) {
                            lastStateTimestamp = System.currentTimeMillis();
                            logger.debug("Vehicle is inactive");
                        }
                    }
                } else if ("vehicleData".equals(request)) {
                    VehicleData vehicleData = gson.fromJson(result, VehicleData.class);
                    if (vehicleData == null) {
                        logger.error("Not able to parse response '{}'", result);
                        return;
                    }

                    driveState = vehicleData.drive_state;
                    if (Math.abs(lastLatitude - driveState.latitude) > locationThreshold
                            || Math.abs(lastLongitude - driveState.longitude) > locationThreshold) {
                        logger.debug("Vehicle moved, resetting last location timestamp");

                        lastLatitude = driveState.latitude;
                        lastLongitude = driveState.longitude;
                        lastLocationChangeTimestamp = System.currentTimeMillis();
                    }
                    logger.trace("Drive state: {}", driveState.shift_state);

                    if ((driveState.shift_state == null) && (lastValidDriveStateNotNull)) {
                        logger.debug("Set NULL shiftstate time");
                        lastValidDriveStateNotNull = false;
                        lastDriveStateChangeToNullTimestamp = System.currentTimeMillis();
                    } else if (driveState.shift_state != null) {
                        logger.trace("Clear NULL shiftstate time");
                        lastValidDriveStateNotNull = true;
                    }

                    guiState = vehicleData.gui_settings;

                    vehicleState = vehicleData.vehicle_state;

                    chargeState = vehicleData.charge_state;
                    if (isCharging()) {
                        updateState(CHANNEL_CHARGE, OnOffType.ON);
                    } else {
                        updateState(CHANNEL_CHARGE, OnOffType.OFF);
                    }

                    climateState = vehicleData.climate_state;
                    BigDecimal avgtemp = roundBigDecimal(new BigDecimal(
                            (climateState.driver_temp_setting + climateState.passenger_temp_setting) / 2.0f));
                    updateState(CHANNEL_COMBINED_TEMP, new QuantityType<>(avgtemp, SIUnits.CELSIUS));

                    softwareUpdate = vehicleState.software_update;

                    try {
                        lock.lock();

                        Set<Map.Entry<String, JsonElement>> entrySet = new HashSet<>();

                        entrySet.addAll(gson.toJsonTree(driveState, DriveState.class).getAsJsonObject().entrySet());
                        entrySet.addAll(gson.toJsonTree(guiState, GUIState.class).getAsJsonObject().entrySet());
                        entrySet.addAll(gson.toJsonTree(vehicleState, VehicleState.class).getAsJsonObject().entrySet());
                        entrySet.addAll(gson.toJsonTree(chargeState, ChargeState.class).getAsJsonObject().entrySet());
                        entrySet.addAll(gson.toJsonTree(climateState, ClimateState.class).getAsJsonObject().entrySet());
                        entrySet.addAll(
                                gson.toJsonTree(softwareUpdate, SoftwareUpdate.class).getAsJsonObject().entrySet());

                        for (Map.Entry<String, JsonElement> entry : entrySet) {
                            try {
                                TeslaChannelSelector selector = TeslaChannelSelector
                                        .getValueSelectorFromRESTID(entry.getKey());
                                if (!selector.isProperty()) {
                                    if (!entry.getValue().isJsonNull()) {
                                        updateState(selector.getChannelID(), teslaChannelSelectorProxy
                                                .getState(entry.getValue().getAsString(), selector, editProperties()));
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("The variable/value pair '{}':'{}' is successfully processed",
                                                    entry.getKey(), entry.getValue());
                                        }
                                    } else {
                                        updateState(selector.getChannelID(), UnDefType.UNDEF);
                                    }
                                } else if (!entry.getValue().isJsonNull()) {
                                    Map<String, String> properties = editProperties();
                                    properties.put(selector.getChannelID(), entry.getValue().getAsString());
                                    updateProperties(properties);
                                    if (logger.isTraceEnabled()) {
                                        logger.trace(
                                                "The variable/value pair '{}':'{}' is successfully used to set property '{}'",
                                                entry.getKey(), entry.getValue(), selector.getChannelID());
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                logger.trace("The variable/value pair '{}':'{}' is not (yet) supported", entry.getKey(),
                                        entry.getValue());
                            } catch (ClassCastException | IllegalStateException e) {
                                logger.trace("An exception occurred while converting the JSON data : '{}'",
                                        e.getMessage(), e);
                            }
                        }

                        if (softwareUpdate.version == null || softwareUpdate.version.isBlank()) {
                            updateState(CHANNEL_SOFTWARE_UPDATE_AVAILABLE, OnOffType.OFF);
                        } else {
                            updateState(CHANNEL_SOFTWARE_UPDATE_AVAILABLE, OnOffType.ON);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (Exception p) {
            logger.error("An exception occurred while parsing data received from the vehicle: '{}'", p.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    protected QuantityType<Temperature> commandToQuantityType(Command command) {
        if (command instanceof QuantityType) {
            return ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS);
        }
        return new QuantityType<>(new BigDecimal(command.toString()), SIUnits.CELSIUS);
    }

    protected float quanityToRoundedFloat(QuantityType<Temperature> quantity) {
        return roundBigDecimal(quantity.toBigDecimal()).floatValue();
    }

    protected BigDecimal roundBigDecimal(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_EVEN);
    }

    protected Runnable stateRunnable = () -> {
        try {
            queryVehicleAndUpdate();
            boolean allowQuery = allowQuery();

            if (allowQuery) {
                requestAllData();
            } else if (allowWakeUp) {
                wakeUp();
            } else if (isAwake()) {
                logger.debug("Throttled state polling to allow sleep, occupied/idle, or in a console mode");
            } else {
                lastAdvModesTimestamp = System.currentTimeMillis();
            }
        } catch (Exception e) {
            logger.warn("Exception occurred in stateRunnable", e);
        }
    };

    protected Runnable eventRunnable = new Runnable() {
        TeslaEventEndpoint eventEndpoint;
        boolean isAuthenticated = false;
        long lastPingTimestamp = 0;

        @Override
        public void run() {
            eventEndpoint = new TeslaEventEndpoint(getThing().getUID(), webSocketFactory);
            eventEndpoint.addEventHandler(new TeslaEventEndpoint.EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    if (event != null) {
                        switch (event.msg_type) {
                            case "control:hello":
                                logger.debug("Event : Received hello");
                                break;
                            case "data:update":
                                logger.debug("Event : Received an update: '{}'", event.value);

                                String[] vals = event.value.split(",");
                                long currentTimeStamp = Long.parseLong(vals[0]);
                                long systemTimeStamp = System.currentTimeMillis();
                                if (logger.isDebugEnabled()) {
                                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                    logger.debug("STS {} CTS {} Delta {}",
                                            dateFormatter.format(new Date(systemTimeStamp)),
                                            dateFormatter.format(new Date(currentTimeStamp)),
                                            systemTimeStamp - currentTimeStamp);
                                }
                                if (systemTimeStamp - currentTimeStamp < EVENT_TIMESTAMP_AGE_LIMIT) {
                                    if (currentTimeStamp > lastTimeStamp) {
                                        lastTimeStamp = Long.parseLong(vals[0]);
                                        if (logger.isDebugEnabled()) {
                                            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                            logger.debug("Event : Event stamp is {}",
                                                    dateFormatter.format(new Date(lastTimeStamp)));
                                        }
                                        for (int i = 0; i < EventKeys.values().length; i++) {
                                            TeslaChannelSelector selector = TeslaChannelSelector
                                                    .getValueSelectorFromRESTID((EventKeys.values()[i]).toString());

                                            if (!selector.isProperty()) {
                                                State newState = teslaChannelSelectorProxy.getState(vals[i], selector,
                                                        editProperties());
                                                if (newState != null && !"".equals(vals[i])) {
                                                    updateState(selector.getChannelID(), newState);
                                                } else {
                                                    updateState(selector.getChannelID(), UnDefType.UNDEF);
                                                }
                                                if (logger.isTraceEnabled()) {
                                                    logger.trace(
                                                            "The variable/value pair '{}':'{}' is successfully processed",
                                                            EventKeys.values()[i], vals[i]);
                                                }
                                            } else {
                                                Map<String, String> properties = editProperties();
                                                properties.put(selector.getChannelID(),
                                                        (selector.getState(vals[i])).toString());
                                                updateProperties(properties);
                                                if (logger.isTraceEnabled()) {
                                                    logger.trace(
                                                            "The variable/value pair '{}':'{}' is successfully used to set property '{}'",
                                                            EventKeys.values()[i], vals[i], selector.getChannelID());
                                                }
                                            }
                                        }
                                    } else if (logger.isDebugEnabled()) {
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                        logger.debug(
                                                "Event : Discarding an event with an out of sync timestamp {} (last is {})",
                                                dateFormatter.format(new Date(currentTimeStamp)),
                                                dateFormatter.format(new Date(lastTimeStamp)));
                                    }
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                        logger.debug(
                                                "Event : Discarding an event that differs {} ms from the system time: {} (system is {})",
                                                systemTimeStamp - currentTimeStamp,
                                                dateFormatter.format(currentTimeStamp),
                                                dateFormatter.format(systemTimeStamp));
                                    }
                                    if (systemTimeStamp - currentTimeStamp > EVENT_TIMESTAMP_MAX_DELTA) {
                                        logger.trace("Event : The event endpoint will be reset");
                                        eventEndpoint.closeConnection();
                                    }
                                }
                                break;
                            case "data:error":
                                logger.debug("Event : Received an error: '{}'/'{}'", event.value, event.error_type);
                                eventEndpoint.closeConnection();
                                break;
                        }
                    }
                }
            });

            while (true) {
                try {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        if (isAwake()) {
                            eventEndpoint.connect(new URI(URI_EVENT));

                            if (eventEndpoint.isConnected()) {
                                if (!isAuthenticated) {
                                    logger.debug("Event : Authenticating vehicle {}", vehicle.vehicle_id);
                                    JsonObject payloadObject = new JsonObject();
                                    payloadObject.addProperty("msg_type", "data:subscribe_oauth");
                                    payloadObject.addProperty("token", account.getAccessToken());
                                    payloadObject.addProperty("value", Arrays.asList(EventKeys.values()).stream()
                                            .skip(1).map(Enum::toString).collect(Collectors.joining(",")));
                                    payloadObject.addProperty("tag", vehicle.vehicle_id);

                                    eventEndpoint.sendMessage(gson.toJson(payloadObject));
                                    isAuthenticated = true;

                                    lastPingTimestamp = System.nanoTime();
                                }

                                if (TimeUnit.MILLISECONDS.convert(System.nanoTime() - lastPingTimestamp,
                                        TimeUnit.NANOSECONDS) > EVENT_PING_INTERVAL) {
                                    logger.trace("Event : Pinging the Tesla event stream infrastructure");
                                    eventEndpoint.ping();
                                    lastPingTimestamp = System.nanoTime();
                                }
                            }

                            if (!eventEndpoint.isConnected()) {
                                isAuthenticated = false;
                                eventIntervalErrors++;
                                if (eventIntervalErrors >= EVENT_MAXIMUM_ERRORS_IN_INTERVAL) {
                                    logger.warn(
                                            "Event : Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                                            EVENT_MAXIMUM_ERRORS_IN_INTERVAL, EVENT_ERROR_INTERVAL_SECONDS);
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                                    eventEndpoint.closeConnection();
                                }

                                if ((System.currentTimeMillis() - eventIntervalTimestamp) > 1000
                                        * EVENT_ERROR_INTERVAL_SECONDS) {
                                    logger.trace(
                                            "Event : Resetting the error counter. ({} errors in the last interval)",
                                            eventIntervalErrors);
                                    eventIntervalTimestamp = System.currentTimeMillis();
                                    eventIntervalErrors = 0;
                                }
                            }
                        } else {
                            logger.debug("Event : The vehicle is not awake");
                            if (vehicle != null) {
                                if (allowWakeUp) {
                                    // wake up the vehicle until streaming token <> 0
                                    logger.debug("Event : Waking up the vehicle");
                                    wakeUp();
                                }
                            } else {
                                vehicle = queryVehicle();
                            }
                        }
                    }
                } catch (URISyntaxException | NumberFormatException | IOException e) {
                    logger.debug("Event : An exception occurred while processing events: '{}'", e.getMessage());
                }

                try {
                    Thread.sleep(EVENT_STREAM_PAUSE);
                } catch (InterruptedException e) {
                    logger.debug("Event : An exception occurred while putting the event thread to sleep: '{}'",
                            e.getMessage());
                }

                if (Thread.interrupted()) {
                    logger.debug("Event : The event thread was interrupted");
                    eventEndpoint.close();
                    return;
                }
            }
        }
    };
}
