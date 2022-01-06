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
package org.openhab.binding.icloud.internal.handler;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.configuration.ICloudDeviceThingConfiguration;
import org.openhab.binding.icloud.internal.json.response.ICloudDeviceInformation;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles updates of an iCloud device Thing.
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Hans-Jörg Merk - Helped with testing and feedback
 * @author Gaël L'hopital - Added low battery
 *
 */
@NonNullByDefault
public class ICloudDeviceHandler extends BaseThingHandler implements ICloudDeviceInformationListener {
    private final Logger logger = LoggerFactory.getLogger(ICloudDeviceHandler.class);
    private @Nullable String deviceId;
    private @Nullable ICloudAccountBridgeHandler icloudAccount;

    public ICloudDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void deviceInformationUpdate(List<ICloudDeviceInformation> deviceInformationList) {
        ICloudDeviceInformation deviceInformationRecord = getDeviceInformationRecord(deviceInformationList);
        if (deviceInformationRecord != null) {
            if (deviceInformationRecord.getDeviceStatus() == 200) {
                updateStatus(ONLINE);
            } else {
                updateStatus(OFFLINE, COMMUNICATION_ERROR, "Reported offline by iCloud webservice");
            }

            updateState(BATTERY_STATUS, new StringType(deviceInformationRecord.getBatteryStatus()));

            Double batteryLevel = deviceInformationRecord.getBatteryLevel();
            if (batteryLevel != Double.NaN) {
                updateState(BATTERY_LEVEL, new DecimalType(deviceInformationRecord.getBatteryLevel() * 100));
                updateState(LOW_BATTERY, batteryLevel < 0.2 ? OnOffType.ON : OnOffType.OFF);
            }

            if (deviceInformationRecord.getLocation() != null) {
                updateLocationRelatedStates(deviceInformationRecord);
            }
        } else {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "The device is not included in the current account");
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        Object bridgeStatus = (bridge == null) ? null : bridge.getStatus();
        logger.debug("initializeThing thing [{}]; bridge status: [{}]", getThing().getUID(), bridgeStatus);

        ICloudDeviceThingConfiguration configuration = getConfigAs(ICloudDeviceThingConfiguration.class);
        this.deviceId = configuration.deviceId;

        ICloudAccountBridgeHandler handler = getIcloudAccount();
        if (handler != null) {
            refreshData();
        } else {
            updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "Bridge not found");
        }
    }

    private void refreshData() {
        ICloudAccountBridgeHandler bridge = getIcloudAccount();
        if (bridge != null) {
            bridge.refreshData();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        ICloudAccountBridgeHandler bridge = getIcloudAccount();
        if (bridge == null) {
            logger.debug("No bridge found, ignoring command");
            return;
        }

        String channelId = channelUID.getId();
        if (channelId.equals(FIND_MY_PHONE)) {
            if (command == OnOffType.ON) {
                try {
                    final String deviceId = this.deviceId;
                    if (deviceId == null) {
                        logger.debug("Can't send Find My Device request, because deviceId is null!");
                        return;
                    }
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
        ICloudAccountBridgeHandler bridge = getIcloudAccount();
        if (bridge != null) {
            bridge.unregisterListener(this);
        }
        super.dispose();
    }

    private void updateLocationRelatedStates(ICloudDeviceInformation deviceInformationRecord) {
        DecimalType latitude = new DecimalType(deviceInformationRecord.getLocation().getLatitude());
        DecimalType longitude = new DecimalType(deviceInformationRecord.getLocation().getLongitude());
        DecimalType altitude = new DecimalType(deviceInformationRecord.getLocation().getAltitude());
        DecimalType accuracy = new DecimalType(deviceInformationRecord.getLocation().getHorizontalAccuracy());

        PointType location = new PointType(latitude, longitude, altitude);

        updateState(LOCATION, location);
        updateState(LOCATION_ACCURACY, accuracy);
        updateState(LOCATION_LASTUPDATE, getLastLocationUpdateDateTimeState(deviceInformationRecord));
    }

    private @Nullable ICloudDeviceInformation getDeviceInformationRecord(
            List<ICloudDeviceInformation> deviceInformationList) {
        logger.debug("Device: [{}]", deviceId);

        for (ICloudDeviceInformation deviceInformationRecord : deviceInformationList) {
            String currentId = deviceInformationRecord.getId();

            logger.debug("Current data element: [id = {}]", currentId);

            if (currentId != null && currentId.equals(deviceId)) {
                return deviceInformationRecord;
            }
        }

        logger.debug("Unable to find device data.");
        return null;
    }

    private State getLastLocationUpdateDateTimeState(ICloudDeviceInformation deviceInformationRecord) {
        State dateTime = UnDefType.UNDEF;

        if (deviceInformationRecord.getLocation().getTimeStamp() > 0) {
            Date date = new Date(deviceInformationRecord.getLocation().getTimeStamp());
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            dateTime = new DateTimeType(zonedDateTime);
        }

        return dateTime;
    }

    private @Nullable ICloudAccountBridgeHandler getIcloudAccount() {
        if (icloudAccount == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ICloudAccountBridgeHandler) {
                icloudAccount = (ICloudAccountBridgeHandler) handler;
                icloudAccount.registerListener(this);
            } else {
                return null;
            }
        }
        return icloudAccount;
    }
}
