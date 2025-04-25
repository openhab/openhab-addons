/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.MyBMWVehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingProfile;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSession;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSettings;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.CheckControlMessage;
import org.openhab.binding.mybmw.internal.dto.vehicle.RequiredService;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleDoorsState;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleLocation;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleRoofState;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleState;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleTireStates;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleWindowsState;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.handler.backend.NetworkException;
import org.openhab.binding.mybmw.internal.utils.ChargingProfileUtils;
import org.openhab.binding.mybmw.internal.utils.ChargingProfileUtils.TimedChannel;
import org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper;
import org.openhab.binding.mybmw.internal.utils.ChargingProfileWrapper.ProfileKey;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} handles responses from BMW API
 *
 * the introduction of channelToBeUpdated is ugly, but if there is a refresh of one channel, always all channels were
 * updated
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit and send charge profile
 * @author Martin Grassl - refactoring, merge with VehicleChannelHandler
 * @author Mark Herwege - refactoring, V2 API charging
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private boolean hasFuel = false;
    private boolean isElectric = false;
    private boolean isHybrid = false;

    // List Interfaces
    private volatile List<RequiredService> serviceList = List.of();
    private volatile String selectedService = Constants.UNDEF;
    private volatile List<CheckControlMessage> checkControlList = List.of();
    private volatile String selectedCC = Constants.UNDEF;
    private volatile List<ChargingSession> sessionList = List.of();
    private volatile String selectedSession = Constants.UNDEF;

    private MyBMWCommandOptionProvider commandOptionProvider;
    private LocationProvider locationProvider;
    private TimeZoneProvider timeZoneProvider;

    // Data Caches
    private Optional<VehicleStateContainer> vehicleStatusCache = Optional.empty();
    private Optional<byte[]> imageCache = Optional.empty();

    private Optional<MyBMWProxy> proxy = Optional.empty();
    private Optional<RemoteServiceExecutor> remote = Optional.empty();
    private Optional<MyBMWVehicleConfiguration> vehicleConfiguration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> editTimeout = Optional.empty();

    private ImageProperties imageProperties = new ImageProperties();

    private ThingStatus currentStatus = ThingStatus.UNKNOWN;

    public VehicleHandler(Thing thing, MyBMWCommandOptionProvider commandOptionProvider,
            LocationProvider locationProvider, TimeZoneProvider timeZoneProvider, String driveTrain) {
        super(thing);
        logger.trace("VehicleHandler.constructor {}, {}", thing.getUID(), driveTrain);
        this.commandOptionProvider = commandOptionProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.locationProvider = locationProvider;
        if (locationProvider.getLocation() == null) {
            logger.debug("Home location not available");
        }

        hasFuel = driveTrain.equals(VehicleType.CONVENTIONAL.toString())
                || driveTrain.equals(VehicleType.PLUGIN_HYBRID.toString())
                || driveTrain.equals(VehicleType.ELECTRIC_REX.toString())
                || driveTrain.equals(VehicleType.MILD_HYBRID.toString());
        isElectric = driveTrain.equals(VehicleType.PLUGIN_HYBRID.toString())
                || driveTrain.equals(VehicleType.ELECTRIC_REX.toString())
                || driveTrain.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;

        setOptions(CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_COMMAND, RemoteServiceUtils.getOptions(isElectric));
    }

    private void setOptions(final String group, final String id, List<CommandOption> options) {
        commandOptionProvider.setCommandOptions(new ChannelUID(thing.getUID(), group, id), options);
    }

    @Override
    public void initialize() {
        logger.trace("VehicleHandler.initialize");
        currentStatus = ThingStatus.UNKNOWN;
        updateStatus(currentStatus);
        vehicleConfiguration = Optional.of(getConfigAs(MyBMWVehicleConfiguration.class));

        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                // Can be long running if we have to wait for the bridge to be initialized
                scheduler.submit(() -> {
                    proxy = ((MyBMWBridgeHandler) handler).getMyBmwProxy();
                    remote = Optional.of(new RemoteServiceExecutor(this, proxy.get()));

                    imageProperties = new ImageProperties();
                    updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT,
                            Converter.toTitleCase(imageProperties.viewport), null);

                    startSchedule(vehicleConfiguration.get().getRefreshInterval());
                });
            } else {
                logger.debug("Bridge Handler null");
            }
        } else {
            logger.debug("Bridge null");
        }
    }

    private void startSchedule(int interval) {
        // start update schedule only if the refreshInterval is not 0
        if (interval > 0) {
            logger.info("VehicleHandler.startSchedule with interval {}min", interval);
            refreshJob.ifPresentOrElse(job -> {
                if (job.isCancelled()) {
                    refreshJob = Optional
                            .of(scheduler.scheduleWithFixedDelay(this::updateData, 0, interval, TimeUnit.MINUTES));
                } // else - scheduler is already running!
            }, () -> {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::updateData, 0, interval, TimeUnit.MINUTES));
            });
        } else {
            logger.info("VehicleHandler initialize: don't start schedule as interval is 0");
            updateData();
        }
    }

    @Override
    public void dispose() {
        logger.trace("VehicleHandler.dispose");
        refreshJob.ifPresent(job -> job.cancel(true));
        editTimeout.ifPresent(job -> job.cancel(true));
        remote.ifPresent(RemoteServiceExecutor::cancel);
    }

    /**
     * update all data
     */
    void updateData() {
        logger.trace("VehicleHandler.updateData");
        updateVehicleStatus();
        if (isElectric) {
            updateCharging();
        }
        updateImage();
    }

    private void updateVehicleStatus() {
        proxy.ifPresentOrElse(prox -> {
            vehicleConfiguration.ifPresentOrElse(config -> {
                try {
                    VehicleStateContainer vehicleState = prox.requestVehicleState(config.getVin(),
                            config.getVehicleBrand());
                    triggerVehicleStatusUpdate(vehicleState, null);
                    currentStatus = ThingStatus.ONLINE;
                    updateStatus(currentStatus);
                } catch (NetworkException e) {
                    logger.debug("{}", e.toString());
                    currentStatus = ThingStatus.OFFLINE;
                    updateStatus(currentStatus, ThingStatusDetail.COMMUNICATION_ERROR, "Vehicle State Update failed");
                }
            }, () -> {
                logger.warn("MyBMW Vehicle Configuration isn't present");
            });
        }, () -> {
            logger.warn("MyBMWProxy isn't present");
        });
    }

    private void updateCharging() {
        proxy.ifPresentOrElse(prox -> {
            vehicleConfiguration.ifPresentOrElse(config -> {
                if (isElectric && ThingStatus.ONLINE.equals(currentStatus)) {
                    try {
                        updateChargingStatistics(
                                prox.requestChargeStatistics(config.getVin(), config.getVehicleBrand()), null);
                        updateChargingSessions(prox.requestChargeSessions(config.getVin(), config.getVehicleBrand()),
                                null);
                    } catch (NetworkException e) {
                        logger.debug("{}", e.toString());
                    }
                }
            }, () -> {
                logger.warn("MyBMW Vehicle Configuration isn't present");
            });
        }, () -> {
            logger.warn("MyBMWProxy isn't present");
        });
    }

    private void updateImage() {
        proxy.ifPresentOrElse(prox -> {
            vehicleConfiguration.ifPresentOrElse(config -> {
                if (!imageCache.isPresent() && !imageProperties.failLimitReached()
                        && ThingStatus.ONLINE.equals(currentStatus)) {
                    try {
                        updateImage(prox.requestImage(config.getVin(), config.getVehicleBrand(), imageProperties));
                    } catch (NetworkException e) {
                        logger.debug("{}", e.toString());
                    }
                }
            }, () -> {
                logger.warn("MyBMW Vehicle Configuration isn't present");
            });
        }, () -> {
            logger.warn("MyBMWProxy isn't present");
        });
    }

    private void triggerVehicleStatusUpdate(VehicleStateContainer vehicleState, @Nullable String channelToBeUpdated) {
        logger.trace("VehicleHandler.triggerVehicleStatusUpdate for {}", channelToBeUpdated);
        if (vehicleConfiguration.isPresent()) {
            vehicleStatusCache = Optional.of(vehicleState);
            updateChannel(CHANNEL_GROUP_STATUS, RAW, vehicleState.getRawStateJson(), channelToBeUpdated);

            updateVehicleStatus(vehicleState.getState(), channelToBeUpdated);
            if (isElectric) {
                updateChargingProfile(vehicleState.getState().getChargingProfile(), channelToBeUpdated);
            }
        } else {
            logger.debug("configuration not present");
        }
    }

    public void updateRemoteExecutionStatus(@Nullable String service, String status) {
        updateChannel(CHANNEL_GROUP_REMOTE, REMOTE_STATE,
                (service == null ? "-" : service) + Constants.SPACE + status.toLowerCase(), null);
    }

    public Optional<MyBMWVehicleConfiguration> getVehicleConfiguration() {
        logger.trace("VehicleHandler.getVehicleConfiguration");
        return vehicleConfiguration;
    }

    public ScheduledExecutorService getScheduler() {
        logger.trace("VehicleHandler.getScheduler");
        return scheduler;
    }

    private void updateChannel(final String group, final String id, final String state,
            @Nullable final String channelToBeUpdated) {
        updateChannel(group, id, StringType.valueOf(state), channelToBeUpdated);
    }

    /**
     * this method sets the state for a single channel. if a channelToBeUpdated is provided, the update will only take
     * place for that single channel.
     */
    private void updateChannel(final String group, final String id, final State state,
            @Nullable final String channelToBeUpdated) {
        if (channelToBeUpdated == null || id.equals(channelToBeUpdated)) {
            if (!"png".equals(id)) {
                logger.trace("updating channel {}, {}, {}", group, id, state.toFullString());
            } else {
                logger.trace("updating channel {}, {}, {}", group, id, "not printed");
            }

            updateState(new ChannelUID(thing.getUID(), group, id), state);
        }
    }

    private void updateChargingStatistics(ChargingStatisticsContainer chargingStatisticsContainer,
            @Nullable String channelToBeUpdated) {
        if (!"".equals(chargingStatisticsContainer.getDescription())) {
            updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, TITLE, chargingStatisticsContainer.getDescription(),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, ENERGY, QuantityType
                    .valueOf(chargingStatisticsContainer.getStatistics().getTotalEnergyCharged(), Units.KILOWATT_HOUR),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, SESSIONS,
                    DecimalType.valueOf(Integer
                            .toString(chargingStatisticsContainer.getStatistics().getNumberOfChargingSessions())),
                    channelToBeUpdated);
        }
    }

    /**
     * updates the channels with the current state of the vehicle
     *
     * @param vehicleStateState
     */
    private void updateVehicleStatus(VehicleState vehicleStateState, @Nullable String channelToBeUpdated) {
        boolean isLeftSteering = vehicleStateState.isLeftSteering();

        updateVehicleOverallStatus(vehicleStateState, channelToBeUpdated);
        updateRange(vehicleStateState, channelToBeUpdated);
        updateDoors(vehicleStateState.getDoorsState(), isLeftSteering, channelToBeUpdated);
        updateWindows(vehicleStateState.getWindowsState(), isLeftSteering, channelToBeUpdated);
        updateRoof(vehicleStateState.getRoofState(), channelToBeUpdated);
        updatePosition(vehicleStateState.getLocation(), channelToBeUpdated);
        updateServices(vehicleStateState.getRequiredServices(), channelToBeUpdated);
        updateCheckControls(vehicleStateState.getCheckControlMessages(), channelToBeUpdated);
        updateTires(vehicleStateState.getTireState(), channelToBeUpdated);
    }

    private void updateTires(@Nullable VehicleTireStates vehicleTireStates, @Nullable String channelToBeUpdated) {
        if (vehicleTireStates == null) {
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_CURRENT, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_TARGET, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_CURRENT, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_TARGET, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_CURRENT, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_TARGET, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_CURRENT, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_TARGET, UnDefType.UNDEF, channelToBeUpdated);
        } else {
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_CURRENT,
                    calculatePressure(vehicleTireStates.getFrontLeft().getStatus().getCurrentPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_TARGET,
                    calculatePressure(vehicleTireStates.getFrontLeft().getStatus().getTargetPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_CURRENT,
                    calculatePressure(vehicleTireStates.getFrontRight().getStatus().getCurrentPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_TARGET,
                    calculatePressure(vehicleTireStates.getFrontRight().getStatus().getTargetPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_CURRENT,
                    calculatePressure(vehicleTireStates.getRearLeft().getStatus().getCurrentPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_TARGET,
                    calculatePressure(vehicleTireStates.getRearLeft().getStatus().getTargetPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_CURRENT,
                    calculatePressure(vehicleTireStates.getRearRight().getStatus().getCurrentPressure()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_TARGET,
                    calculatePressure(vehicleTireStates.getRearRight().getStatus().getTargetPressure()),
                    channelToBeUpdated);
        }
    }

    /**
     * if the pressure is undef it is < 0
     *
     * @param pressure
     * @return
     */
    private State calculatePressure(int pressure) {
        if (pressure > 0) {
            return QuantityType.valueOf(pressure / 100.0, Units.BAR);
        } else {
            return UnDefType.UNDEF;
        }
    }

    private void updateVehicleOverallStatus(VehicleState vehicleState, @Nullable String channelToBeUpdated) {
        updateChannel(CHANNEL_GROUP_STATUS, LOCK,
                Converter.toTitleCase(vehicleState.getDoorsState().getCombinedSecurityState()), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_DATE,
                VehicleStatusUtils.getNextServiceDate(vehicleState.getRequiredServices()), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_MILEAGE,
                VehicleStatusUtils.getNextServiceMileage(vehicleState.getRequiredServices()), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, CHECK_CONTROL,
                Converter.toTitleCase(vehicleState.getOverallCheckControlStatus()), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, LAST_UPDATE,
                Converter.zonedToLocalDateTime(vehicleState.getLastUpdatedAt(), timeZoneProvider.getTimeZone()),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, LAST_FETCHED,
                Converter.zonedToLocalDateTime(vehicleState.getLastFetched(), timeZoneProvider.getTimeZone()),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, DOORS,
                Converter.toTitleCase(vehicleState.getDoorsState().getCombinedState()), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_STATUS, WINDOWS,
                Converter.toTitleCase(vehicleState.getWindowsState().getCombinedState()), channelToBeUpdated);

        if (isElectric) {
            updateChannel(CHANNEL_GROUP_STATUS, PLUG_CONNECTION,
                    Converter.getConnectionState(vehicleState.getElectricChargingState().isChargerConnected()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_STATUS, CHARGE_STATUS,
                    Converter.toTitleCase(vehicleState.getElectricChargingState().getChargingStatus()),
                    channelToBeUpdated);

            int remainingTime = vehicleState.getElectricChargingState().getRemainingChargingMinutes();
            updateChannel(CHANNEL_GROUP_STATUS, CHARGE_REMAINING,
                    remainingTime >= 0 ? QuantityType.valueOf(remainingTime, Units.MINUTE) : UnDefType.UNDEF,
                    channelToBeUpdated);
        }
    }

    private void updateRange(VehicleState vehicleState, @Nullable String channelToBeUpdated) {
        // get the right unit
        Unit<Length> lengthUnit = Constants.KILOMETRE_UNIT;

        if (isElectric) {
            int rangeElectric = vehicleState.getElectricChargingState().getRange();
            QuantityType<Length> qtElectricRange = QuantityType.valueOf(rangeElectric, lengthUnit);
            QuantityType<Length> qtElectricRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeElectric),
                    lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_ELECTRIC, qtElectricRange, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC, qtElectricRadius, channelToBeUpdated);
        }

        if (hasFuel && !isHybrid) {
            int rangeFuel = vehicleState.getCombustionFuelLevel().getRange();
            QuantityType<Length> qtFuelRange = QuantityType.valueOf(rangeFuel, lengthUnit);
            QuantityType<Length> qtFuelRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeFuel), lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_FUEL, qtFuelRange, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL, qtFuelRadius, channelToBeUpdated);
        }

        if (isHybrid) {
            int rangeCombined = vehicleState.getRange();

            // there is a bug/feature in the API that the fuel range is the same like the combined range, hence in case
            // of hybrid the fuel range has to be subtracted by the electric range
            int rangeFuel = vehicleState.getCombustionFuelLevel().getRange()
                    - vehicleState.getElectricChargingState().getRange();

            QuantityType<Length> qtHybridRange = QuantityType.valueOf(rangeCombined, lengthUnit);
            QuantityType<Length> qtHybridRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeCombined),
                    lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_HYBRID, qtHybridRange, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID, qtHybridRadius, channelToBeUpdated);

            QuantityType<Length> qtFuelRange = QuantityType.valueOf(rangeFuel, lengthUnit);
            QuantityType<Length> qtFuelRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeFuel), lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_FUEL, qtFuelRange, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL, qtFuelRadius, channelToBeUpdated);
        }

        if (vehicleState.getCurrentMileage() == Constants.INT_UNDEF) {
            updateChannel(CHANNEL_GROUP_RANGE, MILEAGE, UnDefType.UNDEF, channelToBeUpdated);
        } else {
            updateChannel(CHANNEL_GROUP_RANGE, MILEAGE,
                    QuantityType.valueOf(vehicleState.getCurrentMileage(), lengthUnit), channelToBeUpdated);
        }
        if (isElectric) {
            updateChannel(
                    CHANNEL_GROUP_RANGE, SOC, QuantityType
                            .valueOf(vehicleState.getElectricChargingState().getChargingLevelPercent(), Units.PERCENT),
                    channelToBeUpdated);
        }
        if (hasFuel) {
            updateChannel(CHANNEL_GROUP_RANGE, REMAINING_FUEL,
                    QuantityType.valueOf(vehicleState.getCombustionFuelLevel().getRemainingFuelLiters(), Units.LITRE),
                    channelToBeUpdated);

            if (vehicleState.getCombustionFuelLevel().getRemainingFuelLiters() > 0
                    && vehicleState.getCombustionFuelLevel().getRange() > 1) {
                double estimatedFuelConsumption = vehicleState.getCombustionFuelLevel().getRemainingFuelLiters() * 1.0
                        / vehicleState.getCombustionFuelLevel().getRange() * 100.0;
                updateChannel(CHANNEL_GROUP_RANGE, ESTIMATED_FUEL_L_100KM,
                        DecimalType.valueOf(estimatedFuelConsumption + ""), channelToBeUpdated);
                updateChannel(CHANNEL_GROUP_RANGE, ESTIMATED_FUEL_MPG,
                        DecimalType.valueOf((235.214583 / estimatedFuelConsumption) + ""), channelToBeUpdated);
            } else {
                updateChannel(CHANNEL_GROUP_RANGE, ESTIMATED_FUEL_L_100KM, UnDefType.UNDEF, channelToBeUpdated);
                updateChannel(CHANNEL_GROUP_RANGE, ESTIMATED_FUEL_MPG, UnDefType.UNDEF, channelToBeUpdated);
            }
        }
    }

    private void updateCheckControls(List<CheckControlMessage> checkControlMessages,
            @Nullable String channelToBeUpdated) {
        if (checkControlMessages.isEmpty()) {
            // No Check Control available - show not active
            CheckControlMessage checkControlMessage = new CheckControlMessage();
            checkControlMessage.setName(Constants.NO_ENTRIES);
            checkControlMessage.setDescription(Constants.NO_ENTRIES);
            checkControlMessage.setSeverity(Constants.NO_ENTRIES);
            checkControlMessage.setType(Constants.NO_ENTRIES);
            checkControlMessage.setId(-1);
            checkControlMessages.add(checkControlMessage);
        }

        // add all elements to options
        checkControlList = checkControlMessages;
        List<CommandOption> ccmDescriptionOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CheckControlMessage checkControlMessage : checkControlList) {
            ccmDescriptionOptions.add(
                    new CommandOption(Integer.toString(index), Converter.toTitleCase(checkControlMessage.getType())));
            if (selectedCC.equals(checkControlMessage.getType())) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_CHECK_CONTROL, NAME, ccmDescriptionOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectCheckControl(0, channelToBeUpdated);
        }
    }

    private void selectCheckControl(int index, @Nullable String channelToBeUpdated) {
        if (index >= 0 && index < checkControlList.size()) {
            CheckControlMessage checkControlMessage = checkControlList.get(index);
            selectedCC = checkControlMessage.getType();
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, NAME, Converter.toTitleCase(checkControlMessage.getType()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, DETAILS,
                    StringType.valueOf(checkControlMessage.getDescription()), channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, SEVERITY,
                    Converter.toTitleCase(checkControlMessage.getSeverity()), channelToBeUpdated);
        }
    }

    private void updateServices(List<RequiredService> requiredServiceList, @Nullable String channelToBeUpdated) {
        // if list is empty add "undefined" element
        if (requiredServiceList.isEmpty()) {
            RequiredService requiredService = new RequiredService();
            requiredService.setType(Constants.NO_ENTRIES);
            requiredService.setDescription(Constants.NO_ENTRIES);
            requiredServiceList.add(requiredService);
        }

        // add all elements to options
        serviceList = requiredServiceList;
        List<CommandOption> serviceNameOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (RequiredService requiredService : requiredServiceList) {
            // create StateOption with "value = list index" and "label = human readable
            // string"
            serviceNameOptions
                    .add(new CommandOption(Integer.toString(index), Converter.toTitleCase(requiredService.getType())));
            if (selectedService.equals(requiredService.getType())) {
                isSelectedElementIn = true;
            }
            index++;
        }

        setOptions(CHANNEL_GROUP_SERVICE, NAME, serviceNameOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectService(0, channelToBeUpdated);
        }
    }

    private void selectService(int index, @Nullable String channelToBeUpdated) {
        if (index >= 0 && index < serviceList.size()) {
            RequiredService serviceEntry = serviceList.get(index);
            selectedService = serviceEntry.getType();
            updateChannel(CHANNEL_GROUP_SERVICE, NAME, Converter.toTitleCase(serviceEntry.getType()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_SERVICE, DETAILS, StringType.valueOf(serviceEntry.getDescription()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_SERVICE, DATE,
                    Converter.zonedToLocalDateTime(serviceEntry.getDateTime(), timeZoneProvider.getTimeZone()),
                    channelToBeUpdated);

            if (serviceEntry.getMileage() > 0) {
                updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE,
                        QuantityType.valueOf(serviceEntry.getMileage(), Constants.KILOMETRE_UNIT), channelToBeUpdated);
            } else {
                updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE, UnDefType.UNDEF, channelToBeUpdated);
            }
        }
    }

    private void updateChargingSessions(ChargingSessionsContainer chargeSessionsContainer,
            @Nullable String channelToBeUpdated) {
        List<ChargingSession> chargeSessions = new ArrayList<>();

        if (chargeSessionsContainer.chargingSessions != null
                && chargeSessionsContainer.chargingSessions.getSessions() != null
                && !chargeSessionsContainer.chargingSessions.getSessions().isEmpty()) {
            chargeSessions.addAll(chargeSessionsContainer.chargingSessions.getSessions());
        } else {
            // if list is empty add "undefined" element
            ChargingSession cs = new ChargingSession();
            cs.setTitle(Constants.NO_ENTRIES);
            chargeSessions.add(cs);
        }

        // add all elements to options
        sessionList = chargeSessions;
        List<CommandOption> sessionNameOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (ChargingSession session : sessionList) {
            // create StateOption with "value = list index" and "label = human readable
            // string"
            sessionNameOptions.add(new CommandOption(Integer.toString(index), session.getTitle()));
            if (selectedSession.equals(session.getTitle())) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_CHARGE_SESSION, TITLE, sessionNameOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectSession(0, channelToBeUpdated);
        }
    }

    private void selectSession(int index, @Nullable String channelToBeUpdated) {
        if (index >= 0 && index < sessionList.size()) {
            ChargingSession sessionEntry = sessionList.get(index);
            selectedSession = sessionEntry.getTitle();
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, TITLE, StringType.valueOf(sessionEntry.getTitle()),
                    channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, SUBTITLE, StringType.valueOf(sessionEntry.getSubtitle()),
                    channelToBeUpdated);
            if (sessionEntry.getEnergyCharged() != null) {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ENERGY, StringType.valueOf(sessionEntry.getEnergyCharged()),
                        channelToBeUpdated);
            } else {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ENERGY, StringType.valueOf(Constants.UNDEF),
                        channelToBeUpdated);
            }
            if (sessionEntry.getIssues() != null) {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ISSUE, StringType.valueOf(sessionEntry.getIssues()),
                        channelToBeUpdated);
            } else {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ISSUE, StringType.valueOf(Constants.HYPHEN),
                        channelToBeUpdated);
            }
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, STATUS, StringType.valueOf(sessionEntry.getSessionStatus()),
                    channelToBeUpdated);
        }
    }

    private void updateChargingProfile(ChargingProfile cp, @Nullable String channelToBeUpdated) {
        ChargingProfileWrapper cpw = new ChargingProfileWrapper(cp);

        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_PREFERENCE, StringType.valueOf(cpw.getPreference()),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_MODE, StringType.valueOf(cpw.getMode()),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_CONTROL, StringType.valueOf(cpw.getControlType()),
                channelToBeUpdated);
        ChargingSettings cs = cpw.getChargingSettings();
        if (cs != null) {
            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_TARGET,
                    QuantityType.valueOf(cs.getTargetSoc(), Units.PERCENT), channelToBeUpdated);

            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_LIMIT,
                    OnOffType.from(cs.isAcCurrentLimitActive()), channelToBeUpdated);
        }
        final Boolean climate = cpw.isEnabled(ProfileKey.CLIMATE);
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_CLIMATE,
                climate == null ? UnDefType.UNDEF : OnOffType.from(climate), channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.WINDOWSTART, channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.WINDOWEND, channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.TIMER1, channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.TIMER2, channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.TIMER3, channelToBeUpdated);
        updateTimedState(cpw, ProfileKey.TIMER4, channelToBeUpdated);
    }

    private void updateTimedState(ChargingProfileWrapper profile, ProfileKey key, @Nullable String channelToBeUpdated) {
        final TimedChannel timed = ChargingProfileUtils.getTimedChannel(key);
        if (timed != null) {
            final LocalTime time = profile.getTime(key);
            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, timed.time,
                    time.equals(Constants.NULL_LOCAL_TIME) ? UnDefType.UNDEF
                            : new DateTimeType(ZonedDateTime.of(Constants.EPOCH_DAY, time, ZoneId.systemDefault())),
                    channelToBeUpdated);
            if (timed.timer != null) {
                final Boolean enabled = profile.isEnabled(key);
                updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, timed.timer + CHARGE_ENABLED,
                        enabled == null ? UnDefType.UNDEF : OnOffType.from(enabled), channelToBeUpdated);
                if (timed.hasDays) {
                    final Set<DayOfWeek> days = profile.getDays(key);
                    EnumSet.allOf(DayOfWeek.class).forEach(day -> {
                        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE,
                                timed.timer + ChargingProfileUtils.getDaysChannel(day),
                                days == null ? UnDefType.UNDEF : OnOffType.from(days.contains(day)),
                                channelToBeUpdated);
                    });
                }
            }
        }
    }

    private void updateDoors(VehicleDoorsState vehicleDoorsState, boolean isLeftSteering,
            @Nullable String channelToBeUpdated) {
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleDoorsState.getLeftFront() : vehicleDoorsState.getRightFront())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleDoorsState.getLeftRear() : vehicleDoorsState.getRightRear())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleDoorsState.getRightFront() : vehicleDoorsState.getLeftFront())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleDoorsState.getRightRear() : vehicleDoorsState.getLeftRear())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, TRUNK,
                StringType.valueOf(Converter.toTitleCase(vehicleDoorsState.getTrunk())), channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, HOOD, StringType.valueOf(Converter.toTitleCase(vehicleDoorsState.getHood())),
                channelToBeUpdated);
    }

    private void updateWindows(VehicleWindowsState vehicleWindowState, boolean isLeftSteering,
            @Nullable String channelToBeUpdated) {
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleWindowState.getLeftFront() : vehicleWindowState.getRightFront())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleWindowState.getLeftRear() : vehicleWindowState.getRightRear())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleWindowState.getRightFront() : vehicleWindowState.getLeftFront())),
                channelToBeUpdated);
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(
                        isLeftSteering ? vehicleWindowState.getRightRear() : vehicleWindowState.getLeftRear())),
                channelToBeUpdated);
    }

    private void updateRoof(VehicleRoofState vehicleRoofState, @Nullable String channelToBeUpdated) {
        updateChannel(CHANNEL_GROUP_DOORS, SUNROOF,
                StringType.valueOf(Converter.toTitleCase(vehicleRoofState.getRoofState())), channelToBeUpdated);
    }

    private void updatePosition(VehicleLocation location, @Nullable String channelToBeUpdated) {
        if (location.getCoordinates().getLatitude() < 0) {
            updateChannel(CHANNEL_GROUP_LOCATION, GPS, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_LOCATION, HEADING, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_LOCATION, ADDRESS, UnDefType.UNDEF, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE, UnDefType.UNDEF, channelToBeUpdated);
        } else {
            PointType vehicleLocation = PointType.valueOf(Double.toString(location.getCoordinates().getLatitude()) + ","
                    + Double.toString(location.getCoordinates().getLongitude()));
            updateChannel(CHANNEL_GROUP_LOCATION, GPS, vehicleLocation, channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_LOCATION, HEADING,
                    QuantityType.valueOf(location.getHeading(), Units.DEGREE_ANGLE), channelToBeUpdated);
            updateChannel(CHANNEL_GROUP_LOCATION, ADDRESS, StringType.valueOf(location.getAddress().getFormatted()),
                    channelToBeUpdated);
            PointType homeLocation = locationProvider.getLocation();
            if (homeLocation != null) {
                updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE,
                        QuantityType.valueOf(vehicleLocation.distanceFrom(homeLocation).intValue(), SIUnits.METRE),
                        channelToBeUpdated);
            } else {
                updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE, UnDefType.UNDEF, channelToBeUpdated);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("VehicleHandler.handleCommand {}, {}, {}", command.toFullString(), channelUID.getAsString(),
                channelUID.getIdWithoutGroup());
        String group = channelUID.getGroupId();

        if (group == null) {
            logger.debug("Cannot handle command {}, no group for channel {}", command.toFullString(),
                    channelUID.getAsString());
            return;
        }

        if (command instanceof RefreshType) {
            // Refresh of Channels with cached values
            if (CHANNEL_GROUP_VEHICLE_IMAGE.equals(group)) {
                imageCache.ifPresent(image -> updateImage(image));
            } else {
                vehicleStatusCache.ifPresent(
                        vehicleStatus -> triggerVehicleStatusUpdate(vehicleStatus, channelUID.getIdWithoutGroup()));
            }
        } else if (command instanceof StringType) {
            // Check for Channel Group and corresponding Actions
            switch (group) {
                case CHANNEL_GROUP_REMOTE:
                    // Executing Remote Services
                    String serviceCommand = ((StringType) command).toFullString();
                    remote.ifPresent(remot -> {
                        RemoteServiceUtils.getRemoteServiceFromCommand(serviceCommand)
                                .ifPresentOrElse(service -> remot.execute(service), () -> {
                                    logger.debug("Remote service execution {} unknown", serviceCommand);
                                });
                    });
                    break;
                case CHANNEL_GROUP_VEHICLE_IMAGE:
                    // Image Change
                    vehicleConfiguration.ifPresent(config -> {
                        if (channelUID.getIdWithoutGroup().equals(IMAGE_VIEWPORT)) {
                            String newViewport = command.toString();
                            synchronized (imageProperties) {
                                if (!imageProperties.viewport.equals(newViewport)) {
                                    imageProperties = new ImageProperties(newViewport);
                                    imageCache = Optional.empty();
                                    Optional<byte[]> imageContent = proxy.map(prox -> {
                                        try {
                                            return prox.requestImage(config.getVin(), config.getVehicleBrand(),
                                                    imageProperties);
                                        } catch (NetworkException e) {
                                            logger.debug("{}", e.toString());
                                            return "".getBytes();
                                        }
                                    });
                                    imageContent.ifPresent(imageContentData -> updateImage(imageContentData));
                                }
                            }
                            updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT, StringType.valueOf(newViewport),
                                    IMAGE_VIEWPORT);
                        }
                    });
                    break;
                case CHANNEL_GROUP_SERVICE:
                    int serviceIndex = Converter.parseIntegerString(command.toFullString());
                    if (serviceIndex != -1) {
                        selectService(serviceIndex, null);
                    } else {
                        logger.debug("Cannot select Service index {}", command.toFullString());
                    }
                    break;
                case CHANNEL_GROUP_CHECK_CONTROL:
                    int checkControlIndex = Converter.parseIntegerString(command.toFullString());
                    if (checkControlIndex != -1) {
                        selectCheckControl(checkControlIndex, null);
                    } else {
                        logger.debug("Cannot select CheckControl index {}", command.toFullString());
                    }
                    break;
                case CHANNEL_GROUP_CHARGE_SESSION:
                    int sessionIndex = Converter.parseIntegerString(command.toFullString());
                    if (sessionIndex != -1) {
                        selectSession(sessionIndex, null);
                    } else {
                        logger.debug("Cannot select Session index {}", command.toFullString());
                    }
                    break;
                default:
                    logger.debug("Cannot handle command {}, channel {} in group {} not a command channel",
                            command.toFullString(), channelUID.getAsString(), group);
            }
        } else if (command instanceof OnOffType) {
            if (CHANNEL_GROUP_UPDATE.equals(group) && OnOffType.ON.equals(command)) {
                // triggering the update of the respective channel
                switch (channelUID.getIdWithoutGroup()) {
                    case STATE_UPDATE:
                        updateVehicleStatus();
                        break;
                    case CHARGING_UPDATE:
                        updateCharging();
                        break;
                    case IMAGE_UPDATE:
                        updateImage();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void updateImage(byte[] imageContent) {
        if (imageContent.length > 0) {
            imageCache = Optional.of(imageContent);
            String contentType = HttpUtil.guessContentTypeFromData(imageContent);
            updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_FORMAT, new RawType(imageContent, contentType),
                    IMAGE_FORMAT);
        } else {
            synchronized (imageProperties) {
                imageProperties.failed();
            }
        }
    }
}
