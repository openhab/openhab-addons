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
package org.openhab.binding.icloud.internal.handler;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.*;
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
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
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
 * @author Simon Spielmann - Rework for new iCloud API
 * @author Simon Spielmann - Recover status after bridge and communication failures
 *
 */
@NonNullByDefault
public class ICloudDeviceHandler extends BaseThingHandler implements ICloudDeviceInformationListener {
    private final Logger logger = LoggerFactory.getLogger(ICloudDeviceHandler.class);

    private static final int DEVICE_STATUS_ONLINE = 200;

    /**
     * Reported while a requested location update is still in progress. The device is reachable, so it is treated as
     * online; how old the location is can be seen in the last location update channel.
     */
    private static final int DEVICE_STATUS_PENDING = 203;

    private @Nullable String deviceId;

    private volatile @Nullable ICloudDeviceInformation deviceInformationRecord;

    /** Whether the bridge has delivered device data since initialization. */
    private volatile boolean deviceInformationReceived;

    private final Object statusLock = new Object();

    public ICloudDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void deviceInformationUpdate(List<ICloudDeviceInformation> deviceInformationList) {
        ICloudDeviceInformation deviceInformationRecord = getDeviceInformationRecord(deviceInformationList);
        synchronized (statusLock) {
            this.deviceInformationRecord = deviceInformationRecord;
            this.deviceInformationReceived = true;
            updateThingStatus(false);
        }
        if (deviceInformationRecord != null) {
            updateState(BATTERY_STATUS, new StringType(deviceInformationRecord.getBatteryStatus()));

            Double batteryLevel = deviceInformationRecord.getBatteryLevel();
            if (batteryLevel != Double.NaN) {
                updateState(BATTERY_LEVEL, new DecimalType(deviceInformationRecord.getBatteryLevel() * 100));
                updateState(LOW_BATTERY, OnOffType.from(batteryLevel < 0.2));
            }

            if (deviceInformationRecord.getLocation() != null) {
                updateLocationRelatedStates(deviceInformationRecord);
            }
        }
    }

    @Override
    public void initialize() {
        ICloudDeviceThingConfiguration configuration = getConfigAs(ICloudDeviceThingConfiguration.class);
        this.deviceId = configuration.deviceId;
        this.deviceInformationRecord = null;
        this.deviceInformationReceived = false;

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge unavailable while initializing Thing: {}", thing.getUID());
            return;
        }
        if (bridge.getHandler() instanceof ICloudAccountBridgeHandler handler) {
            handler.registerListener(this);
            synchronized (statusLock) {
                updateThingStatus(true);
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                // Fetching the device data requires network access, which must not block initialize().
                scheduler.execute(handler::refreshData);
            }
        } else {
            updateStatus(ThingStatus.UNKNOWN, "Bridge handler is not configured");
        }
    }

    /**
     * openHAB core delivers bridge status notifications asynchronously, so they may arrive out of order. The status
     * is therefore derived from the current bridge status instead of the delivered one.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (statusLock) {
            updateThingStatus(false);
        }
    }

    /**
     * Derives the Thing status from the current bridge status and the last device data received from the bridge.
     * Must be called while holding {@link #statusLock}.
     *
     * @param initializing whether the handler is initializing and has to leave the INITIALIZING status
     */
    private void updateThingStatus(boolean initializing) {
        Bridge bridge = getBridge();
        ThingStatus bridgeStatus = bridge != null ? bridge.getStatus() : ThingStatus.OFFLINE;
        if (bridgeStatus == ThingStatus.ONLINE) {
            ICloudDeviceInformation record = deviceInformationRecord;
            if (!deviceInformationReceived) {
                updateStatus(ThingStatus.UNKNOWN);
            } else if (record == null) {
                updateStatus(CONFIGURATION_ERROR, "The device is not included in the current account");
            } else if (record.getDeviceStatus() == DEVICE_STATUS_ONLINE
                    || record.getDeviceStatus() == DEVICE_STATUS_PENDING) {
                updateStatus(ONLINE);
            } else {
                updateStatus(COMMUNICATION_ERROR,
                        "Reported offline by iCloud webservice (device status " + record.getDeviceStatus() + ")");
            }
        } else if (bridgeStatus == ThingStatus.OFFLINE || initializing) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        }
        // Otherwise the bridge is re-initializing (UNKNOWN). The status is kept until the bridge reports a result.
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        Bridge bridge = getBridge();
        if (bridge == null) {
            this.logger.debug("No bridge found, ignoring command");
            return;
        }

        ICloudAccountBridgeHandler bridgeHandler = (ICloudAccountBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            this.logger.debug("No bridge handler found, ignoring command");
            return;
        }

        String channelId = channelUID.getId();
        if (channelId.equals(FIND_MY_PHONE)) {
            if (command == OnOffType.ON) {
                try {
                    ICloudDeviceInformation localDeviceInformationRecord = deviceInformationRecord;
                    if (localDeviceInformationRecord == null) {
                        this.logger
                                .debug("Can't send Find My Device request, because deviceInformationRecord is null!");
                        return;
                    }
                    String id = localDeviceInformationRecord.getId();
                    if (id == null) {
                        this.logger.debug(
                                "Can't send Find My Device request, because deviceInformationRecord.getId() is null!");
                        return;
                    }
                    bridgeHandler.findMyDevice(id);
                } catch (IOException | InterruptedException e) {
                    this.logger.warn("Unable to execute find my device request", e);
                }
                updateState(FIND_MY_PHONE, OnOffType.OFF);
            }
        }

        if (command instanceof RefreshType) {
            bridgeHandler.refreshData();
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof ICloudAccountBridgeHandler iCloudAccountBridgeHandler) {
                iCloudAccountBridgeHandler.unregisterListener(this);
            }
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
        this.logger.debug("Device: [{}]", this.deviceId);
        for (ICloudDeviceInformation deviceInformationRecord : deviceInformationList) {
            String currentId = deviceInformationRecord.getDeviceDiscoveryId();
            if (currentId == null || currentId.isBlank()) {
                logger.debug("deviceDiscoveryId is empty, using device name for identification.");
                currentId = deviceInformationRecord.getName();
            }
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
}
