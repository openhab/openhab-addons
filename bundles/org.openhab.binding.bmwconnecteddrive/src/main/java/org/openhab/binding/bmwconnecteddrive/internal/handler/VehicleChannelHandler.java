/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.VehicleStatusUtils;
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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
@NonNullByDefault
public class VehicleChannelHandler extends BaseThingHandler {
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

    // Vehicle Status Channels
    protected ChannelUID doors;
    protected ChannelUID windows;
    protected ChannelUID lock;
    protected ChannelUID serviceNextDate;
    protected ChannelUID serviceNextMileage;
    protected ChannelUID checkControl;
    protected ChannelUID lastUpdate;

    protected ChannelUID serviceDate;
    protected ChannelUID serviceMileage;
    protected ChannelUID serviceName;

    protected ChannelUID checkControlMileage;
    protected ChannelUID checkControlName;

    protected ChannelUID doorDriverFront;
    protected ChannelUID doorDriverRear;
    protected ChannelUID doorPassengerFront;
    protected ChannelUID doorPassengerRear;
    protected ChannelUID doorHood;
    protected ChannelUID doorTrunk;

    protected ChannelUID windowDriverFront;
    protected ChannelUID windowDriverRear;
    protected ChannelUID windowPassengerFront;
    protected ChannelUID windowPassengerRear;
    protected ChannelUID windowRear;
    protected ChannelUID windowSunroof;

    // Range channels
    protected ChannelUID mileage;
    protected ChannelUID remainingRangeHybrid;
    protected ChannelUID remainingRangeElectric;
    protected ChannelUID remainingSoc;
    protected ChannelUID remainingRangeFuel;
    protected ChannelUID remainingFuel;
    protected ChannelUID rangeRadiusElectric;
    protected ChannelUID rangeRadiusFuel;
    protected ChannelUID rangeRadiusHybrid;

    // Lifetime Efficiency Channels
    protected ChannelUID lifeTimeAverageConsumption;
    protected ChannelUID lifetimeAvgCombinedConsumption;
    protected ChannelUID lifeTimeAverageRecuperation;
    protected ChannelUID lifeTimeTotalDrivenDistance;
    protected ChannelUID lifeTimeSingleLongestDistance;

    // Last Trip Channels
    protected ChannelUID tripDateTime;
    protected ChannelUID tripDuration;
    protected ChannelUID tripDistance;
    protected ChannelUID tripDistanceSinceCharging;
    protected ChannelUID tripAvgConsumption;
    protected ChannelUID tripAvgCombinedConsumption;
    protected ChannelUID tripAvgRecuperation;

    // Location Channels
    protected ChannelUID gpsLocation;
    protected ChannelUID heading;

    // Remote Services
    protected ChannelUID remoteCommandChannel;
    protected ChannelUID remoteStateChannel;

    // Remote Services
    protected ChannelUID destinationName;
    protected ChannelUID destinationLocation;

    // Charging
    protected ChannelUID chargingStatus;
    protected ChannelUID chargingTimeRemaining;
    protected ChannelUID chargeProfileClimate;
    protected ChannelUID chargeProfileChargeMode;
    protected ChannelUID chargeProfilePreference;
    protected Map<ProfileKey, TimedChannels> timedChannels = new HashMap<>();

    private class TimedChannels {
        TimedChannels(final ChannelUID time, final ChannelUID hour, final ChannelUID minute) {
            this.time = time;
            this.hour = hour;
            this.minute = minute;
        }

        final ChannelUID time;
        final ChannelUID hour;
        final ChannelUID minute;
    }

    private class TimerChannels extends TimedChannels {
        TimerChannels(final ChannelUID time, final ChannelUID hour, final ChannelUID minute, final ChannelUID enabled) {
            super(time, hour, minute);
            this.enabled = enabled;
        }

        final ChannelUID enabled;
    }

