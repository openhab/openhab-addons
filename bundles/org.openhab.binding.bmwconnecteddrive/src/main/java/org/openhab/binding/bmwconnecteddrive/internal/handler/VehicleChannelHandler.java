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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.Destination;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CCMMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Doors;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Windows;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils.TimedChannel;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.RemoteServiceUtils;
import org.openhab.binding.bmwconnecteddrive.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link VehicleChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
@NonNullByDefault
public abstract class VehicleChannelHandler extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(VehicleChannelHandler.class);
    protected boolean imperial = false;
    protected boolean hasFuel = false;
    protected boolean isElectric = false;
    protected boolean isHybrid = false;

    // List Interfaces
    protected List<CBSMessage> serviceList = new ArrayList<CBSMessage>();
    protected String selectedService = Constants.UNDEF;
    protected List<CCMMessage> checkControlList = new ArrayList<CCMMessage>();
    protected String selectedCC = Constants.UNDEF;
    protected List<Destination> destinationList = new ArrayList<Destination>();
    protected String selectedDestination = Constants.UNDEF;

    protected BMWConnectedDriveOptionProvider optionProvider;

    // Data Caches
    protected Optional<String> vehicleStatusCache = Optional.empty();
    protected Optional<String> lastTripCache = Optional.empty();
    protected Optional<String> allTripsCache = Optional.empty();
    protected Optional<String> chargeProfileCache = Optional.empty();
    protected Optional<String> rangeMapCache = Optional.empty();
    protected Optional<String> destinationCache = Optional.empty();
    protected Optional<byte[]> imageCache = Optional.empty();

    public VehicleChannelHandler(Thing thing, BMWConnectedDriveOptionProvider op, String type, boolean imperial) {
        super(thing);
        optionProvider = op;

        this.imperial = imperial;
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;

        setOptions(CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_COMMAND, RemoteServiceUtils.getOptions(isElectric));
    }

    private void setOptions(final String group, final String id, List<StateOption> options) {
        optionProvider.setStateOptions(new ChannelUID(thing.getUID(), group, id), options);
    }

    protected void updateChannel(final String group, final String id, final State state) {
        updateState(new ChannelUID(thing.getUID(), group, id), state);
    }

    protected void updateCheckControls(List<CCMMessage> ccl) {
        if (ccl.isEmpty()) {
            // No Check Control available - show not active
            CCMMessage ccm = new CCMMessage();
            ccm.ccmDescriptionLong = Constants.NO_ENTRIES;
            ccm.ccmDescriptionShort = Constants.NO_ENTRIES;
            ccm.ccmId = -1;
            ccm.ccmMileage = -1;
            ccl.add(ccm);
        }

        // add all elements to options
        checkControlList = ccl;
        List<StateOption> ccmDescriptionOptions = new ArrayList<>();
        List<StateOption> ccmDetailsOptions = new ArrayList<>();
        List<StateOption> ccmMileageOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CCMMessage ccEntry : checkControlList) {
            ccmDescriptionOptions.add(new StateOption(Integer.toString(index), ccEntry.ccmDescriptionShort));
            ccmDetailsOptions.add(new StateOption(Integer.toString(index), ccEntry.ccmDescriptionLong));
            ccmMileageOptions.add(new StateOption(Integer.toString(index), Integer.toString(ccEntry.ccmMileage)));
            if (selectedCC.equals(ccEntry.ccmDescriptionShort)) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_CHECK_CONTROL, NAME, ccmDescriptionOptions);
        setOptions(CHANNEL_GROUP_CHECK_CONTROL, DETAILS, ccmDetailsOptions);
        setOptions(CHANNEL_GROUP_CHECK_CONTROL, MILEAGE, ccmMileageOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectCheckControl(0);
        }
    }

    protected void selectCheckControl(int index) {
        if (index >= 0 && index < checkControlList.size()) {
            CCMMessage ccEntry = checkControlList.get(index);
            selectedCC = ccEntry.ccmDescriptionShort;
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, NAME, StringType.valueOf(ccEntry.ccmDescriptionShort));
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, DETAILS, StringType.valueOf(ccEntry.ccmDescriptionLong));
            updateChannel(CHANNEL_GROUP_CHECK_CONTROL, MILEAGE, QuantityType.valueOf(
                    Converter.round(ccEntry.ccmMileage), imperial ? ImperialUnits.MILE : Constants.KILOMETRE_UNIT));
        }
    }

    protected void updateServices(List<CBSMessage> sl) {
        // if list is empty add "undefined" element
        if (sl.isEmpty()) {
            CBSMessage cbsm = new CBSMessage();
            cbsm.cbsType = Constants.NO_ENTRIES;
            cbsm.cbsDescription = Constants.NO_ENTRIES;
            sl.add(cbsm);
        }

        // add all elements to options
        serviceList = sl;
        List<StateOption> serviceNameOptions = new ArrayList<>();
        List<StateOption> serviceDetailsOptions = new ArrayList<>();
        List<StateOption> serviceDateOptions = new ArrayList<>();
        List<StateOption> serviceMileageOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CBSMessage serviceEntry : serviceList) {
            // create StateOption with "value = list index" and "label = human readable string"
            serviceNameOptions.add(new StateOption(Integer.toString(index), serviceEntry.getType()));
            serviceDetailsOptions.add(new StateOption(Integer.toString(index), serviceEntry.getDescription()));
            serviceDateOptions.add(new StateOption(Integer.toString(index), serviceEntry.getDueDate()));
            serviceMileageOptions
                    .add(new StateOption(Integer.toString(index), Integer.toString(serviceEntry.cbsRemainingMileage)));
            if (selectedService.equals(serviceEntry.getType())) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_SERVICE, NAME, serviceNameOptions);
        setOptions(CHANNEL_GROUP_SERVICE, DETAILS, serviceDetailsOptions);
        setOptions(CHANNEL_GROUP_SERVICE, DATE, serviceDateOptions);
        setOptions(CHANNEL_GROUP_SERVICE, MILEAGE, serviceMileageOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectService(0);
        }
    }

    protected void selectService(int index) {
        if (index >= 0 && index < serviceList.size()) {
            CBSMessage serviceEntry = serviceList.get(index);
            selectedService = serviceEntry.cbsType;
            updateChannel(CHANNEL_GROUP_SERVICE, NAME,
                    StringType.valueOf(Converter.toTitleCase(serviceEntry.getType())));
            updateChannel(CHANNEL_GROUP_SERVICE, DETAILS,
                    StringType.valueOf(Converter.toTitleCase(serviceEntry.getDescription())));
            updateChannel(CHANNEL_GROUP_SERVICE, DATE,
                    DateTimeType.valueOf(Converter.getLocalDateTime(serviceEntry.getDueDate())));
            updateChannel(CHANNEL_GROUP_SERVICE, MILEAGE,
                    QuantityType.valueOf(Converter.round(serviceEntry.cbsRemainingMileage),
                            imperial ? ImperialUnits.MILE : Constants.KILOMETRE_UNIT));
        }
    }

    protected void updateDestinations(List<Destination> dl) {
        // if list is empty add "undefined" element
        if (dl.isEmpty()) {
            Destination dest = new Destination();
            dest.city = Constants.NO_ENTRIES;
            dest.lat = -1;
            dest.lon = -1;
            dl.add(dest);
        }

        // add all elements to options
        destinationList = dl;
        List<StateOption> destinationNameOptions = new ArrayList<>();
        List<StateOption> destinationGPSOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (Destination destination : destinationList) {
            destinationNameOptions.add(new StateOption(Integer.toString(index), destination.getAddress()));
            destinationGPSOptions.add(new StateOption(Integer.toString(index), destination.getCoordinates()));
            if (selectedDestination.equals(destination.getAddress())) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(CHANNEL_GROUP_DESTINATION, NAME, destinationNameOptions);
        setOptions(CHANNEL_GROUP_DESTINATION, GPS, destinationGPSOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectDestination(0);
        }
    }

    protected void selectDestination(int index) {
        if (index >= 0 && index < destinationList.size()) {
            Destination destinationEntry = destinationList.get(index);
            // update selected Item
            selectedDestination = destinationEntry.getAddress();
            // update coordinates according to new set location
            updateChannel(CHANNEL_GROUP_DESTINATION, NAME, StringType.valueOf(destinationEntry.getAddress()));
            updateChannel(CHANNEL_GROUP_DESTINATION, GPS, PointType.valueOf(destinationEntry.getCoordinates()));
        }
    }

    protected void updateAllTrips(AllTrips allTrips) {
        QuantityType<Length> qtTotalElectric = QuantityType
                .valueOf(Converter.round(allTrips.totalElectricDistance.userTotal), Constants.KILOMETRE_UNIT);
        QuantityType<Length> qtLongestElectricRange = QuantityType
                .valueOf(Converter.round(allTrips.chargecycleRange.userHigh), Constants.KILOMETRE_UNIT);
        QuantityType<Length> qtDistanceSinceCharge = QuantityType
                .valueOf(Converter.round(allTrips.chargecycleRange.userCurrentChargeCycle), Constants.KILOMETRE_UNIT);

        updateChannel(CHANNEL_GROUP_LIFETIME, TOTAL_DRIVEN_DISTANCE,
                imperial ? Converter.getMiles(qtTotalElectric) : qtTotalElectric);
        updateChannel(CHANNEL_GROUP_LIFETIME, SINGLE_LONGEST_DISTANCE,
                imperial ? Converter.getMiles(qtLongestElectricRange) : qtLongestElectricRange);
        updateChannel(CHANNEL_GROUP_LAST_TRIP, DISTANCE_SINCE_CHARGING,
                imperial ? Converter.getMiles(qtDistanceSinceCharge) : qtDistanceSinceCharge);

        // Conversion from kwh/100km to kwh/10mi has to be done manually
        double avgConsumotion = imperial ? allTrips.avgElectricConsumption.userAverage * Converter.MILES_TO_KM_RATIO
                : allTrips.avgElectricConsumption.userAverage;
        double avgCombinedConsumption = imperial
                ? allTrips.avgCombinedConsumption.userAverage * Converter.MILES_TO_KM_RATIO
                : allTrips.avgCombinedConsumption.userAverage;
        double avgRecuperation = imperial ? allTrips.avgRecuperation.userAverage * Converter.MILES_TO_KM_RATIO
                : allTrips.avgRecuperation.userAverage;

        updateChannel(CHANNEL_GROUP_LIFETIME, AVG_CONSUMPTION,
                QuantityType.valueOf(Converter.round(avgConsumotion), Units.KILOWATT_HOUR));
        updateChannel(CHANNEL_GROUP_LIFETIME, AVG_COMBINED_CONSUMPTION,
                QuantityType.valueOf(Converter.round(avgCombinedConsumption), Units.LITRE));
        updateChannel(CHANNEL_GROUP_LIFETIME, AVG_RECUPERATION,
                QuantityType.valueOf(Converter.round(avgRecuperation), Units.KILOWATT_HOUR));
    }

    protected void updateLastTrip(LastTrip trip) {
        // Whyever the Last Trip DateTime is delivered without offest - so LocalTime
        updateChannel(CHANNEL_GROUP_LAST_TRIP, DATE,
                DateTimeType.valueOf(Converter.getLocalDateTimeWithoutOffest(trip.date)));
        updateChannel(CHANNEL_GROUP_LAST_TRIP, DURATION, QuantityType.valueOf(trip.duration, Units.MINUTE));

        QuantityType<Length> qtTotalDistance = QuantityType.valueOf(Converter.round(trip.totalDistance),
                Constants.KILOMETRE_UNIT);
        updateChannel(CHANNEL_GROUP_LAST_TRIP, DISTANCE,
                imperial ? Converter.getMiles(qtTotalDistance) : qtTotalDistance);

        // Conversion from kwh/100km to kwh/10mi has to be done manually
        double avgConsumtption = imperial ? trip.avgElectricConsumption * Converter.MILES_TO_KM_RATIO
                : trip.avgElectricConsumption;
        double avgCombinedConsumption = imperial ? trip.avgCombinedConsumption * Converter.MILES_TO_KM_RATIO
                : trip.avgCombinedConsumption;
        double avgRecuperation = imperial ? trip.avgRecuperation * Converter.MILES_TO_KM_RATIO : trip.avgRecuperation;

        updateChannel(CHANNEL_GROUP_LAST_TRIP, AVG_CONSUMPTION,
                QuantityType.valueOf(Converter.round(avgConsumtption), Units.KILOWATT_HOUR));
        updateChannel(CHANNEL_GROUP_LAST_TRIP, AVG_COMBINED_CONSUMPTION,
                QuantityType.valueOf(Converter.round(avgCombinedConsumption), Units.LITRE));
        updateChannel(CHANNEL_GROUP_LAST_TRIP, AVG_RECUPERATION,
                QuantityType.valueOf(Converter.round(avgRecuperation), Units.KILOWATT_HOUR));
    }

    protected void updateChargeProfileFromContent(String content) {
        ChargeProfileWrapper.fromJson(content).ifPresent(this::updateChargeProfile);
    }

    protected void updateChargeProfile(ChargeProfileWrapper wrapper) {
        updateChannel(CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_PREFERENCE,
                StringType.valueOf(Converter.toTitleCase(wrapper.getPreference())));
        updateChannel(CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_MODE,
                StringType.valueOf(Converter.toTitleCase(wrapper.getMode())));
        final Boolean climate = wrapper.isEnabled(ProfileKey.CLIMATE);
        updateChannel(CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_CLIMATE,
                climate == null ? UnDefType.UNDEF : OnOffType.from(climate));
        updateTimedState(wrapper, ProfileKey.WINDOWSTART);
        updateTimedState(wrapper, ProfileKey.WINDOWEND);
        updateTimedState(wrapper, ProfileKey.TIMER1);
        updateTimedState(wrapper, ProfileKey.TIMER2);
        updateTimedState(wrapper, ProfileKey.TIMER3);
        updateTimedState(wrapper, ProfileKey.OVERRIDE);
    }

    protected void updateTimedState(ChargeProfileWrapper profile, ProfileKey key) {
        final TimedChannel timed = ChargeProfileUtils.getTimedChannel(key);
        if (timed != null) {
            final LocalTime time = profile.getTime(key);
            updateChannel(CHANNEL_GROUP_CHARGE, timed.time, time == null ? UnDefType.UNDEF
                    : new DateTimeType(ZonedDateTime.of(Constants.EPOCH_DAY, time, ZoneId.systemDefault())));
            if (timed.timer != null) {
                final Boolean enabled = profile.isEnabled(key);
                updateChannel(CHANNEL_GROUP_CHARGE, timed.timer + CHARGE_ENABLED,
                        enabled == null ? UnDefType.UNDEF : OnOffType.from(enabled));
                if (timed.hasDays) {
                    final Set<DayOfWeek> days = profile.getDays(key);
                    updateChannel(CHANNEL_GROUP_CHARGE, timed.timer + CHARGE_DAYS,
                            days == null ? UnDefType.UNDEF : StringType.valueOf(ChargeProfileUtils.formatDays(days)));
                    EnumSet.allOf(DayOfWeek.class).forEach(day -> {
                        updateChannel(CHANNEL_GROUP_CHARGE, timed.timer + ChargeProfileUtils.getDaysChannel(day),
                                days == null ? UnDefType.UNDEF : OnOffType.from(days.contains(day)));
                    });
                }
            }
        }
    }

    protected void updateDoors(Doors doorState) {
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(doorState.doorDriverFront)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(doorState.doorDriverRear)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(doorState.doorPassengerFront)));
        updateChannel(CHANNEL_GROUP_DOORS, DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(doorState.doorPassengerRear)));
        updateChannel(CHANNEL_GROUP_DOORS, TRUNK, StringType.valueOf(Converter.toTitleCase(doorState.trunk)));
        updateChannel(CHANNEL_GROUP_DOORS, HOOD, StringType.valueOf(Converter.toTitleCase(doorState.hood)));
    }

    protected void updateWindows(Windows windowState) {
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_FRONT,
                StringType.valueOf(Converter.toTitleCase(windowState.windowDriverFront)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_REAR,
                StringType.valueOf(Converter.toTitleCase(windowState.windowDriverRear)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_FRONT,
                StringType.valueOf(Converter.toTitleCase(windowState.windowPassengerFront)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_REAR,
                StringType.valueOf(Converter.toTitleCase(windowState.windowPassengerRear)));
        updateChannel(CHANNEL_GROUP_DOORS, WINDOW_REAR,
                StringType.valueOf(Converter.toTitleCase(windowState.rearWindow)));
        updateChannel(CHANNEL_GROUP_DOORS, SUNROOF, StringType.valueOf(Converter.toTitleCase(windowState.sunroof)));
    }

    protected void updatePosition(Position pos) {
        updateChannel(CHANNEL_GROUP_LOCATION, GPS, PointType.valueOf(pos.getCoordinates()));
        updateChannel(CHANNEL_GROUP_LOCATION, HEADING, QuantityType.valueOf(pos.heading, Units.DEGREE_ANGLE));
    }

    protected void updateVehicleStatus(VehicleStatus vStatus) {
        // Vehicle Status
        updateChannel(CHANNEL_GROUP_STATUS, LOCK, StringType.valueOf(Converter.toTitleCase(vStatus.doorLockState)));

        // Service Updates
        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_DATE,
                DateTimeType.valueOf(Converter.getLocalDateTime(VehicleStatusUtils.getNextServiceDate(vStatus))));

        updateChannel(CHANNEL_GROUP_STATUS, SERVICE_MILEAGE,
                QuantityType.valueOf(Converter.round(VehicleStatusUtils.getNextServiceMileage(vStatus)),
                        imperial ? ImperialUnits.MILE : Constants.KILOMETRE_UNIT));
        // CheckControl Active?
        updateChannel(CHANNEL_GROUP_STATUS, CHECK_CONTROL,
                StringType.valueOf(Converter.toTitleCase(VehicleStatusUtils.checkControlActive(vStatus))));
        // last update Time
        updateChannel(CHANNEL_GROUP_STATUS, LAST_UPDATE,
                DateTimeType.valueOf(Converter.getLocalDateTime(VehicleStatusUtils.getUpdateTime(vStatus))));
        // last update reason
        updateChannel(CHANNEL_GROUP_STATUS, LAST_UPDATE_REASON,
                StringType.valueOf(Converter.toTitleCase(vStatus.updateReason)));

        Doors doorState = null;
        try {
            doorState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Doors.class);
        } catch (JsonSyntaxException jse) {
            logger.debug("Doors parse exception {}", jse.getMessage());
        }
        if (doorState != null) {
            updateChannel(CHANNEL_GROUP_STATUS, DOORS, StringType.valueOf(VehicleStatusUtils.checkClosed(doorState)));
            updateDoors(doorState);
        }
        Windows windowState = null;
        try {
            windowState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Windows.class);
        } catch (JsonSyntaxException jse) {
            logger.debug("Windows parse exception {}", jse.getMessage());
        }
        if (windowState != null) {
            updateChannel(CHANNEL_GROUP_STATUS, WINDOWS,
                    StringType.valueOf(VehicleStatusUtils.checkClosed(windowState)));
            updateWindows(windowState);
        }

        // Range values
        // based on unit of length decide if range shall be reported in km or miles
        double totalRange = 0;
        double maxTotalRange = 0;
        if (isElectric) {
            totalRange += vStatus.remainingRangeElectric;
            QuantityType<Length> qtElectricRange = QuantityType.valueOf(vStatus.remainingRangeElectric,
                    Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtElectricRadius = QuantityType
                    .valueOf(Converter.guessRangeRadius(vStatus.remainingRangeElectric), Constants.KILOMETRE_UNIT);

            updateChannel(CHANNEL_GROUP_RANGE, RANGE_ELECTRIC,
                    imperial ? Converter.getMiles(qtElectricRange) : qtElectricRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC,
                    imperial ? Converter.getMiles(qtElectricRadius) : qtElectricRadius);

            maxTotalRange += vStatus.maxRangeElectric;
            QuantityType<Length> qtMaxElectricRange = QuantityType.valueOf(vStatus.maxRangeElectric,
                    Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtMaxElectricRadius = QuantityType
                    .valueOf(Converter.guessRangeRadius(vStatus.maxRangeElectric), Constants.KILOMETRE_UNIT);

            updateChannel(CHANNEL_GROUP_RANGE, RANGE_ELECTRIC_MAX,
                    imperial ? Converter.getMiles(qtMaxElectricRange) : qtMaxElectricRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC_MAX,
                    imperial ? Converter.getMiles(qtMaxElectricRadius) : qtMaxElectricRadius);
        }
        if (hasFuel) {
            totalRange += vStatus.remainingRangeFuel;
            maxTotalRange += vStatus.remainingRangeFuel;
            QuantityType<Length> qtFuelRange = QuantityType.valueOf(vStatus.remainingRangeFuel,
                    Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtFuelRadius = QuantityType
                    .valueOf(Converter.guessRangeRadius(vStatus.remainingRangeFuel), Constants.KILOMETRE_UNIT);

            updateChannel(CHANNEL_GROUP_RANGE, RANGE_FUEL, imperial ? Converter.getMiles(qtFuelRange) : qtFuelRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL,
                    imperial ? Converter.getMiles(qtFuelRadius) : qtFuelRadius);
        }
        if (isHybrid) {
            QuantityType<Length> qtHybridRange = QuantityType.valueOf(totalRange, Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtHybridRadius = QuantityType.valueOf(Converter.guessRangeRadius(totalRange),
                    Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtMaxHybridRange = QuantityType.valueOf(maxTotalRange, Constants.KILOMETRE_UNIT);
            QuantityType<Length> qtMaxHybridRadius = QuantityType.valueOf(Converter.guessRangeRadius(maxTotalRange),
                    Constants.KILOMETRE_UNIT);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_HYBRID,
                    imperial ? Converter.getMiles(qtHybridRange) : qtHybridRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID,
                    imperial ? Converter.getMiles(qtHybridRadius) : qtHybridRadius);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_HYBRID_MAX,
                    imperial ? Converter.getMiles(qtMaxHybridRange) : qtMaxHybridRange);
            updateChannel(CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID_MAX,
                    imperial ? Converter.getMiles(qtMaxHybridRadius) : qtMaxHybridRadius);
        }

        updateChannel(CHANNEL_GROUP_RANGE, MILEAGE,
                QuantityType.valueOf(vStatus.mileage, imperial ? ImperialUnits.MILE : Constants.KILOMETRE_UNIT));
        if (isElectric) {
            updateChannel(CHANNEL_GROUP_RANGE, SOC, QuantityType.valueOf(vStatus.chargingLevelHv, Units.PERCENT));
        }
        if (hasFuel) {
            updateChannel(CHANNEL_GROUP_RANGE, REMAINING_FUEL,
                    QuantityType.valueOf(vStatus.remainingFuel, Units.LITRE));
        }

        // Charge Values
        if (isElectric) {
            if (vStatus.connectionStatus != null) {
                updateChannel(CHANNEL_GROUP_STATUS, PLUG_CONNECTION,
                        StringType.valueOf(Converter.toTitleCase(vStatus.connectionStatus)));
            } else {
                updateChannel(CHANNEL_GROUP_STATUS, PLUG_CONNECTION, UnDefType.NULL);
            }
            if (vStatus.chargingStatus != null) {
                if (Constants.INVALID.equals(vStatus.chargingStatus)) {
                    updateChannel(CHANNEL_GROUP_STATUS, CHARGE_STATUS,
                            StringType.valueOf(Converter.toTitleCase(vStatus.lastChargingEndReason)));
                } else {
                    // State INVALID is somehow misleading. Instead show the Last Charging End Reason
                    updateChannel(CHANNEL_GROUP_STATUS, CHARGE_STATUS,
                            StringType.valueOf(Converter.toTitleCase(vStatus.chargingStatus)));
                }
            } else {
                updateChannel(CHANNEL_GROUP_STATUS, CHARGE_STATUS, UnDefType.NULL);
            }
            if (vStatus.chargingTimeRemaining != null) {
                try {
                    updateChannel(CHANNEL_GROUP_STATUS, CHARGE_REMAINING,
                            QuantityType.valueOf(vStatus.chargingTimeRemaining, Units.MINUTE));
                } catch (NumberFormatException nfe) {
                    updateChannel(CHANNEL_GROUP_STATUS, CHARGE_REMAINING, UnDefType.UNDEF);
                }
            } else {
                updateChannel(CHANNEL_GROUP_STATUS, CHARGE_REMAINING, UnDefType.NULL);
            }
        }
    }
}
