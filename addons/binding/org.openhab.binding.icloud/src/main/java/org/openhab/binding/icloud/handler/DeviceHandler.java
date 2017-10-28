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
import org.openhab.binding.icloud.internal.json.icloud.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrik Gfeller
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

    public void update(ArrayList<Content> content) {
        Content deviceData = getDeviceData(content);
        if (deviceData != null) {
            deviceId = deviceData.getId();
            updateStatus(ThingStatus.ONLINE);

            updateState(BATTERYSTATUS, new StringType(deviceData.getBatteryStatus()));

            Double batteryLevel = deviceData.getBatteryLevel();
            if ((batteryLevel != null) && (batteryLevel != Double.NaN)) {
                updateState(BATTERYLEVEL, new DecimalType(deviceData.getBatteryLevel() * 100));
            }

            if (deviceData.getLocation() != null) {
                updateLocationRelatedStates(deviceData);
            }
            updateState(DEVICENAME, new StringType(deviceData.getName()));
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
        if (channelId.equals(FINDMYPHONE)) {
            if (command == OnOffType.ON) {
                bridge.pingPhone(deviceId);
                updateState(FINDMYPHONE, OnOffType.OFF);
            }
        }
    }

    @Override
    public void dispose() {
        bridge.unregisterDevice(this);
        super.dispose();
    }

    private void updateLocationRelatedStates(Content deviceData) {
        DecimalType latitude = new DecimalType(deviceData.getLocation().getLatitude());
        DecimalType longitude = new DecimalType(deviceData.getLocation().getLongitude());
        DecimalType accuracy = new DecimalType(deviceData.getLocation().getHorizontalAccuracy());

        if (deviceData.getLocation().getTimeStamp() > 0) {
            Date javaDate = new Date(deviceData.getLocation().getTimeStamp());
            SimpleDateFormat javaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            String lastUpdate = javaDateFormat.format(javaDate);

            updateState(LOCATIONLASTUPDATE, new DateTimeType(lastUpdate));
        }

        PointType location = new PointType(latitude, longitude);

        updateState(LOCATION, location);
        updateState(LOCATIONACCURACY, accuracy);

        updateAddressStates(location);

        if (locationProvider != null) {
            PointType homeLocation = locationProvider.getLocation();
            if (homeLocation != null) {
                DecimalType distanceFromHome = homeLocation.distanceFrom(location);

                updateState(DISTANCEFROMHOME, distanceFromHome);
            }
        }
    }

    private void updateAddressStates(PointType location) {

        State streetState = UnDefType.UNDEF;
        State cityState = UnDefType.UNDEF;
        State countryState = UnDefType.UNDEF;
        State formattedAddressState = UnDefType.UNDEF;

        try {
            Address address = null;
            address = bridge.getAddress(location);
            if (address != null) {
                streetState = (address.Street != null) ? new StringType(address.Street) : UnDefType.UNDEF;
                cityState = (address.City != null) ? new StringType(address.City) : UnDefType.UNDEF;
                countryState = (address.Country != null) ? new StringType(address.Country) : UnDefType.UNDEF;
                formattedAddressState = (address.FormattedAddress != null) ? new StringType(address.FormattedAddress)
                        : UnDefType.UNDEF;
            }
        } catch (Exception e) {
            logException(e);
        }

        updateState(ADDRESSSTREET, streetState);
        updateState(ADDRESSCITY, cityState);
        updateState(ADDRESSCOUNTRY, countryState);
        updateState(FORMATTEDADDRESS, formattedAddressState);
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

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

    private Content getDeviceData(ArrayList<Content> content) {
        String deviceId = thing.getProperties().get(IDPROPERTY);

        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).getId().compareToIgnoreCase(deviceId) == 0) {
                return content.get(i);
            }
        }

        return null;
    }

    private void logException(Exception exception) {
        logger.error("{}", exception.getMessage() + "\n" + exception.getStackTrace().toString());
    }

}