    private class TimerDaysChannels extends TimerChannels {
        TimerDaysChannels(final ChannelUID time, final ChannelUID hour, final ChannelUID minute,
                final ChannelUID enabled, final ChannelUID days, final ChannelUID mon, final ChannelUID tue,
                final ChannelUID wed, final ChannelUID thu, final ChannelUID fri, final ChannelUID sat,
                final ChannelUID sun) {
            super(time, hour, minute, enabled);
            this.days = days;
            this.mon = mon;
            this.tue = tue;
            this.wed = wed;
            this.thu = thu;
            this.fri = fri;
            this.sat = sat;
            this.sun = sun;
        }

        final ChannelUID days;
        final ChannelUID mon;
        final ChannelUID tue;
        final ChannelUID wed;
        final ChannelUID thu;
        final ChannelUID fri;
        final ChannelUID sat;
        final ChannelUID sun;
    }

    // Image
    protected ChannelUID imageChannel;
    protected ChannelUID imageViewportChannel;
    protected ChannelUID imageSizeChannel;

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

        // Vehicle Status channels
        doors = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, DOORS);
        windows = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, WINDOWS);
        lock = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LOCK);
        serviceNextDate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE_DATE);
        serviceNextMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE_MILEAGE);
        checkControl = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHECK_CONTROL);
        chargingStatus = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHARGE_STATUS);
        chargingTimeRemaining = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHARGE_REMAINING);
        lastUpdate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LAST_UPDATE);

        serviceDate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, DATE);
        serviceMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, MILEAGE);
        serviceName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, NAME);

        checkControlMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, MILEAGE);
        checkControlName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, NAME);

        doorDriverFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_DRIVER_FRONT);
        doorDriverRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_DRIVER_REAR);
        doorPassengerFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_PASSENGER_FRONT);
        doorPassengerRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_PASSENGER_REAR);
        doorHood = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, HOOD);
        doorTrunk = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, TRUNK);

        windowDriverFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_FORNT);
        windowDriverRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_REAR);
        windowPassengerFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_FRONT);
        windowPassengerRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_REAR);
        windowRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_REAR);
        windowSunroof = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, SUNROOF);

        // range Channels
        mileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, MILEAGE);
        remainingRangeHybrid = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_HYBRID);
        remainingRangeElectric = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_ELECTRIC);
        remainingRangeFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_FUEL);
        remainingSoc = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, SOC);
        remainingFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_FUEL);
        rangeRadiusElectric = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC);
        rangeRadiusFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL);
        rangeRadiusHybrid = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID);

        // Last Trip Channels
        tripDateTime = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DATE);
        tripDuration = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DURATION);
        tripDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE);
        tripDistanceSinceCharging = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE_SINCE_CHARGING);
        tripAvgConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_CONSUMPTION);
        tripAvgCombinedConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_COMBINED_CONSUMPTION);
        tripAvgRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_RECUPERATION);

        // Lifetime Channels
        lifeTimeAverageConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_CONSUMPTION);
        lifetimeAvgCombinedConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME,
                AVG_COMBINED_CONSUMPTION);
        lifeTimeAverageRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_RECUPERATION);
        lifeTimeTotalDrivenDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, TOTAL_DRIVEN_DISTANCE);
        lifeTimeSingleLongestDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, SINGLE_LONGEST_DISTANCE);

        // Location Channels
        gpsLocation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, GPS);
        heading = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, HEADING);

        // Charge Channels
        chargeProfileClimate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_CLIMATE);
        chargeProfileChargeMode = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_MODE);
        chargeProfilePreference = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_PREFERENCE);
        timedChannels.put(ProfileKey.WINDOWSTART,
                new TimedChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_START),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_START_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_START_MINUTE)));
        timedChannels.put(ProfileKey.WINDOWEND,
                new TimedChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_END),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_END_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_END_MINUTE)));
        timedChannels.put(ProfileKey.TIMER1,
                new TimerDaysChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DEPARTURE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DEPARTURE_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DEPARTURE_MINUTE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_ENABLED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAYS),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_MON),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_TUE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_WED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_THU),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_FRI),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_SAT),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAY_SUN)));
        timedChannels.put(ProfileKey.TIMER2,
                new TimerDaysChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DEPARTURE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DEPARTURE_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DEPARTURE_MINUTE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_ENABLED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAYS),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_MON),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_TUE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_WED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_THU),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_FRI),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_SAT),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAY_SUN)));
        timedChannels.put(ProfileKey.TIMER3,
                new TimerDaysChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DEPARTURE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DEPARTURE_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DEPARTURE_MINUTE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_ENABLED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAYS),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_MON),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_TUE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_WED),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_THU),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_FRI),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_SAT),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAY_SUN)));
        timedChannels.put(ProfileKey.OVERRIDE,
                new TimerChannels(new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_OVERRIDE_DEPARTURE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_OVERRIDE_DEPARTURE_HOUR),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_OVERRIDE_DEPARTURE_MINUTE),
                        new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_OVERRIDE_ENABLED)));

        remoteCommandChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_COMMAND);
        remoteStateChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_STATE);

        destinationName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, NAME);
        destinationLocation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, GPS);

        imageChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_FORMAT);
        imageViewportChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT);
        imageSizeChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_SIZE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
    }

    protected void updateCheckControls(List<CCMMessage> ccl) {
        if (ccl.size() == 0) {
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
        List<StateOption> ccmMileageOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CCMMessage ccEntry : checkControlList) {
            ccmDescriptionOptions.add(new StateOption(Integer.toString(index), ccEntry.ccmDescriptionShort));
            ccmMileageOptions.add(new StateOption(Integer.toString(index), Integer.toString(ccEntry.ccmMileage)));
            if (selectedCC.equals(ccEntry.ccmDescriptionShort)) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(checkControlName, ccmDescriptionOptions);
        setOptions(checkControlMileage, ccmMileageOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectCheckControl(0);
        }
    }

    protected void selectCheckControl(int index) {
        if (index >= 0 && index < checkControlList.size()) {
            CCMMessage ccEntry = checkControlList.get(index);
            selectedCC = ccEntry.ccmDescriptionShort;
            updateState(checkControlName, StringType.valueOf(ccEntry.ccmDescriptionShort));
            QuantityType<Length> qtLength = QuantityType.valueOf(Converter.round(ccEntry.ccmMileage),
                    MetricPrefix.KILO(SIUnits.METRE));
            if (imperial) {
                updateState(checkControlMileage,
                        QuantityType.valueOf(Converter.round(ccEntry.ccmMileage), ImperialUnits.MILE));
            } else {
                updateState(checkControlMileage, qtLength);
            }
        }
    }

    protected void updateServices(List<CBSMessage> sl) {
        // if list is empty add "undefined" element
        if (sl.size() == 0) {
            CBSMessage cbsm = new CBSMessage();
            cbsm.cbsType = Constants.NO_ENTRIES;
            sl.add(cbsm);
        }

        // add all elements to options
        serviceList = sl;
        List<StateOption> serviceNameOptions = new ArrayList<>();
        List<StateOption> serviceDateOptions = new ArrayList<>();
        List<StateOption> serviceMileageOptions = new ArrayList<>();
        boolean isSelectedElementIn = false;
        int index = 0;
        for (CBSMessage serviceEntry : serviceList) {
            // create StateOption with "value = list index" and "label = human readable string"
            serviceNameOptions.add(new StateOption(Integer.toString(index), serviceEntry.getType()));
            serviceDateOptions.add(new StateOption(Integer.toString(index), serviceEntry.getDueDate()));
            serviceMileageOptions
                    .add(new StateOption(Integer.toString(index), Integer.toString(serviceEntry.cbsRemainingMileage)));
            if (selectedService.equals(serviceEntry.getType())) {
                isSelectedElementIn = true;
            }
            index++;
        }
        setOptions(serviceName, serviceNameOptions);
        setOptions(serviceDate, serviceDateOptions);
        setOptions(serviceMileage, serviceMileageOptions);

        // if current selected item isn't anymore in the list select first entry
        if (!isSelectedElementIn) {
            selectService(0);
        }
    }

    protected void selectService(int index) {
        if (index >= 0 && index < serviceList.size()) {
            CBSMessage serviceEntry = serviceList.get(index);
            selectedService = serviceEntry.cbsType;
            updateState(serviceName, StringType.valueOf(Converter.toTitleCase(serviceEntry.getType())));
            updateState(serviceDate, DateTimeType.valueOf(Converter.getLocalDateTime(serviceEntry.getDueDate())));
            if (imperial) {
                updateState(serviceMileage,
                        QuantityType.valueOf(Converter.round(serviceEntry.cbsRemainingMileage), ImperialUnits.MILE));
            } else {
                updateState(serviceMileage, QuantityType.valueOf(Converter.round(serviceEntry.cbsRemainingMileage),
                        MetricPrefix.KILO(SIUnits.METRE)));
            }
        }
    }

    protected void updateDestinations(List<Destination> dl) {
        // if list is empty add "undefined" element
        if (dl.size() == 0) {
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
        setOptions(destinationName, destinationNameOptions);
        setOptions(destinationLocation, destinationGPSOptions);

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
            updateState(destinationName, StringType.valueOf(destinationEntry.getAddress()));
            updateState(destinationLocation, PointType.valueOf(destinationEntry.getCoordinates()));
        }
    }

    private void setOptions(ChannelUID cuid, List<StateOption> options) {
        optionProvider.setStateOptions(cuid, options);
    }

    protected void updateAllTrips(AllTrips allTrips) {
        QuantityType<Length> qtTotalElectric = QuantityType
                .valueOf(Converter.round(allTrips.totalElectricDistance.userTotal), MetricPrefix.KILO(SIUnits.METRE));
        QuantityType<Length> qtLongestElectricRange = QuantityType
                .valueOf(Converter.round(allTrips.chargecycleRange.userHigh), MetricPrefix.KILO(SIUnits.METRE));
        QuantityType<Length> qtDistanceSinceCharge = QuantityType.valueOf(
                Converter.round(allTrips.chargecycleRange.userCurrentChargeCycle), MetricPrefix.KILO(SIUnits.METRE));
        double avgConsumotion = allTrips.avgElectricConsumption.userAverage;
        double avgCombinedConsumption = allTrips.avgCombinedConsumption.userAverage;
        double avgRecuperation = allTrips.avgRecuperation.userAverage;
        if (imperial) {
            updateState(lifeTimeTotalDrivenDistance, Converter.getMiles(qtTotalElectric));
            updateState(lifeTimeSingleLongestDistance, Converter.getMiles(qtLongestElectricRange));
            updateState(tripDistanceSinceCharging, Converter.getMiles(qtDistanceSinceCharge));

            // Conversion from kwh/100km to kwh/10mi has to be done manually
            avgConsumotion *= Converter.MILES_TO_KM_RATIO;
            avgCombinedConsumption *= Converter.MILES_TO_KM_RATIO;
            avgRecuperation *= Converter.MILES_TO_KM_RATIO;
        } else {
            updateState(lifeTimeTotalDrivenDistance, qtTotalElectric);
            updateState(lifeTimeSingleLongestDistance, qtLongestElectricRange);
            updateState(tripDistanceSinceCharging, qtDistanceSinceCharge);
        }
        updateState(lifeTimeAverageConsumption,
                QuantityType.valueOf(Converter.round(avgConsumotion), Units.KILOWATT_HOUR));
        updateState(lifetimeAvgCombinedConsumption,
                QuantityType.valueOf(Converter.round(avgCombinedConsumption), Units.LITRE));
        updateState(lifeTimeAverageRecuperation,
                QuantityType.valueOf(Converter.round(avgRecuperation), Units.KILOWATT_HOUR));
    }

    protected void updateLastTrip(LastTrip trip) {
        // Whyever the Last Trip DateTime is delivered without offest - so LocalTime
        updateState(tripDateTime, DateTimeType.valueOf(Converter.getLocalDateTimeWithoutOffest(trip.date)));
        updateState(tripDuration, QuantityType.valueOf(trip.duration, Units.MINUTE));
        QuantityType<Length> qtTotalDistance = QuantityType.valueOf(Converter.round(trip.totalDistance),
                MetricPrefix.KILO(SIUnits.METRE));
        double avgConsumtption = trip.avgElectricConsumption;
        double avgCombinedConsumption = trip.avgCombinedConsumption;
        double avgRecuperation = trip.avgRecuperation;
        if (imperial) {
            updateState(tripDistance, Converter.getMiles(qtTotalDistance));

            // Conversion from kwh/100km to kwh/10mi has to be done manually
            avgConsumtption *= Converter.MILES_TO_KM_RATIO;
            avgCombinedConsumption *= Converter.MILES_TO_KM_RATIO;
            avgRecuperation *= Converter.MILES_TO_KM_RATIO;
        } else {
            updateState(tripDistance, qtTotalDistance);
        }
        updateState(tripAvgConsumption, QuantityType.valueOf(Converter.round(avgConsumtption), Units.KILOWATT_HOUR));
        updateState(tripAvgCombinedConsumption,
                QuantityType.valueOf(Converter.round(avgCombinedConsumption), Units.LITRE));
        updateState(tripAvgRecuperation, QuantityType.valueOf(Converter.round(avgRecuperation), Units.KILOWATT_HOUR));
    }

    protected void updateChargeProfileFromContent(String content) {
        ChargeProfileWrapper.fromJson(content).ifPresent(wrapper -> updateChargeProfile(wrapper));
    }

    protected void updateChargeProfile(ChargeProfileWrapper wrapper) {
        updateState(chargeProfilePreference, StringType.valueOf(Converter.toTitleCase(wrapper.getPreference())));
        updateState(chargeProfileChargeMode, StringType.valueOf(Converter.toTitleCase(wrapper.getMode())));
        final Boolean climate = wrapper.isEnabled(ProfileKey.CLIMATE);
        updateState(chargeProfileClimate, climate == null ? UnDefType.UNDEF : OnOffType.from(climate));
        updateTimedState(wrapper, ProfileKey.WINDOWSTART);
        updateTimedState(wrapper, ProfileKey.WINDOWEND);
        updateTimedState(wrapper, ProfileKey.TIMER1);
        updateTimedState(wrapper, ProfileKey.TIMER2);
        updateTimedState(wrapper, ProfileKey.TIMER3);
        updateTimedState(wrapper, ProfileKey.OVERRIDE);
    }

    protected void updateTimedState(ChargeProfileWrapper profile, ProfileKey key) {
        final TimedChannels channels = timedChannels.get(key);
        if (channels != null) {
            final LocalTime time = profile.getTime(key);
            updateState(channels.time, time == null ? UnDefType.UNDEF
                    : new DateTimeType(ZonedDateTime.of(Constants.EPOCH_DAY, time, ZoneId.systemDefault())));
            updateState(channels.hour, time == null ? UnDefType.UNDEF : new DecimalType(time.getHour()));
            updateState(channels.minute, time == null ? UnDefType.UNDEF : new DecimalType(time.getMinute()));
            if (channels instanceof TimerChannels) {
                final Boolean enabled = profile.isEnabled(key);
                updateState(((TimerChannels) channels).enabled,
                        enabled == null ? UnDefType.UNDEF : OnOffType.from(enabled));
                if (channels instanceof TimerDaysChannels) {
                    final List<String> days = profile.getDays(key);
                    final Boolean mon = profile.isDayEnabled(key, Day.MONDAY);
                    final Boolean tue = profile.isDayEnabled(key, Day.TUESDAY);
                    final Boolean wed = profile.isDayEnabled(key, Day.WEDNESDAY);
                    final Boolean thu = profile.isDayEnabled(key, Day.THURSDAY);
                    final Boolean fri = profile.isDayEnabled(key, Day.FRIDAY);
                    final Boolean sat = profile.isDayEnabled(key, Day.SATURDAY);
                    final Boolean sun = profile.isDayEnabled(key, Day.SUNDAY);
                    updateState(((TimerDaysChannels) channels).days,
                            days == null ? UnDefType.UNDEF : StringType.valueOf(ChargeProfileUtils.formatDays(days)));
                    updateState(((TimerDaysChannels) channels).mon,
                            mon == null ? UnDefType.UNDEF : OnOffType.from(mon));
                    updateState(((TimerDaysChannels) channels).tue,
                            tue == null ? UnDefType.UNDEF : OnOffType.from(tue));
                    updateState(((TimerDaysChannels) channels).wed,
                            wed == null ? UnDefType.UNDEF : OnOffType.from(wed));
                    updateState(((TimerDaysChannels) channels).thu,
                            thu == null ? UnDefType.UNDEF : OnOffType.from(thu));
                    updateState(((TimerDaysChannels) channels).fri,
                            fri == null ? UnDefType.UNDEF : OnOffType.from(fri));
                    updateState(((TimerDaysChannels) channels).sat,
                            sat == null ? UnDefType.UNDEF : OnOffType.from(sat));
                    updateState(((TimerDaysChannels) channels).sun,
                            sun == null ? UnDefType.UNDEF : OnOffType.from(sun));
                }
            }
        }
    }

    protected void updateDoors(Doors doorState) {
        updateState(doorDriverFront, StringType.valueOf(Converter.toTitleCase(doorState.doorDriverFront)));
        updateState(doorDriverRear, StringType.valueOf(Converter.toTitleCase(doorState.doorDriverRear)));
        updateState(doorPassengerFront, StringType.valueOf(Converter.toTitleCase(doorState.doorPassengerFront)));
        updateState(doorPassengerRear, StringType.valueOf(Converter.toTitleCase(doorState.doorPassengerRear)));
        updateState(doorTrunk, StringType.valueOf(Converter.toTitleCase(doorState.trunk)));
        updateState(doorHood, StringType.valueOf(Converter.toTitleCase(doorState.hood)));
    }

    protected void updateWindows(Windows windowState) {
        updateState(windowDriverFront, StringType.valueOf(Converter.toTitleCase(windowState.windowDriverFront)));
        updateState(windowDriverRear, StringType.valueOf(Converter.toTitleCase(windowState.windowDriverRear)));
        updateState(windowPassengerFront, StringType.valueOf(Converter.toTitleCase(windowState.windowPassengerFront)));
        updateState(windowPassengerRear, StringType.valueOf(Converter.toTitleCase(windowState.windowPassengerRear)));
        updateState(windowRear, StringType.valueOf(Converter.toTitleCase(windowState.rearWindow)));
        updateState(windowSunroof, StringType.valueOf(Converter.toTitleCase(windowState.sunroof)));
    }

    protected void updatePosition(Position pos) {
        updateState(gpsLocation, PointType.valueOf(pos.getCoordinates()));
        updateState(heading, QuantityType.valueOf(pos.heading, Units.DEGREE_ANGLE));
    }

    protected void updateVehicleStatus(VehicleStatus vStatus) {
        // Vehicle Status
        updateState(lock, StringType.valueOf(Converter.toTitleCase(vStatus.doorLockState)));

        // Service Updates
        String nextServiceDate = VehicleStatusUtils.getNextServiceDate(vStatus);
        updateState(serviceNextDate, DateTimeType.valueOf(Converter.getLocalDateTime(nextServiceDate)));
        double nextServiceMileage = VehicleStatusUtils.getNextServiceMileage(vStatus);
        if (imperial) {
            updateState(serviceNextMileage,
                    QuantityType.valueOf(Converter.round(nextServiceMileage), ImperialUnits.MILE));
        } else {
            updateState(serviceNextMileage,
                    QuantityType.valueOf(Converter.round(nextServiceMileage), MetricPrefix.KILO(SIUnits.METRE)));
        }
        // CheckControl Active?
        updateState(checkControl,
                StringType.valueOf(Converter.toTitleCase(VehicleStatusUtils.checkControlActive(vStatus))));
        // last update Time
        updateState(lastUpdate,
                DateTimeType.valueOf(Converter.getLocalDateTime(VehicleStatusUtils.getUpdateTime(vStatus))));

        Doors doorState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Doors.class);
        Windows windowState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Windows.class);
        if (doorState != null) {
            updateState(doors, StringType.valueOf(VehicleStatusUtils.checkClosed(doorState)));
            updateDoors(doorState);
        }
        if (windowState != null) {
            updateState(windows, StringType.valueOf(VehicleStatusUtils.checkClosed(windowState)));
            updateWindows(windowState);
        }

        // Range values
        // based on unit of length decide if range shall be reported in km or miles
        float totalRange = 0;
        if (isElectric) {
            totalRange += vStatus.remainingRangeElectric;
            QuantityType<Length> qtElectricRange = QuantityType.valueOf(vStatus.remainingRangeElectric,
                    MetricPrefix.KILO(SIUnits.METRE));
            QuantityType<Length> qtElectricRadius = QuantityType.valueOf(
                    Converter.guessRangeRadius(vStatus.remainingRangeElectric), MetricPrefix.KILO(SIUnits.METRE));
            if (imperial) {
                updateState(remainingRangeElectric, Converter.getMiles(qtElectricRange));
                updateState(rangeRadiusElectric, Converter.getMiles(qtElectricRadius));
            } else {
                updateState(remainingRangeElectric, qtElectricRange);
                updateState(rangeRadiusElectric, qtElectricRadius);
            }
        }
        if (hasFuel) {
            totalRange += vStatus.remainingRangeFuel;
            QuantityType<Length> qtFuealRange = QuantityType.valueOf(vStatus.remainingRangeFuel,
                    MetricPrefix.KILO(SIUnits.METRE));
            QuantityType<Length> qtFuelRadius = QuantityType
                    .valueOf(Converter.guessRangeRadius(vStatus.remainingRangeFuel), MetricPrefix.KILO(SIUnits.METRE));
            if (imperial) {
                updateState(remainingRangeFuel, Converter.getMiles(qtFuealRange));
                updateState(rangeRadiusFuel, Converter.getMiles(qtFuelRadius));
            } else {
                updateState(remainingRangeFuel, qtFuealRange);
                updateState(rangeRadiusFuel, qtFuelRadius);
            }
        }
        if (isHybrid) {
            QuantityType<Length> qtHybridRange = QuantityType.valueOf(totalRange, MetricPrefix.KILO(SIUnits.METRE));
            QuantityType<Length> qtHybridRadius = QuantityType.valueOf(Converter.guessRangeRadius(totalRange),
                    MetricPrefix.KILO(SIUnits.METRE));
            if (imperial) {
                updateState(remainingRangeHybrid, Converter.getMiles(qtHybridRange));
                updateState(rangeRadiusHybrid, Converter.getMiles(qtHybridRadius));
            } else {
                updateState(remainingRangeHybrid, qtHybridRange);
                updateState(rangeRadiusHybrid, qtHybridRadius);
            }
        }

        if (imperial) {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, ImperialUnits.MILE));
        } else {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, MetricPrefix.KILO(SIUnits.METRE)));
        }
        if (isElectric) {
            updateState(remainingSoc, QuantityType.valueOf(vStatus.chargingLevelHv, Units.PERCENT));
        }
        if (hasFuel) {
            updateState(remainingFuel, QuantityType.valueOf(vStatus.remainingFuel, Units.LITRE));
        }

        // Charge Values
        if (isElectric) {
            if (vStatus.chargingStatus != null) {
                if (Constants.INVALID.equals(vStatus.chargingStatus)) {
                    updateState(chargingStatus,
                            StringType.valueOf(Converter.toTitleCase(vStatus.lastChargingEndReason)));
                } else {
                    // State INVALID is somehow misleading. Instead show the Last Charging End Reason
                    updateState(chargingStatus, StringType.valueOf(Converter.toTitleCase(vStatus.chargingStatus)));
                }
            } else {
                updateState(chargingStatus, UnDefType.NULL);
            }
            if (vStatus.chargingTimeRemaining != null) {
                try {
                    updateState(chargingTimeRemaining,
                            QuantityType.valueOf(vStatus.chargingTimeRemaining, Units.MINUTE));
                } catch (NumberFormatException nfe) {
                    updateState(chargingTimeRemaining, UnDefType.UNDEF);
                }
            } else {
                updateState(chargingTimeRemaining, UnDefType.NULL);
            }
        }
    }
}
