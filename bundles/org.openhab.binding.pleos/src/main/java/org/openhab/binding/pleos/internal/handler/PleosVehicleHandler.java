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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.google.gson.JsonElement;

/**
 * The {@link PleosVehicleHandler} cyclically retrieves vehicle data via the bridge's
 * {@link PleosApiClient} and updates the openHAB channels.
 *
 * @author Stephan Sandmann - Initial contribution
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
        this.vehicleType = VehicleType.fromApiValue(getThing().getProperties().get("vehicleType"));

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

            // 1. Battery and charging (only applicable if the car is not a pure internal combustion engine vehicle)
            if (vehicleType.supportsCharging()) {
                updateBatteryStatus(apiClient.getBatteryStatus(vin));
            } else {
                setAllBatteryChannelsToUndef();
            }

            // 2. Powertrain & Range
            updatePowertrainStatus(apiClient.getPowertrainStatus(vin));

            // 3. Position and Speed
            updateLocationStatus(apiClient.getLocationInfo(vin));

            // 4. Vehicle Operation, Ignition & Mileage
            updateDrivingStatus(apiClient.getOperationStatus(vin));

            // 5. Doors, Tires, Warnings, Climate & Seats
            updateVehicleStatus(apiClient.getVehicleStatus(vin));

            // 6. Cold storage (Freezer – optional feature; 4045/NoData is cleanly ignored)
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
                    props.put("vehicleType", detected.name());
                    String sellname = info.sellname();
                    if (sellname != null && !sellname.isBlank()) {
                        props.put("modelName", sellname.trim());
                    }
                    String modelCode = info.name();
                    if (modelCode != null && !modelCode.isBlank()) {
                        props.put("modelCode", modelCode.trim());
                    }
                    updateProperties(props);
                    break;
                }
            }
        } catch (PleosApiException e) {
            logger.debug("Could not reload vehicle metadata for {}: {}", vin, e.getMessage());
        }
    }
    // =========================================================================
    // MAPPING METHODS (Records -> openHAB Channels)
    // =========================================================================

    private void updateBatteryStatus(BatteryResponse response) {
        BatteryData data = response.data();
        ChargeInfo charge = data != null ? data.charge() : null;
        if (charge == null) {
            setAllBatteryChannelsToUndef();
            return;
        }

        updateChannel(GROUP_VEHICLE, "soc", parseQuantityOrUndef(charge.stateOfCharge(), Units.PERCENT));

        Boolean charging = charge.charging();
        updateChannel(GROUP_VEHICLE, "charging", charging != null ? OnOffType.from(charging) : UnDefType.UNDEF);

        String pluginRaw = charge.plugin();
        if (pluginRaw != null) {
            String mappedPlugin = switch (pluginRaw.toLowerCase(Locale.ROOT)) {
                case "connected" -> "PLUGGED";
                case "invalid" -> "INVALID";
                default -> "UNPLUGGED";
            };
            updateChannel(GROUP_VEHICLE, "plugin", new StringType(mappedPlugin));
        } else {
            updateChannel(GROUP_VEHICLE, "plugin", UnDefType.UNDEF);
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
            updateChannel(GROUP_VEHICLE, "charge-status", new StringType(mappedStatus));
        } else {
            updateChannel(GROUP_VEHICLE, "charge-status", UnDefType.UNDEF);
        }

        updateChannel(GROUP_VEHICLE, "remain-time", parseQuantityOrUndef(charge.remainTime(), Units.MINUTE));
        updateChannel(GROUP_VEHICLE, "capacity", parseQuantityOrUndef(charge.batteryCapacity(), Units.WATT_HOUR));

        TargetSoC target = charge.targetStateOfCharge();
        updateChannel(GROUP_VEHICLE, "target-soc-standard",
                target != null ? parseQuantityOrUndef(target.standard(), Units.PERCENT) : UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "target-soc-quick",
                target != null ? parseQuantityOrUndef(target.quick(), Units.PERCENT) : UnDefType.UNDEF);
    }

    private void updatePowertrainStatus(PowertrainResponse response) {
        PowertrainData data = response.data();
        if (data == null) {
            return;
        }

        EngineInfo engine = data.engine();
        if (vehicleType.supportsFuel() && engine != null) {
            updateChannel(GROUP_VEHICLE, "fuel-level", parseQuantityOrUndef(engine.fuelLevel(), Units.PERCENT));
        } else {
            updateChannel(GROUP_VEHICLE, "fuel-level", UnDefType.UNDEF);
        }

        List<DistanceInfo> distances = data.distanceToEmpties();
        if (distances != null && !distances.isEmpty()) {
            DistanceInfo primary = distances.get(0);
            Unit<?> distUnit = "mile".equalsIgnoreCase(primary.unit()) ? ImperialUnits.MILE
                    : MetricPrefix.KILO(SIUnits.METRE);
            updateChannel(GROUP_VEHICLE, "distance-to-empty", parseQuantityOrUndef(primary.value(), distUnit));

            String rawType = primary.type();
            if (rawType != null && !rawType.isBlank()) {
                String upper = rawType.toUpperCase(Locale.ROOT);
                updateChannel(GROUP_VEHICLE, "distance-type", new StringType("ICE".equals(upper) ? "GASOLINE" : upper));
            } else {
                updateChannel(GROUP_VEHICLE, "distance-type", UnDefType.UNDEF);
            }
        }
    }

    private void updateLocationStatus(LocationResponse response) {
        LocationData data = response.data();
        if (data == null) {
            setAllLocationChannelsToUndef();
            return;
        }

        updateChannel(GROUP_VEHICLE, "location-last-update", parseDateTimeOrUndef(data.timestamp()));

        GpsLocation loc = data.location();
        if (loc == null) {
            setAllLocationChannelsToUndef();
            return;
        }

        Double lat = parseDouble(loc.latitude());
        Double lon = parseDouble(loc.longitude());
        Double alt = parseDouble(loc.altitude());

        if (lat != null && lon != null) {
            updateChannel(GROUP_VEHICLE, "latitude", new DecimalType(lat));
            updateChannel(GROUP_VEHICLE, "longitude", new DecimalType(lon));
            if (alt != null) {
                updateChannel(GROUP_VEHICLE, "altitude", new QuantityType<>(alt, SIUnits.METRE));
                updateChannel(GROUP_VEHICLE, "location",
                        new PointType(new DecimalType(lat), new DecimalType(lon), new DecimalType(alt)));
            } else {
                updateChannel(GROUP_VEHICLE, "altitude", UnDefType.UNDEF);
                updateChannel(GROUP_VEHICLE, "location", new PointType(new DecimalType(lat), new DecimalType(lon)));
            }
        } else {
            updateChannel(GROUP_VEHICLE, "latitude", UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, "longitude", UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, "altitude", UnDefType.UNDEF);
            updateChannel(GROUP_VEHICLE, "location", UnDefType.UNDEF);
        }

        updateChannel(GROUP_VEHICLE, "speed", parseQuantityOrUndef(loc.extractRawSpeed(), SIUnits.KILOMETRE_PER_HOUR));

        Double heading = parseDouble(loc.heading());
        updateChannel(GROUP_VEHICLE, "heading",
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
            updateChannel(GROUP_VEHICLE, "ignition",
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
                        case "acc" -> updateChannel(GROUP_VEHICLE, "ignition-acc", state);
                        case "ign1" -> updateChannel(GROUP_VEHICLE, "ignition-ign1", state);
                        case "ign3" -> updateChannel(GROUP_VEHICLE, "ignition-ign3", state);
                        default -> {
                        }
                    }
                }
            }
        }

        String sleepMode = data.sleepMode();
        updateChannel(GROUP_VEHICLE, "sleep-mode",
                sleepMode != null ? new StringType(sleepMode.toUpperCase(Locale.ROOT)) : UnDefType.UNDEF);

        OdometerInfo odo = data.odometer();
        if (odo != null) {
            Double val = odo.value();
            if (val != null) {
                Unit<?> odoUnit = "mile".equalsIgnoreCase(odo.unit()) ? ImperialUnits.MILE
                        : MetricPrefix.KILO(SIUnits.METRE);
                updateChannel(GROUP_VEHICLE, "odometer", new QuantityType<>(val, odoUnit));
            }
        }

        TransmissionInfo trans = data.transmission();
        Boolean parking = trans != null ? trans.parkingPosition() : null;
        if (parking != null) {
            updateChannel(GROUP_VEHICLE, "parking-position", OnOffType.from(parking));
        }

        updateChannel(GROUP_VEHICLE, "last-update", parseDateTimeOrUndef(data.timestamp()));
    }

    private void updateVehicleStatus(VehicleStatusResponse response) {
        VehicleStatusData data = response.data();
        if (data == null) {
            return;
        }

        // 1. Doors and flaps
        List<LeftRightRow<String>> doors = data.doors();
        LeftRightRow<String> frontDoors = (doors != null && !doors.isEmpty()) ? doors.get(0) : null;
        LeftRightRow<String> rearDoors = (doors != null && doors.size() > 1) ? doors.get(1) : null;

        updateChannel(GROUP_DOORS, "door-front-left",
                toUpperStringOrUndef(frontDoors != null ? frontDoors.left() : null));
        updateChannel(GROUP_DOORS, "door-front-right",
                toUpperStringOrUndef(frontDoors != null ? frontDoors.right() : null));
        updateChannel(GROUP_DOORS, "door-rear-left", toUpperStringOrUndef(rearDoors != null ? rearDoors.left() : null));
        updateChannel(GROUP_DOORS, "door-rear-right",
                toUpperStringOrUndef(rearDoors != null ? rearDoors.right() : null));
        updateChannel(GROUP_DOORS, "hood", toUpperStringOrUndef(data.hood()));
        updateChannel(GROUP_DOORS, "trunk", toUpperStringOrUndef(data.trunk()));
        updateChannel(GROUP_DOORS, "sunroof", toUpperStringOrUndef(data.sunroof()));

        // 2. Tire pressure
        List<TireRow> tires = data.tire();
        updateTireRow(tires != null && !tires.isEmpty() ? tires.get(0) : null, "tire-pressure-front-left",
                "tire-pressure-front-right");
        updateTireRow(tires != null && tires.size() > 1 ? tires.get(1) : null, "tire-pressure-rear-left",
                "tire-pressure-rear-right");

        // 3. Warnings
        List<String> rawWarnings = data.warnings();
        List<String> warnings = rawWarnings != null ? rawWarnings : List.of();
        updateChannel(GROUP_WARNINGS, "warning-low-fuel", OnOffType.from(warnings.contains("lowFuel")));
        updateChannel(GROUP_WARNINGS, "warning-smartkey-battery", OnOffType.from(warnings.contains("smartKeyBattery")));
        updateChannel(GROUP_WARNINGS, "warning-wash-fluid", OnOffType.from(warnings.contains("washFluid")));
        updateChannel(GROUP_WARNINGS, "warning-brake-oil", OnOffType.from(warnings.contains("brakeOil")));
        updateChannel(GROUP_WARNINGS, "warning-engine-oil", OnOffType.from(warnings.contains("engineOil")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-headlamp", OnOffType.from(warnings.contains("frontHeadLamp")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-turn-front-left",
                OnOffType.from(warnings.contains("frontLeftTurnSignal")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-turn-front-right",
                OnOffType.from(warnings.contains("frontRightTurnSignal")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-stop-rear-left",
                OnOffType.from(warnings.contains("rearLeftStopLamp")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-stop-rear-right",
                OnOffType.from(warnings.contains("rearRightStopLamp")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-turn-rear-left",
                OnOffType.from(warnings.contains("rearLeftTurnSignal")));
        updateChannel(GROUP_WARNINGS, "warning-lamp-turn-rear-right",
                OnOffType.from(warnings.contains("rearRightTurnSignal")));

        updateChannel(GROUP_TIRES, "tire-warning-fl", OnOffType.from(warnings.contains("tirePressureFrontLeft")));
        updateChannel(GROUP_TIRES, "tire-warning-fr", OnOffType.from(warnings.contains("tirePressureFrontRight")));
        updateChannel(GROUP_TIRES, "tire-warning-rl", OnOffType.from(warnings.contains("tirePressureRearLeft")));
        updateChannel(GROUP_TIRES, "tire-warning-rr", OnOffType.from(warnings.contains("tirePressureRearRight")));
        updateChannel(GROUP_TIRES, "tire-warning-low-generic", OnOffType.from(warnings.contains("tirePressureLow")));

        // 4. Climate and temperatures
        ClimateControl climate = data.climateControl();
        if (climate != null) {
            updateChannel(GROUP_CLIMATE, "climate-mode", toUpperStringOrUndef(climate.mode()));
            updateChannel(GROUP_CLIMATE, "climate-status",
                    climate.status() != null ? OnOffType.from("on".equalsIgnoreCase(climate.status()))
                            : UnDefType.UNDEF);
            Integer speed = climate.speed();
            if (speed != null) {
                updateChannel(GROUP_CLIMATE, "climate-speed",
                        climate.speed() != null ? new DecimalType(speed) : UnDefType.UNDEF);
            }
            updateChannel(GROUP_CLIMATE, "climate-target-temp", toTemperatureOrUndef(climate.temperature()));
        } else {
            updateChannel(GROUP_CLIMATE, "climate-mode", UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, "climate-status", UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, "climate-speed", UnDefType.UNDEF);
            updateChannel(GROUP_CLIMATE, "climate-target-temp", UnDefType.UNDEF);
        }

        WindshieldInfo windshield = data.windshield();
        updateChannel(GROUP_CLIMATE, "windshield-heater",
                (windshield != null && windshield.defrost() != null)
                        ? OnOffType.from("on".equalsIgnoreCase(windshield.defrost()))
                        : UnDefType.UNDEF);

        updateChannel(GROUP_CLIMATE, "steering-wheel-heater",
                data.steeringWheelHeat() != null ? OnOffType.from("on".equalsIgnoreCase(data.steeringWheelHeat()))
                        : UnDefType.UNDEF);

        updateChannel(GROUP_CLIMATE, "outside-temperature", toTemperatureOrUndef(data.outsideTemperature()));

        // 5. Seats
        List<LeftRightRow<SeatInfo>> seats = data.seats();
        LeftRightRow<SeatInfo> frontSeats = (seats != null && !seats.isEmpty()) ? seats.get(0) : null;
        LeftRightRow<SeatInfo> rearSeats = (seats != null && seats.size() > 1) ? seats.get(1) : null;

        updateSeatSide(frontSeats != null ? frontSeats.left() : null, "seat-front-left");
        updateSeatSide(frontSeats != null ? frontSeats.right() : null, "seat-front-right");
        updateSeatSide(rearSeats != null ? rearSeats.left() : null, "seat-rear-left");
        updateSeatSide(rearSeats != null ? rearSeats.right() : null, "seat-rear-right");
    }

    // 6. Freezer
    private void updateFreezerStatus(FreezerResponse response) {
        FreezerData data = response.data();
        FreezerInfo freezer = data != null ? data.freezer() : null;
        if (freezer == null) {
            return;
        }
        String status = freezer.status();
        updateChannel(GROUP_FREEZER, "freezer-active",
                status != null ? OnOffType.from("on".equalsIgnoreCase(status)) : UnDefType.UNDEF);
        Double temp = freezer.temperature();
        updateChannel(GROUP_FREEZER, "freezer-temperature",
                temp != null ? new QuantityType<>(temp, SIUnits.CELSIUS) : UnDefType.UNDEF);
    }

    // =========================================================================
    // HELPER METHODS FOR TYPE CONVERSION
    // =========================================================================
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
        if (raw == null || raw == 255.0) {
            return UnDefType.UNDEF;
        }
        double scaled = switch (apiUnit) {
            case "kpa" -> raw * 5.0;
            case "bar" -> raw / 10.0;
            default -> raw;
        };
        return new QuantityType<>(scaled, uomUnit);
    }

    private void updateSeatSide(@Nullable SeatInfo seat, String prefix) {
        if (seat == null) {
            updateChannel(GROUP_SEATS, prefix + "-heat", UnDefType.UNDEF);
            updateChannel(GROUP_SEATS, prefix + "-vent", UnDefType.UNDEF);
            return;
        }
        updateChannel(GROUP_SEATS, prefix + "-heat", new StringType(resolveSeatLevel(seat.heat())));
        updateChannel(GROUP_SEATS, prefix + "-vent", new StringType(resolveSeatLevel(seat.ventilation())));
    }

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
        updateChannel(GROUP_VEHICLE, "soc", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "charging", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "plugin", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "charge-status", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "remain-time", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "target-soc-standard", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "target-soc-quick", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "capacity", UnDefType.UNDEF);
    }

    private void setAllLocationChannelsToUndef() {
        updateChannel(GROUP_VEHICLE, "location-last-update", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "latitude", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "longitude", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "altitude", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "location", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "speed", UnDefType.UNDEF);
        updateChannel(GROUP_VEHICLE, "heading", UnDefType.UNDEF);
    }

    private @Nullable PleosAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof PleosAccountHandler handler) {
            return handler;
        }
        return null;
    }
}
