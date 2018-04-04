/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import static org.openhab.binding.icloud.BindingConstants.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
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
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.configuration.DeviceThingConfiguration;
import org.openhab.binding.icloud.internal.json.DeviceInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles updates of an icloud device thing.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Hans-Jörg Merk
 */
public class ICloudDeviceHandler extends BaseThingHandler implements ICloudDeviceInformationListener {
    private final Logger logger = LoggerFactory.getLogger(ICloudDeviceHandler.class);
    private String deviceId;
    private ICloudAccountBridgeHandler bridge;

    public ICloudDeviceHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void deviceInformationUpdate(List<DeviceInformation> deviceInformationList) {
        DeviceInformation deviceInformationRecord = getDeviceInformationRecord(deviceInformationList);
        if (deviceInformationRecord != null) {
            deviceId = deviceInformationRecord.getId();

            if (deviceInformationRecord.getDeviceStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            updateState(BATTERY_STATUS, new StringType(deviceInformationRecord.getBatteryStatus()));

            Double batteryLevel = deviceInformationRecord.getBatteryLevel();
            if (batteryLevel != Double.NaN) {
                updateState(BATTERY_LEVEL, new DecimalType(deviceInformationRecord.getBatteryLevel() * 100));
            }

            if (deviceInformationRecord.getLocation() != null) {
                updateLocationRelatedStates(deviceInformationRecord);
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
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        String channelId = channelUID.getId();
        if (channelId.equals(FIND_MY_PHONE)) {
            if (command == OnOffType.ON) {
                try {
                    bridge.findMyDevice(deviceId);
                } catch (IOException e) {
                    logger.warn("Unable to execute find my device request", e);
                }
                updateState(FIND_MY_PHONE, OnOffType.OFF);
            }
        }

        if (command instanceof RefreshType) {
            bridge.refreshData();
        }
    }

    @Override
    public void dispose() {
        bridge.unregisterListener(this);
        super.dispose();
    }

    private void updateLocationRelatedStates(DeviceInformation deviceInformationRecord) {
        DecimalType latitude = new DecimalType(deviceInformationRecord.getLocation().getLatitude());
        DecimalType longitude = new DecimalType(deviceInformationRecord.getLocation().getLongitude());
        DecimalType accuracy = new DecimalType(deviceInformationRecord.getLocation().getHorizontalAccuracy());

        PointType location = new PointType(latitude, longitude);

        updateState(LOCATION, location);
        updateState(LOCATION_ACCURACY, accuracy);
        updateState(LOCATION_LASTUPDATE, getLastLocationUpdateDateTimeState(deviceInformationRecord));
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing [{}]; bridge status: [{}]", getThing().getUID(), bridgeStatus);

        DeviceThingConfiguration configuration = getConfigAs(DeviceThingConfiguration.class);
        this.deviceId = configuration.deviceId;

        bridge = (ICloudAccountBridgeHandler) getBridge().getHandler();

        if (bridge != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                bridge.registerListener(this);
                bridge.refreshData();
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private DeviceInformation getDeviceInformationRecord(List<DeviceInformation> deviceInformationList) {
        logger.debug("Device: [{}]", deviceId);

        for (DeviceInformation deviceInformationRecord : deviceInformationList) {
            String currentId = deviceInformationRecord.getId();

            logger.debug("Current data element: [{}]", currentId);

            if (deviceId.equalsIgnoreCase(currentId)) {
                return deviceInformationRecord;
            }
        }

        logger.debug("Unable to find device data.");
        return null;
    }

    private State getLastLocationUpdateDateTimeState(DeviceInformation deviceInformationRecord) {
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
