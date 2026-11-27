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
package org.openhab.binding.pleos.internal.handler;

import static org.openhab.binding.pleos.internal.PleosBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pleos.internal.api.PleosApiClient;
import org.openhab.binding.pleos.internal.api.PleosApiException;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.BatteryData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.ChargeInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.TargetSoC;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.DrivingData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.IgnitionDetail;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.IgnitionInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.OdometerInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.TransmissionInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse.FreezerData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse.FreezerInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LeftRightRow;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse.GpsLocation;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse.LocationData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse.DistanceInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse.EngineInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse.PowertrainData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.ClimateControl;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.SeatInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.TireRow;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.ValueWithUnit;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.VehicleStatusData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.WindshieldInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehicleInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehiclesData;
import org.openhab.binding.pleos.internal.config.PleosVehicleConfiguration;
import org.openhab.binding.pleos.internal.model.VehicleType;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link PleosVehicleHandler} cyclically retrieves vehicle data via the bridge's
 * {@link PleosApiClient} and updates the openHAB channels.
 *
 * @author Stephan Sandmann - Initial contribution
 * @author Marcus Better - Idea for createDynamicChannels and channelBuilder
 */
@NonNullByDefault
public class PleosVehicleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PleosVehicleHandler.class);

    private String vin = "";
    private volatile VehicleType vehicleType = VehicleType.UNKNOWN;

    public PleosVehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Pleos Vehicle Handler for Thing: {}", getThing().getUID());

        PleosVehicleConfiguration config = getConfigAs(PleosVehicleConfiguration.class);
        String configuredVin = config.vin;
        if (configuredVin == null || configuredVin.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/vehicle-handler.initialize.missing-vin");
            return;
        }
        this.vin = configuredVin.trim();
        this.vehicleType = VehicleType.fromApiValue(getThing().getProperties().get(PROPERTY_VEHICLE_TYPE));
        if (this.vehicleType != VehicleType.UNKNOWN) {
            createDynamicChannels();
        }

        PleosAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::refreshVehicleData);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::refreshVehicleData);
        }
    }

    /**
     * Retrieves all relevant endpoints for this vehicle.
     */
    public void refreshVehicleData() {
        PleosAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        PleosApiClient apiClient = accountHandler.getApiClient();
        if (apiClient == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        try {
            // If the car was added manually without using discovery, we determine the VehicleType on a one-time basis.
            if (vehicleType == VehicleType.UNKNOWN) {
                resolveVehicleMetadata(apiClient);
            }

            if (vehicleType.supportsCharging()) {
                updateBatteryStatus(apiClient.getBatteryStatus(vin));
            }

            updatePowertrainStatus(apiClient.getPowertrainStatus(vin));
            updateLocationStatus(apiClient.getLocationInfo(vin));
            updateDrivingStatus(apiClient.getOperationStatus(vin));
            updateVehicleStatus(apiClient.getVehicleStatus(vin));

            // Freezer is an optional feature; 4045 (no data) is expected if not equipped
            try {
                updateFreezerStatus(apiClient.getFreezerStatus(vin));
            } catch (PleosApiException e) {
                if (!e.isNoDataAvailable()) {
                    logger.debug("Freezer endpoint for {} not available: {}", vin, e.getMessage());
                }
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (PleosApiException e) {
            handleApiException(accountHandler, e);
        }
    }

    private void handleApiException(PleosAccountHandler accountHandler, PleosApiException e) {
        if (e.isAuthError()) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            String msg = e.getMessage();
            accountHandler.handleFatalAuthError(msg != null ? msg : "Authentication failed");
        } else if (e.isUnregisteredVehicle()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } else if (e.isNoDataAvailable()) {
            // Car is registered but has not yet transmitted any telematics data -> Thing remains ONLINE
            logger.debug("No telematics data available yet for VIN {} (code 4045).", vin);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else if (e.isInvalidParameter()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/vehicle-handler.status.invalid-vin");
        } else {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void resolveVehicleMetadata(PleosApiClient apiClient) {
        try {
            VehiclesResponse resp = apiClient.getVehicles();
            VehiclesData data = resp.data();
            List<VehicleInfo> vehicles = data != null ? data.vehicles() : null;
            if (vehicles == null) {
                return;
            }
            for (VehicleInfo info : vehicles) {
                if (vin.equalsIgnoreCase(info.vin())) {
                    VehicleType detected = VehicleType.fromApiValue(info.type());
                    this.vehicleType = detected;

                    Map<String, String> props = new HashMap<>(getThing().getProperties());
                    props.put(PROPERTY_VEHICLE_TYPE, detected.name());
                    String sellname = info.sellname();
                    if (sellname != null && !sellname.isBlank()) {
                        props.put(PROPERTY_MODEL_NAME, sellname.trim());
                    }
                    String modelCode = info.name();
                    if (modelCode != null && !modelCode.isBlank()) {
                        props.put(PROPERTY_MODEL_CODE, modelCode.trim());
                    }
                    updateProperties(props);
                    createDynamicChannels();
                    break;
                }
            }
        } catch (PleosApiException e) {
            logger.debug("Could not reload vehicle metadata for {}: {}", vin, e.getMessage());
        }
    }

    private void createDynamicChannels() {
        if (vehicleType == VehicleType.UNKNOWN) {
            return;
        }

        final Thing thing = getThing();
        final ThingUID thingUID = thing.getUID();
        final Set<ChannelUID> existingChannels = thing.getChannels().stream().map(Channel::getUID)
                .collect(Collectors.toUnmodifiableSet());

        final List<ChannelBuilder> channelsToAdd = new ArrayList<>();
        final List<ChannelUID> channelsToRemove = new ArrayList<>();

        // Clean up obsolete channels from the "vehicle" group if they were defined in previous versions
        final List<String> obsoleteVehicleChannelIds = List.of(CHANNEL_SOC, CHANNEL_CAPACITY, CHANNEL_CHARGING,
                CHANNEL_PLUGIN, CHANNEL_CHARGE_STATUS, CHANNEL_REMAIN_TIME, CHANNEL_TARGET_SOC_STANDARD,
                CHANNEL_TARGET_SOC_QUICK, CHANNEL_FUEL_LEVEL);
        for (String chId : obsoleteVehicleChannelIds) {
            ChannelUID oldUID = new ChannelUID(thingUID, GROUP_VEHICLE, chId);
            if (existingChannels.contains(oldUID)) {
                channelsToRemove.add(oldUID);
            }
        }

        if (vehicleType.supportsCharging()) {
            final List<ChannelBuilder> chargingChannels = List.of(
                    buildChannel(GROUP_CHARGING, CHANNEL_SOC, CoreItemFactory.NUMBER + ":Dimensionless",
                            CHANNEL_TYPE_SOC),
                    buildChannel(GROUP_CHARGING, CHANNEL_CAPACITY, CoreItemFactory.NUMBER + ":Energy",
                            CHANNEL_TYPE_BATTERY_CAPACITY),
                    buildChannel(GROUP_CHARGING, CHANNEL_CHARGING, CoreItemFactory.SWITCH,
                            CHANNEL_TYPE_CHARGING_BOOLEAN),
                    buildChannel(GROUP_CHARGING, CHANNEL_PLUGIN, CoreItemFactory.STRING,
                            CHANNEL_TYPE_CHARGE_PLUGIN_ENUM),
                    buildChannel(GROUP_CHARGING, CHANNEL_CHARGE_STATUS, CoreItemFactory.STRING,
                            CHANNEL_TYPE_CHARGE_STATUS_ENUM),
                    buildChannel(GROUP_CHARGING, CHANNEL_REMAIN_TIME, CoreItemFactory.NUMBER + ":Time",
                            CHANNEL_TYPE_REMAIN_TIME),
                    buildChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_STANDARD, CoreItemFactory.NUMBER + ":Dimensionless",
                            CHANNEL_TYPE_TARGET_SOC_STANDARD),
                    buildChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_QUICK, CoreItemFactory.NUMBER + ":Dimensionless",
                            CHANNEL_TYPE_TARGET_SOC_QUICK));

            for (ChannelBuilder cb : chargingChannels) {
                Channel ch = cb.build();
                if (!existingChannels.contains(ch.getUID())) {
                    channelsToAdd.add(cb);
                }
            }
        } else {
            thing.getChannelsOfGroup(GROUP_CHARGING).stream().map(Channel::getUID).forEach(channelsToRemove::add);
        }

        if (vehicleType.supportsFuel()) {
            ChannelBuilder fuelChannel = buildChannel(GROUP_FUEL, CHANNEL_FUEL_LEVEL,
                    CoreItemFactory.NUMBER + ":Dimensionless", CHANNEL_TYPE_FUEL_LEVEL);
            Channel ch = fuelChannel.build();
            if (!existingChannels.contains(ch.getUID())) {
                channelsToAdd.add(fuelChannel);
            }
        } else {
            thing.getChannelsOfGroup(GROUP_FUEL).stream().map(Channel::getUID).forEach(channelsToRemove::add);
        }

        if (!channelsToAdd.isEmpty() || !channelsToRemove.isEmpty()) {
            final ThingBuilder thingBuilder = editThing();
            channelsToRemove.forEach(thingBuilder::withoutChannel);
            channelsToAdd.stream().map(ChannelBuilder::build).forEach(thingBuilder::withChannel);
            updateThing(thingBuilder.build());
        }
    }

    private ChannelBuilder buildChannel(String group, String channelId, String itemType, String channelTypeId) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), group, channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelTypeId);
        return ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID);
    }

    private void updateBatteryStatus(BatteryResponse response) {
        BatteryData data = response.data();
        ChargeInfo charge = data != null ? data.charge() : null;
        if (charge == null) {
            setAllBatteryChannelsToUndef();
            return;
        }

        updateChannel(GROUP_CHARGING, CHANNEL_SOC, parseQuantityOrUndef(charge.stateOfCharge(), Units.PERCENT));

        Boolean charging = charge.charging();
        updateChannel(GROUP_CHARGING, CHANNEL_CHARGING, charging != null ? OnOffType.from(charging) : UnDefType.UNDEF);

        String pluginRaw = charge.plugin();
        if (pluginRaw != null) {
            String mappedPlugin = switch (pluginRaw.toLowerCase(Locale.ROOT)) {
                case "connected" -> "PLUGGED";
                case "invalid" -> "INVALID";
                default -> "UNPLUGGED";
            };
            updateChannel(GROUP_CHARGING, CHANNEL_PLUGIN, new StringType(mappedPlugin));
        } else {
            updateChannel(GROUP_CHARGING, CHANNEL_PLUGIN, UnDefType.UNDEF);
        }

        String statusRaw = charge.status();
        if (statusRaw != null) {
            String mappedStatus = switch (statusRaw.toLowerCase(Locale.ROOT)) {
                case "charging" -> "CHARGING_AC";
                case "fastcharging" -> "CHARGING_DC";
                case "finished" -> "FINISHED";
                case "reservedcharging" -> "WAITING_SCHEDULE";
                default -> "NOT_CHARGING";
            };
            updateChannel(GROUP_CHARGING, CHANNEL_CHARGE_STATUS, new StringType(mappedStatus));
        } else {
            updateChannel(GROUP_CHARGING, CHANNEL_CHARGE_STATUS, UnDefType.UNDEF);
        }

        updateChannel(GROUP_CHARGING, CHANNEL_REMAIN_TIME, parseQuantityOrUndef(charge.remainTime(), Units.MINUTE));
        updateChannel(GROUP_CHARGING, CHANNEL_CAPACITY,
                parseQuantityOrUndef(charge.batteryCapacity(), Units.WATT_HOUR));

        TargetSoC target = charge.targetStateOfCharge();
        updateChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_STANDARD,
                target != null ? parseQuantityOrUndef(target.standard(), Units.PERCENT) : UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_QUICK,
                target != null ? parseQuantityOrUndef(target.quick(), Units.PERCENT) : UnDefType.UNDEF);
    }

    private void updatePowertrainStatus(PowertrainResponse response) {
        PowertrainData data = response.data();
        if (data == null) {
            return;
        }

        EngineInfo engine = data.engine();
        if (vehicleType.supportsFuel() && engine != null) {
            updateChannel(GROUP_FUEL, CHANNEL_FUEL_LEVEL, parseQuantityOrUndef(engine.fuelLevel(), Units.PERCENT));
        }

        List<DistanceInfo> distances = data.distanceToEmpties();
        if (distances != null && !distances.isEmpty()) {
            DistanceInfo primary = distances.get(0);
            Unit<?> distUnit = "mile".equalsIgnoreCase(primary.unit()) ? ImperialUnits.MILE
                    : MetricPrefix.KILO(SIUnits.METRE);
            updateChannel(GROUP_VEHICLE, CHANNEL_DISTANCE_TO_EMPTY, parseQuantityOrUndef(primary.value(), distUnit));

            String rawType = primary.type();
            if (rawType != null && !rawType.isBlank()) {
                String upper = rawType.toUpperCase(Locale.ROOT);
                // Normalize API value "ICE" to the channel option "GASOLINE"
                updateChannel(GROUP_VEHICLE, CHANNEL_DISTANCE_TYPE,
                        new StringType("ICE".equals(upper) ? "GASOLINE" : upper));
            } else {
                updateChannel(GROUP_VEHICLE, CHANNEL_DISTANCE_TYPE, UnDefType.UNDEF);
            }
        }
    }

    private void updateLocationStatus(LocationResponse response) {
        LocationData data = response.data();
        if (data == null) {
            setAllLocationChannelsToUndef();
            return;
        }

        updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION_LAST_UPDATE, parseDateTimeOrUndef(data.timestamp()));

        GpsLocation loc = data.location();
        if (loc == null) {
            setAllLocationChannelsToUndef();
            return;
        }

        Double lat = parseDouble(loc.latitude());
        Double lon = parseDouble(loc.longitude());
        Double alt = parseDouble(loc.altitude());

        if (lat != null && lon != null) {
            updateChannel(GROUP_VEHICLE, CHANNEL_LATITUDE, new DecimalType(lat));
            updateChannel(GROUP_VEHICLE, CHANNEL_LONGITUDE, new DecimalType(lon));
            if (alt != null) {
                updateChannel(GROUP_VEHICLE, CHANNEL_ALTITUDE, new QuantityType<>(alt, SIUnits.METRE));
                updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION,
                        new PointType(new DecimalType(lat), new DecimalType(lon), new DecimalType(alt)));
            } else {
                updateChannel(GROUP_VEHICLE, CHANNEL_ALTITUDE, UnDefType.UNDEF);
                updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION,
                        new PointType(new DecimalType(lat), new DecimalType(lon)));
            }
        } else {
            updateChannel(GROUP_VEHICLE, CHANNEL_LATITUDE, UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, CHANNEL_LONGITUDE, UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, CHANNEL_ALTITUDE, UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION, UnDefType.UNDEF);
        }

        updateChannel(GROUP_VEHICLE, CHANNEL_SPEED,
                parseQuantityOrUndef(loc.extractRawSpeed(), SIUnits.KILOMETRE_PER_HOUR));

        Double heading = parseDouble(loc.heading());
        updateChannel(GROUP_VEHICLE, CHANNEL_HEADING,
                heading != null ? new QuantityType<>(heading.intValue(), Units.DEGREE_ANGLE) : UnDefType.UNDEF);
    }

    private void updateDrivingStatus(DrivingResponse response) {
        DrivingData data = response.data();
        if (data == null) {
            return;
        }

        IgnitionInfo ignition = data.ignition();
        if (ignition != null) {
            String ignStatus = ignition.status();
            updateChannel(GROUP_VEHICLE, CHANNEL_IGNITION,
                    ignStatus != null ? new StringType(ignStatus.toUpperCase(Locale.ROOT)) : UnDefType.UNDEF);

            List<IgnitionDetail> details = ignition.ignitionDetails();
            if (details != null) {
                for (IgnitionDetail detail : details) {
                    String type = detail.type();
                    if (type == null) {
                        continue;
                    }
                    OnOffType state = OnOffType.from("on".equalsIgnoreCase(detail.status()));
                    switch (type.toLowerCase(Locale.ROOT)) {
                        case "acc" -> updateChannel(GROUP_VEHICLE, CHANNEL_IGNITION_ACC, state);
                        case "ign1" -> updateChannel(GROUP_VEHICLE, CHANNEL_IGNITION_IGN1, state);
                        case "ign3" -> updateChannel(GROUP_VEHICLE, CHANNEL_IGNITION_IGN3, state);
                        default -> {
                        }
                    }
                }
            }
        }

        String sleepMode = data.sleepMode();
        updateChannel(GROUP_VEHICLE, CHANNEL_SLEEP_MODE,
                sleepMode != null ? new StringType(sleepMode.toUpperCase(Locale.ROOT)) : UnDefType.UNDEF);

        OdometerInfo odo = data.odometer();
        if (odo != null) {
            Double val = odo.value();
            if (val != null) {
                Unit<?> odoUnit = "mile".equalsIgnoreCase(odo.unit()) ? ImperialUnits.MILE
                        : MetricPrefix.KILO(SIUnits.METRE);
                updateChannel(GROUP_VEHICLE, CHANNEL_ODOMETER, new QuantityType<>(val, odoUnit));
            }
        }

        TransmissionInfo trans = data.transmission();
        Boolean parking = trans != null ? trans.parkingPosition() : null;
        if (parking != null) {
            updateChannel(GROUP_VEHICLE, CHANNEL_PARKING_POSITION, OnOffType.from(parking));
        }

        updateChannel(GROUP_VEHICLE, CHANNEL_LAST_UPDATE, parseDateTimeOrUndef(data.timestamp()));
    }

    private void updateVehicleStatus(VehicleStatusResponse response) {
        VehicleStatusData data = response.data();
        if (data == null) {
            return;
        }

        List<LeftRightRow<String>> doors = data.doors();
        LeftRightRow<String> frontDoors = (doors != null && !doors.isEmpty()) ? doors.get(0) : null;
        LeftRightRow<String> rearDoors = (doors != null && doors.size() > 1) ? doors.get(1) : null;

        updateChannel(GROUP_DOORS, CHANNEL_DOOR_FRONT_LEFT,
                toUpperStringOrUndef(frontDoors != null ? frontDoors.left() : null));
        updateChannel(GROUP_DOORS, CHANNEL_DOOR_FRONT_RIGHT,
                toUpperStringOrUndef(frontDoors != null ? frontDoors.right() : null));
        updateChannel(GROUP_DOORS, CHANNEL_DOOR_REAR_LEFT,
                toUpperStringOrUndef(rearDoors != null ? rearDoors.left() : null));
        updateChannel(GROUP_DOORS, CHANNEL_DOOR_REAR_RIGHT,
                toUpperStringOrUndef(rearDoors != null ? rearDoors.right() : null));
        updateChannel(GROUP_DOORS, CHANNEL_HOOD, toUpperStringOrUndef(data.hood()));
        updateChannel(GROUP_DOORS, CHANNEL_TRUNK, toUpperStringOrUndef(data.trunk()));
        updateChannel(GROUP_DOORS, CHANNEL_SUNROOF, toUpperStringOrUndef(data.sunroof()));

        List<TireRow> tires = data.tire();
        updateTireRow(tires != null && !tires.isEmpty() ? tires.get(0) : null, CHANNEL_TIRE_PRESSURE_FRONT_LEFT,
                CHANNEL_TIRE_PRESSURE_FRONT_RIGHT);
        updateTireRow(tires != null && tires.size() > 1 ? tires.get(1) : null, CHANNEL_TIRE_PRESSURE_REAR_LEFT,
                CHANNEL_TIRE_PRESSURE_REAR_RIGHT);

        List<String> rawWarnings = data.warnings();
        List<String> warnings = rawWarnings != null ? rawWarnings : List.of();
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LOW_FUEL, OnOffType.from(warnings.contains("lowFuel")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_SMARTKEY_BATTERY,
                OnOffType.from(warnings.contains("smartKeyBattery")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_WASH_FLUID, OnOffType.from(warnings.contains("washFluid")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_BRAKE_OIL, OnOffType.from(warnings.contains("brakeOil")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_ENGINE_OIL, OnOffType.from(warnings.contains("engineOil")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_HEADLAMP,
                OnOffType.from(warnings.contains("frontHeadLamp")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_TURN_FRONT_LEFT,
                OnOffType.from(warnings.contains("frontLeftTurnSignal")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_TURN_FRONT_RIGHT,
                OnOffType.from(warnings.contains("frontRightTurnSignal")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_STOP_REAR_LEFT,
                OnOffType.from(warnings.contains("rearLeftStopLamp")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_STOP_REAR_RIGHT,
                OnOffType.from(warnings.contains("rearRightStopLamp")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_TURN_REAR_LEFT,
                OnOffType.from(warnings.contains("rearLeftTurnSignal")));
        updateChannel(GROUP_WARNINGS, CHANNEL_WARNING_LAMP_TURN_REAR_RIGHT,
                OnOffType.from(warnings.contains("rearRightTurnSignal")));

        updateChannel(GROUP_TIRES, CHANNEL_TIRE_WARNING_FL, OnOffType.from(warnings.contains("tirePressureFrontLeft")));
        updateChannel(GROUP_TIRES, CHANNEL_TIRE_WARNING_FR,
                OnOffType.from(warnings.contains("tirePressureFrontRight")));
        updateChannel(GROUP_TIRES, CHANNEL_TIRE_WARNING_RL, OnOffType.from(warnings.contains("tirePressureRearLeft")));
        updateChannel(GROUP_TIRES, CHANNEL_TIRE_WARNING_RR, OnOffType.from(warnings.contains("tirePressureRearRight")));
        updateChannel(GROUP_TIRES, CHANNEL_TIRE_WARNING_LOW_GENERIC,
                OnOffType.from(warnings.contains("tirePressureLow")));

        ClimateControl climate = data.climateControl();
        if (climate != null) {
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_MODE, toUpperStringOrUndef(climate.mode()));
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_STATUS,
                    climate.status() != null ? OnOffType.from("on".equalsIgnoreCase(climate.status()))
                            : UnDefType.UNDEF);
            Integer speed = climate.speed();
            if (speed != null) {
                updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_SPEED,
                        climate.speed() != null ? new DecimalType(speed) : UnDefType.UNDEF);
            }
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_TARGET_TEMP, toTemperatureOrUndef(climate.temperature()));
        } else {
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_MODE, UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_STATUS, UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_SPEED, UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, CHANNEL_CLIMATE_TARGET_TEMP, UnDefType.UNDEF);
        }

        WindshieldInfo windshield = data.windshield();
        updateChannel(GROUP_CLIMATE, CHANNEL_WINDSHIELD_HEATER,
                (windshield != null && windshield.defrost() != null)
                        ? OnOffType.from("on".equalsIgnoreCase(windshield.defrost()))
                        : UnDefType.UNDEF);

        updateChannel(GROUP_CLIMATE, CHANNEL_STEERING_WHEEL_HEATER,
                data.steeringWheelHeat() != null ? OnOffType.from("on".equalsIgnoreCase(data.steeringWheelHeat()))
                        : UnDefType.UNDEF);

        updateChannel(GROUP_CLIMATE, CHANNEL_OUTSIDE_TEMPERATURE, toTemperatureOrUndef(data.outsideTemperature()));

        List<LeftRightRow<SeatInfo>> seats = data.seats();
        LeftRightRow<SeatInfo> frontSeats = (seats != null && !seats.isEmpty()) ? seats.get(0) : null;
        LeftRightRow<SeatInfo> rearSeats = (seats != null && seats.size() > 1) ? seats.get(1) : null;

        updateSeatSide(frontSeats != null ? frontSeats.left() : null, CHANNEL_SEAT_FRONT_LEFT_HEAT,
                CHANNEL_SEAT_FRONT_LEFT_VENT);
        updateSeatSide(frontSeats != null ? frontSeats.right() : null, CHANNEL_SEAT_FRONT_RIGHT_HEAT,
                CHANNEL_SEAT_FRONT_RIGHT_VENT);
        updateSeatSide(rearSeats != null ? rearSeats.left() : null, CHANNEL_SEAT_REAR_LEFT_HEAT,
                CHANNEL_SEAT_REAR_LEFT_VENT);
        updateSeatSide(rearSeats != null ? rearSeats.right() : null, CHANNEL_SEAT_REAR_RIGHT_HEAT,
                CHANNEL_SEAT_REAR_RIGHT_VENT);
    }

    private void updateFreezerStatus(FreezerResponse response) {
        FreezerData data = response.data();
        FreezerInfo freezer = data != null ? data.freezer() : null;
        if (freezer == null) {
            return;
        }
        String status = freezer.status();
        updateChannel(GROUP_FREEZER, CHANNEL_FREEZER_ACTIVE,
                status != null ? OnOffType.from("on".equalsIgnoreCase(status)) : UnDefType.UNDEF);
        Double temp = freezer.temperature();
        updateChannel(GROUP_FREEZER, CHANNEL_FREEZER_TEMPERATURE,
                temp != null ? new QuantityType<>(temp, SIUnits.CELSIUS) : UnDefType.UNDEF);
    }

    private void updateChannel(String group, String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channelId), state);
    }

    private void updateTireRow(@Nullable TireRow row, String leftChannel, String rightChannel) {
        if (row == null) {
            updateChannel(GROUP_TIRES, leftChannel, UnDefType.UNDEF);
            updateChannel(GROUP_TIRES, rightChannel, UnDefType.UNDEF);
            return;
        }
        String unit = row.unit();
        if (unit == null) {
            updateChannel(GROUP_TIRES, leftChannel, UnDefType.UNDEF);
            updateChannel(GROUP_TIRES, rightChannel, UnDefType.UNDEF);
            return;
        }
        String apiUnit = unit.toLowerCase(Locale.ROOT);
        Unit<?> uomUnit = switch (apiUnit) {
            case "kpa" -> MetricPrefix.KILO(SIUnits.PASCAL);
            case "psi" -> ImperialUnits.POUND_FORCE_SQUARE_INCH;
            default -> Units.BAR;
        };
        updateChannel(GROUP_TIRES, leftChannel, formatTireValue(row.left(), apiUnit, uomUnit));
        updateChannel(GROUP_TIRES, rightChannel, formatTireValue(row.right(), apiUnit, uomUnit));
    }

    static State formatTireValue(@Nullable Double raw, String apiUnit, Unit<?> uomUnit) {
        // 255 is the API sentinel value for an unavailable tire pressure sensor
        if (raw == null || raw == 255.0) {
            return UnDefType.UNDEF;
        }
        // Pleos API reports kPa in steps of 5 and bar in tenths
        double scaled = switch (apiUnit) {
            case "kpa" -> raw * 5.0;
            case "bar" -> raw / 10.0;
            default -> raw;
        };
        return new QuantityType<>(scaled, uomUnit);
    }

    private void updateSeatSide(@Nullable SeatInfo seat, String heatChannel, String ventChannel) {
        if (seat == null) {
            updateChannel(GROUP_SEATS, heatChannel, UnDefType.UNDEF);
            updateChannel(GROUP_SEATS, ventChannel, UnDefType.UNDEF);
            return;
        }
        updateChannel(GROUP_SEATS, heatChannel, new StringType(resolveSeatLevel(seat.heat())));
        updateChannel(GROUP_SEATS, ventChannel, new StringType(resolveSeatLevel(seat.ventilation())));
    }

    // Pleos API may return seat levels either as numeric codes (0-3) or as strings
    static String resolveSeatLevel(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return "OFF";
        }
        var primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return switch (primitive.getAsInt()) {
                case 1 -> "LOW";
                case 2 -> "MIDDLE";
                case 3 -> "HIGH";
                default -> "OFF";
            };
        }
        if (primitive.isString()) {
            return switch (primitive.getAsString().toUpperCase(Locale.ROOT).trim()) {
                case "LOW", "1" -> "LOW";
                case "MIDDLE", "MID", "MEDIUM", "2" -> "MIDDLE";
                case "HIGH", "3" -> "HIGH";
                default -> "OFF";
            };
        }
        return "OFF";
    }

    static State toTemperatureOrUndef(@Nullable ValueWithUnit temp) {
        if (temp == null) {
            return UnDefType.UNDEF;
        }
        Double val = temp.value();
        if (val == null) {
            return UnDefType.UNDEF;
        }
        Unit<?> unit = "F".equalsIgnoreCase(temp.unit()) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
        return new QuantityType<>(val, unit);
    }

    private State toUpperStringOrUndef(@Nullable String raw) {
        return (raw != null && !raw.isBlank()) ? new StringType(raw.toUpperCase(Locale.ROOT)) : UnDefType.UNDEF;
    }

    private State parseQuantityOrUndef(@Nullable String raw, Unit<?> unit) {
        Double val = parseDouble(raw);
        return val != null ? new QuantityType<>(val, unit) : UnDefType.UNDEF;
    }

    private @Nullable Double parseDouble(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        // Pleos API uses "-" to indicate an unavailable numeric value
        if (trimmed.isEmpty() || "-".equals(trimmed)) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private State parseDateTimeOrUndef(@Nullable String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) {
            return UnDefType.UNDEF;
        }
        try {
            return new DateTimeType(rawTimestamp);
        } catch (IllegalArgumentException e) {
            return UnDefType.UNDEF;
        }
    }

    private void setAllBatteryChannelsToUndef() {
        updateChannel(GROUP_CHARGING, CHANNEL_SOC, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_CHARGING, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_PLUGIN, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_CHARGE_STATUS, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_REMAIN_TIME, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_STANDARD, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_TARGET_SOC_QUICK, UnDefType.UNDEF);
        updateChannel(GROUP_CHARGING, CHANNEL_CAPACITY, UnDefType.UNDEF);
    }

    private void setAllLocationChannelsToUndef() {
        updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION_LAST_UPDATE, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_LATITUDE, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_LONGITUDE, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_ALTITUDE, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_LOCATION, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_SPEED, UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, CHANNEL_HEADING, UnDefType.UNDEF);
    }

    private @Nullable PleosAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof PleosAccountHandler handler) {
            return handler;
        }
        return null;
    }
}
