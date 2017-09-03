/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import static org.openhab.binding.icloud.iCloudBindingConstants.*;

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
import org.openhab.binding.icloud.internal.Address;
import org.openhab.binding.icloud.internal.json.iCloud.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrik Gfeller
 * @author Hans-Jörg Merk
 */
public class iCloudDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(iCloudDeviceHandler.class);
    private int index;
    private String deviceId;
    private iCloudBridgeHandler bridge;
    private LocationProvider locationProvider;

    public iCloudDeviceHandler(@NonNull Thing thing, LocationProvider locationProvider) {
        super(thing);

        String uid = thing.getUID().toString();
        index = Integer.parseInt(uid.substring(uid.lastIndexOf(":") + 1));

        this.locationProvider = locationProvider;
    }

    public void update(ArrayList<Content> content) {
        Content deviceData = getMyContent(content);
        if (deviceData != null) {
            deviceId = deviceData.getId();
            updateStatus(ThingStatus.ONLINE);

            updateState(BATTERYSTATUS, new StringType(deviceData.getBatteryStatus()));

            Double batteryLevel = deviceData.getBatteryLevel();
            if ((batteryLevel != null) && (batteryLevel != Double.NaN)) {

                updateState(BATTERYLEVEL, new DecimalType(deviceData.getBatteryLevel() * 100));
            }

            if (deviceData.getLocation() != null) {
                DecimalType latitude = new DecimalType(deviceData.getLocation().getLatitude());
                DecimalType longitude = new DecimalType(deviceData.getLocation().getLongitude());
                DecimalType accuracy = new DecimalType(deviceData.getLocation().getHorizontalAccuracy());

                if (deviceData.getLocation().getTimeStamp() > 0) {
                    Date javaDate = new Date(deviceData.getLocation().getTimeStamp());
                    SimpleDateFormat javaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
                    String lastUpdate = javaDateFormat.format(javaDate);

                    updateState(LASTUPDATE, new DateTimeType(lastUpdate));
                }

                PointType location = new PointType(latitude, longitude);
                Address address = bridge.getAddress(location);

                updateState(LOCATION, location);
                updateState(LOCATIONACCURACY, accuracy);
                updateState(ADDRESSSTREET, new StringType(address.Street));
                updateState(ADDRESSCITY, new StringType(address.City));
                updateState(ADDRESSCOUNTRY, new StringType(address.Country));
                updateState(FORMATTEDADDRESS, new StringType(address.FormattedAddress));

                if (locationProvider != null) {
                    PointType homeLocation = locationProvider.getLocation();
                    if (homeLocation != null) {
                        DecimalType distanceFromHome = homeLocation.distanceFrom(location);

                        updateState(DISTANCEFROMHOME, distanceFromHome);
                    }
                }
            }
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

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

        bridge = (iCloudBridgeHandler) getBridge().getHandler();

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

    private Content getMyContent(ArrayList<Content> content) {
        return content.get(index);
    }

}
