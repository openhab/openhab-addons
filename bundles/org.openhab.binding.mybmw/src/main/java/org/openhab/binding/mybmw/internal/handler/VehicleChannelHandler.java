/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeProfile;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeSession;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSettings;
import org.openhab.binding.mybmw.internal.dto.properties.CBS;
import org.openhab.binding.mybmw.internal.dto.properties.DoorsWindows;
import org.openhab.binding.mybmw.internal.dto.properties.Location;
import org.openhab.binding.mybmw.internal.dto.properties.Tires;
import org.openhab.binding.mybmw.internal.dto.status.CCMMessage;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileUtils;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileUtils.TimedChannel;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleChannelHandler} handles Channel updates
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
@NonNullByDefault
public abstract class VehicleChannelHandler extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(VehicleChannelHandler.class);
    protected boolean hasFuel = false;
    protected boolean isElectric = false;
    protected boolean isHybrid = false;

    // List Interfaces
    protected List<CBS> serviceList = new ArrayList<CBS>();
    protected String selectedService = Constants.UNDEF;
    protected List<CCMMessage> checkControlList = new ArrayList<CCMMessage>();
    protected String selectedCC = Constants.UNDEF;
    protected List<ChargeSession> sessionList = new ArrayList<ChargeSession>();
    protected String selectedSession = Constants.UNDEF;

    protected MyBMWCommandOptionProvider commandOptionProvider;
    private LocationProvider locationProvider;

    // Data Caches
    protected Optional<String> vehicleStatusCache = Optional.empty();
    protected Optional<byte[]> imageCache = Optional.empty();

    public VehicleChannelHandler(Thing thing, MyBMWCommandOptionProvider cop, LocationProvider lp, String type) {
        super(thing);
        commandOptionProvider = cop;
        locationProvider = lp;
        if (lp.getLocation() == null) {
            logger.debug("Home location not available");
        }

        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.MILD_HYBRID.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;

        setOptions(CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_COMMAND, RemoteServiceUtils.getOptions(isElectric));
    }

    private void setOptions(final String group, final String id, List<CommandOption> options) {
        commandOptionProvider.setCommandOptions(new ChannelUID(thing.getUID(), group, id), options);
    }

    protected void updateChannel(final String group, final String id, final State state) {
        updateState(new ChannelUID(thing.getUID(), group, id), state);
    }

    protected void updateChargeStatistics(ChargeStatisticsContainer csc) {
        updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, TITLE, StringType.valueOf(csc.description));
        updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, ENERGY,
                QuantityType.valueOf(csc.statistics.totalEnergyCharged, Units.KILOWATT_HOUR));
        updateChannel(CHANNEL_GROUP_CHARGE_STATISTICS, SESSIONS,
                DecimalType.valueOf(Integer.toString(csc.statistics.numberOfChargingSessions)));
    }

    protected void updateVehicle(Vehicle v) {
        updateVehicleStatus(v);
        updateRange(v);
        updateDoors(v.properties.doorsAndWindows);
        updateWindows(v.properties.doorsAndWindows);
        updatePosition(v.properties.vehicleLocation);
        updateServices(v.properties.serviceRequired);
        updateCheckControls(v.status.checkControlMessages);
        updateTires(v.properties.tires);
    }

    private void updateTires(@Nullable Tires tires) {
        if (tires == null) {
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_CURRENT, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_TARGET, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_CURRENT, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_TARGET, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_CURRENT, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_TARGET, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_CURRENT, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_TARGET, UnDefType.UNDEF);
        } else {
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_CURRENT,
                    QuantityType.valueOf(tires.frontLeft.status.currentPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_LEFT_TARGET,
                    QuantityType.valueOf(tires.frontLeft.status.targetPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_CURRENT,
                    QuantityType.valueOf(tires.frontRight.status.currentPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, FRONT_RIGHT_TARGET,
                    QuantityType.valueOf(tires.frontRight.status.targetPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_CURRENT,
                    QuantityType.valueOf(tires.rearLeft.status.currentPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, REAR_LEFT_TARGET,
                    QuantityType.valueOf(tires.rearLeft.status.targetPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_CURRENT,
                    QuantityType.valueOf(tires.rearRight.status.currentPressure / 100, Units.BAR));
            updateChannel(CHANNEL_GROUP_TIRES, REAR_RIGHT_TARGET,
                    QuantityType.valueOf(tires.rearRight.status.targetPressure / 100, Units.BAR));
        }
    }

    protected void updateVehicleStatus(Vehicle v) {
        updateChannel(CHANNEL_GROUP_STATUS, LOCK, Converter.getLockState(v.properties.areDoorsLocked));
        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_DATE,
                VehicleStatusUtils.getNextServiceDate(v.properties.serviceRequired));
        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_MILEAGE,
                VehicleStatusUtils.getNextServiceMileage(v.properties.serviceRequired));
        updateChannel(CHANNEL_GROUP_STATUS, CHECK_CONTROL,
                StringType.valueOf(v.status.checkControlMessagesGeneralState));
        updateChannel(CHANNEL_GROUP_STATUS, MOTION, OnOffType.from(v.properties.inMotion));
        updateChannel(CHANNEL_GROUP_STATUS, LAST_UPDATE,
                DateTimeType.valueOf(Converter.zonedToLocalDateTime(v.properties.lastUpdatedAt)));
        updateChannel(CHANNEL_GROUP_STATUS, DOORS, Converter.getClosedState(v.properties.areDoorsClosed));
        updateChannel(CHANNEL_GROUP_STATUS, WINDOWS, Converter.getClosedState(v.properties.areWindowsClosed));

        if (isElectric) {
            updateChannel(CHANNEL_GROUP_STATUS, PLUG_CONNECTION,
                    Converter.getConnectionState(v.properties.chargingState.isChargerConnected));
            updateChannel(CHANNEL_GROUP_STATUS, CHARGE_STATUS,
                    StringType.valueOf(Converter.toTitleCase(VehicleStatusUtils.getChargStatus(v))));
            updateChannel(CHANNEL_GROUP_STATUS, CHARGE_INFO,
                    StringType.valueOf(Converter.getLocalTime(VehicleStatusUtils.getChargeInfo(v))));
        }
    }

    protected void updateRange(Vehicle v) {
        // get the right unit
        Unit<Length> lengthUnit = VehicleStatusUtils.getLengthUnit(v.status.fuelIndicators);
        if (lengthUnit == null) {
            return;
        }
        if (isElectric) {
            int rangeElectric = VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, v);
            QuantityType<Length> qtElectricRange = QuantityType.valueOf(rangeElectric, lengthUnit);
            QuantityType<Length> qtElectricRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeElectric),
                    lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_ELECTRIC, qtElectricRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC, qtElectricRadius);
        }
        if (hasFuel) {
            int rangeFuel = VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, v);
            QuantityType<Length> qtFuelRange = QuantityType.valueOf(rangeFuel, lengthUnit);
            QuantityType<Length> qtFuelRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeFuel), lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_FUEL, qtFuelRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL, qtFuelRadius);
        }
        if (isHybrid) {
            int rangeCombined = VehicleStatusUtils.getRange(Constants.PHEV, v);
            QuantityType<Length> qtHybridRange = QuantityType.valueOf(rangeCombined, lengthUnit);
            QuantityType<Length> qtHybridRadius = QuantityType.valueOf(Converter.guessRangeRadius(rangeCombined),
                    lengthUnit);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_HYBRID, qtHybridRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID, qtHybridRadius);
        }
        if (v.status.currentMileage.mileage == Constants.INT_UNDEF) {
            updateChannel(CHANNEL_GROUP_RANGE, MILEAGE, UnDefType.UNDEF);
        } else {
            updateChannel(CHANNEL_GROUP_RANGE, MILEAGE,
                    QuantityType.valueOf(v.status.currentMileage.mileage, lengthUnit));
        }
        if (isElectric) {
            updateChannel(CHANNEL_GROUP_RANGE, SOC,
                    QuantityType.valueOf(v.properties.chargingState.chargePercentage, Units.PERCENT));
        }
        if (hasFuel) {
            updateChannel(CHANNEL_GROUP_RANGE, REMAINING_FUEL,
                    QuantityType.valueOf(v.properties.fuelLevel.value, Units.LITRE));
        }
    }

    protected void updateCheckControls(List<CCMMessage> ccl) {
        if (ccl.isEmpty()) {
            // No Check Control available - show not active
            CCMMessage ccm = new CCMMessage();
            ccm.title = Constants.NO_ENTRIES;
            ccm.longDescription = Constants.NO_ENTRIES;
            ccm.state = Constants.NO_ENTRIES;
            ccl.add(ccm);
        }

        // add all elements to options
        checkControlList = ccl;
        List<CommandOption> ccmDescriptionOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CCMMessage ccEntry : checkControlList) {
            ccmDescriptionOptions.add(new CommandOption(Integer.toString(index), ccEntry.title));
            if (selectedCC.equals(ccEntry.title)) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_CHECK_CONTROL, NAME, ccmDescriptionOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectCheckControl(0);
        }
    }

    protected void selectCheckControl(int index) {
        if (index >= 0 && index < checkControlList.size()) {
            CCMMessage ccEntry = checkControlList.get(index);
            selectedCC = ccEntry.title;
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, NAME, StringType.valueOf(ccEntry.title));
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, DETAILS, StringType.valueOf(ccEntry.longDescription));
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, SEVERITY, StringType.valueOf(ccEntry.state));
        }
    }

    protected void updateServices(List<CBS> sl) {
        // if list is empty add "undefined" element
        if (sl.isEmpty()) {
            CBS cbsm = new CBS();
            cbsm.type = Constants.NO_ENTRIES;
            sl.add(cbsm);
        }

        // add all elements to options
        serviceList = sl;
        List<CommandOption> serviceNameOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CBS serviceEntry : serviceList) {
            // create StateOption with "value = list index" and "label = human readable string"
            serviceNameOptions.add(new CommandOption(Integer.toString(index), serviceEntry.type));
            if (selectedService.equals(serviceEntry.type)) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_SERVICE, NAME, serviceNameOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectService(0);
        }
    }

    protected void selectService(int index) {
        if (index >= 0 && index < serviceList.size()) {
            CBS serviceEntry = serviceList.get(index);
            selectedService = serviceEntry.type;
            updateChannel(CHANNEL_GROUP_SERVICE, NAME, StringType.valueOf(Converter.toTitleCase(serviceEntry.type)));
            if (serviceEntry.dateTime != null) {
                updateChannel(CHANNEL_GROUP_SERVICE, DATE,
                        DateTimeType.valueOf(Converter.zonedToLocalDateTime(serviceEntry.dateTime)));
            } else {
                updateChannel(CHANNEL_GROUP_SERVICE, DATE, UnDefType.UNDEF);
            }
            if (serviceEntry.distance != null) {
                if (Constants.KILOMETERS_JSON.equals(serviceEntry.distance.units)) {
                    updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE,
                            QuantityType.valueOf(serviceEntry.distance.value, Constants.KILOMETRE_UNIT));
                } else {
                    updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE,
                            QuantityType.valueOf(serviceEntry.distance.value, ImperialUnits.MILE));
                }
            } else {
                updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE,
                        QuantityType.valueOf(Constants.INT_UNDEF, Constants.KILOMETRE_UNIT));
            }
        }
    }

    protected void updateSessions(List<ChargeSession> sl) {
        // if list is empty add "undefined" element
        if (sl.isEmpty()) {
            ChargeSession cs = new ChargeSession();
            cs.title = Constants.NO_ENTRIES;
            sl.add(cs);
        }

        // add all elements to options
        sessionList = sl;
        List<CommandOption> sessionNameOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (ChargeSession session : sessionList) {
            // create StateOption with "value = list index" and "label = human readable string"
            sessionNameOptions.add(new CommandOption(Integer.toString(index), session.title));
            if (selectedService.equals(session.title)) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_CHARGE_SESSION, TITLE, sessionNameOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectSession(0);
        }
    }

    protected void selectSession(int index) {
        if (index >= 0 && index < sessionList.size()) {
            ChargeSession sessionEntry = sessionList.get(index);
            selectedService = sessionEntry.title;
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, TITLE, StringType.valueOf(sessionEntry.title));
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, SUBTITLE, StringType.valueOf(sessionEntry.subtitle));
            if (sessionEntry.energyCharged != null) {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ENERGY, StringType.valueOf(sessionEntry.energyCharged));
            } else {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ENERGY, StringType.valueOf(Constants.UNDEF));
            }
            if (sessionEntry.issues != null) {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ISSUE, StringType.valueOf(sessionEntry.issues));
            } else {
                updateChannel(CHANNEL_GROUP_CHARGE_SESSION, ISSUE, StringType.valueOf(Constants.HYPHEN));
            }
            updateChannel(CHANNEL_GROUP_CHARGE_SESSION, STATUS, StringType.valueOf(sessionEntry.sessionStatus));
        }
    }

    protected void updateChargeProfile(ChargeProfile cp) {
        ChargeProfileWrapper cpw = new ChargeProfileWrapper(cp);

        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_PREFERENCE, StringType.valueOf(cpw.getPreference()));
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_MODE, StringType.valueOf(cpw.getMode()));
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_CONTROL, StringType.valueOf(cpw.getControlType()));
        ChargingSettings cs = cpw.getChargeSettings();
        if (cs != null) {
            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_TARGET,
                    DecimalType.valueOf(Integer.toString(cs.targetSoc)));
            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_LIMIT,
                    OnOffType.from(cs.isAcCurrentLimitActive));
        }
        final Boolean climate = cpw.isEnabled(ProfileKey.CLIMATE);
        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, CHARGE_PROFILE_CLIMATE,
                climate == null ? UnDefType.UNDEF : OnOffType.from(climate));
        updateTimedState(cpw, ProfileKey.WINDOWSTART);
        updateTimedState(cpw, ProfileKey.WINDOWEND);
        updateTimedState(cpw, ProfileKey.TIMER1);
        updateTimedState(cpw, ProfileKey.TIMER2);
        updateTimedState(cpw, ProfileKey.TIMER3);
        updateTimedState(cpw, ProfileKey.TIMER4);
    }

    protected void updateTimedState(ChargeProfileWrapper profile, ProfileKey key) {
        final TimedChannel timed = ChargeProfileUtils.getTimedChannel(key);
        if (timed != null) {
            final LocalTime time = profile.getTime(key);
            updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, timed.time,
                    time.equals(Constants.NULL_LOCAL_TIME) ? UnDefType.UNDEF
                            : new DateTimeType(ZonedDateTime.of(Constants.EPOCH_DAY, time, ZoneId.systemDefault())));
            if (timed.timer != null) {
                final Boolean enabled = profile.isEnabled(key);
                updateChannel(CHANNEL_GROUP_CHARGE_PROFILE, timed.timer + CHARGE_ENABLED,
                        enabled == null ? UnDefType.UNDEF : OnOffType.from(enabled));
                if (timed.hasDays) {
                    final Set<DayOfWeek> days = profile.getDays(key);
                    EnumSet.allOf(DayOfWeek.class).forEach(day -> {
                        updateChannel(CHANNEL_GROUP_CHARGE_PROFILE,
                                timed.timer + ChargeProfileUtils.getDaysChannel(day),
                                days == null ? UnDefType.UNDEF : OnOffType.from(days.contains(day)));
                    });
                }
            }
        }
    }

    protected void updateDoors(DoorsWindows dw) {
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(dw.doors.driverFront)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(dw.doors.driverRear)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(dw.doors.passengerFront)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(dw.doors.passengerRear)));
        updateChannel(CHANNEL_GROUP_DOORS, TRUNK, StringType.valueOf(Converter.toTitleCase(dw.trunk)));
        updateChannel(CHANNEL_GROUP_DOORS, HOOD, StringType.valueOf(Converter.toTitleCase(dw.hood)));
    }

    protected void updateWindows(DoorsWindows dw) {
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(dw.windows.driverFront)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(dw.windows.driverRear)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(dw.windows.passengerFront)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(dw.windows.passengerRear)));
        updateChannel(CHANNEL_GROUP_DOORS, SUNROOF, StringType.valueOf(Converter.toTitleCase(dw.moonroof)));
    }

    protected void updatePosition(Location pos) {
        if (pos.coordinates.latitude < 0) {
            updateChannel(CHANNEL_GROUP_LOCATION, GPS, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_LOCATION, HEADING, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_LOCATION, ADDRESS, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE, UnDefType.UNDEF);
        } else {
            PointType vehicleLocation = PointType.valueOf(
                    Double.toString(pos.coordinates.latitude) + "," + Double.toString(pos.coordinates.longitude));
            updateChannel(CHANNEL_GROUP_LOCATION, GPS, vehicleLocation);
            updateChannel(CHANNEL_GROUP_LOCATION, HEADING, QuantityType.valueOf(pos.heading, Units.DEGREE_ANGLE));
            updateChannel(CHANNEL_GROUP_LOCATION, ADDRESS, StringType.valueOf(pos.address.formatted));
            PointType homeLocation = locationProvider.getLocation();
            if (homeLocation != null) {
                updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE,
                        QuantityType.valueOf(vehicleLocation.distanceFrom(homeLocation).intValue(), SIUnits.METRE));
            } else {
                updateChannel(CHANNEL_GROUP_LOCATION, HOME_DISTANCE, UnDefType.UNDEF);
            }
        }
    }
}
