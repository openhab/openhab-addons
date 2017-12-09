/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import static org.openhab.binding.icloud.BindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.icloud.internal.Address;
import org.openhab.binding.icloud.internal.configuration.DeviceThingConfiguration;
import org.openhab.binding.icloud.internal.json.icloud.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles updates of an icloud device thing.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Hans-Jörg Merk
 */
public class DeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);
    private String deviceId;
    private BridgeHandler bridge;
    private LocationProvider locationProvider;

    public DeviceHandler(@NonNull Thing thing, LocationProvider locationProvider) {
        super(thing);
        this.locationProvider = locationProvider;
    }

    public void update(ArrayList<Content> deviceInformationList) {
        Content deviceInformationRecord = getDeviceInformationRecord(deviceInformationList);
        if (deviceInformationRecord != null) {
            deviceId = deviceInformationRecord.getId();
            updateStatus(ThingStatus.ONLINE);

            updateState(BATTERY_STATUS, new StringType(deviceInformationRecord.getBatteryStatus()));

            Double batteryLevel = deviceInformationRecord.getBatteryLevel();
            if (batteryLevel != Double.NaN) {
                updateState(BATTERY_LEVEL, new DecimalType(deviceInformationRecord.getBatteryLevel() * 100));
            }

            if (deviceInformationRecord.getLocation() != null) {
                updateLocationRelatedStates(deviceInformationRecord);
            }
            updateState(DEVICE_NAME, new StringType(deviceInformationRecord.getName()));
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing iCloud device handler.");
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (channelId.equals(FIND_MY_PHONE)) {
            if (command == OnOffType.ON) {
                bridge.findMyDevice(deviceId);
                updateState(FIND_MY_PHONE, OnOffType.OFF);
            }
        }
    }

    @Override
    public void dispose() {
        bridge.unregisterDevice(this);
        super.dispose();
    }

    private void updateLocationRelatedStates(Content deviceInformationRecord) {
        DecimalType latitude = new DecimalType(deviceInformationRecord.getLocation().getLatitude());
        DecimalType longitude = new DecimalType(deviceInformationRecord.getLocation().getLongitude());
        DecimalType accuracy = new DecimalType(deviceInformationRecord.getLocation().getHorizontalAccuracy());

        PointType location = new PointType(latitude, longitude);

        updateState(LOCATION, location);
        updateState(LOCATION_ACCURACY, accuracy);
        updateState(LOCATION_LASTUPDATE, getLastLocationUpdateDateTimeState(deviceInformationRecord));

        updateAddressStates(location);

        if (locationProvider != null) {
            PointType homeLocation = locationProvider.getLocation();
            if (homeLocation != null) {
                DecimalType distanceFromHome = homeLocation.distanceFrom(location);

                updateState(DISTANCE_FROM_HOME, distanceFromHome);
            }
        }
    }

    private void updateAddressStates(PointType location) {
        State streetState = UnDefType.UNDEF;
        State cityState = UnDefType.UNDEF;
        State countryState = UnDefType.UNDEF;
        State formattedAddressState = UnDefType.UNDEF;

        Address address = null;
        address = bridge.getAddress(location);
        if (address != null) {
            streetState = (address.street != null) ? new StringType(address.street) : UnDefType.UNDEF;
            cityState = (address.city != null) ? new StringType(address.city) : UnDefType.UNDEF;
            countryState = (address.country != null) ? new StringType(address.country) : UnDefType.UNDEF;
            formattedAddressState = (address.formattedAddress != null) ? new StringType(address.formattedAddress)
                    : UnDefType.UNDEF;
        }

        updateState(ADDRESS_STREET, streetState);
        updateState(ADDRESS_CITY, cityState);
        updateState(ADDRESS_COUNTRY, countryState);
        updateState(ADDRESS_HUMAN_READABLE, formattedAddressState);
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing [{}]; bridge status: [{}]", getThing().getUID(), bridgeStatus);

        DeviceThingConfiguration configuration = getConfigAs(DeviceThingConfiguration.class);
        this.deviceId = configuration.deviceId;

        bridge = (BridgeHandler) getBridge().getHandler();

        if (bridge != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                bridge.registerDevice(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private Content getDeviceInformationRecord(ArrayList<Content> deviceInformationList) {
        logger.debug("Device: [{}]", deviceId);

        for (Content deviceInformationRecord : deviceInformationList) {
            String currentId = deviceInformationRecord.getId();

            logger.debug("Current data element: [{}]", currentId);

            if (deviceId.equalsIgnoreCase(currentId)) {
                return deviceInformationRecord;
            }
        }

        logger.debug("Unable to find device data.");
        return null;
    }

    private State getLastLocationUpdateDateTimeState(Content deviceInformationRecord) {
        State dateTime = UnDefType.UNDEF;

        if (deviceInformationRecord.getLocation().getTimeStamp() > 0) {
            Date javaDate = new Date(deviceInformationRecord.getLocation().getTimeStamp());
            SimpleDateFormat javaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            String lastUpdate = javaDateFormat.format(javaDate);

            dateTime = new DateTimeType(lastUpdate);
        }

        return dateTime;
    }

}
