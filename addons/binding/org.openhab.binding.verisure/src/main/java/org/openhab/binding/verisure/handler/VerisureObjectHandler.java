/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.VerisureBindingConstants;
import org.openhab.binding.verisure.internal.DeviceStatusListener;
import org.openhab.binding.verisure.internal.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.VerisureDoorWindowsJSON;
import org.openhab.binding.verisure.internal.VerisureObjectJSON;
import org.openhab.binding.verisure.internal.VerisureSensorJSON;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureSmartPlugJSON;
import org.openhab.binding.verisure.internal.VerisureUserTrackingJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Handler for all of the different object types that Verisure provides.
 *
 * @author Jarle Hjortland
 *
 */
public class VerisureObjectHandler extends BaseThingHandler implements DeviceStatusListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_ALARM,
            THING_TYPE_SMARTPLUG, THING_TYPE_CLIMATESENSOR, THING_TYPE_LOCK, THING_TYPE_USERPRESENCE,
            THING_TYPE_DOORWINDOW);

    private Logger logger = LoggerFactory.getLogger(VerisureObjectHandler.class);

    private VerisureSession session = null;

    private String id = null;

    public VerisureObjectHandler(Thing thing) {
        super(thing);
        this.id = thing.getUID().getId();

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            Bridge brige = getBridge();
            if (brige != null && brige.getHandler() != null) {
                brige.getHandler().handleCommand(channelUID, command);
            }
            update(session.getVerisureObject(this.id));
        } else if (channelUID.getId().equals(CHANNEL_DOORLOCK)) {
            handleLockUnlockDoor(command);
            scheduleImmediateRefresh();
        } else {
            logger.warn("unknown command! {}", command);
        }
    }

    private void handleLockUnlockDoor(Command command) {
        if (command == OnOffType.ON) {
            logger.debug("attempting to lock door!");
            session.lockDoor(this.id);
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, new StringType("pending"));
        } else if (command == OnOffType.OFF) {
            logger.debug("attempting to unlock door!");
            session.unLockDoor(this.id);
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, new StringType("pending"));
        }
    }

    private void scheduleImmediateRefresh() {
        Bridge brige = getBridge();
        if (brige != null && brige.getHandler() != null) {
            VerisureBridgeHandler vbh = (VerisureBridgeHandler) brige.getHandler();
            vbh.scheduleImmediateRefresh();
        }
    }

    @Override
    public void initialize() {
        // Do not go online
        if (getBridge() != null) {
            this.bridgeStatusChanged(getBridge().getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (this.getBridge() != null && this.getBridge().getHandler() != null) {
                VerisureBridgeHandler vbh = (VerisureBridgeHandler) this.getBridge().getHandler();
                session = vbh.getSession();
                update(session.getVerisureObject(this.id));
                vbh.registerObjectStatusListener(this);
            }
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    public synchronized void update(VerisureObjectJSON object) {
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_CLIMATESENSOR)) {
            VerisureSensorJSON obj = (VerisureSensorJSON) object;
            updateSensorState(obj);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_LOCK)) {
            VerisureAlarmJSON obj = (VerisureAlarmJSON) object;
            updateLockState(obj);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_DOORWINDOW)) {
            VerisureDoorWindowsJSON obj = (VerisureDoorWindowsJSON) object;
            updateDoorWindowState(obj);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_USERPRESENCE)) {
            VerisureUserTrackingJSON obj = (VerisureUserTrackingJSON) object;
            updateUserPresenceState(obj);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_SMARTPLUG)) {
            VerisureSmartPlugJSON obj = (VerisureSmartPlugJSON) object;
            updateSmartPlugState(obj);
        } else {
            logger.warn("cant handle this thingtypeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateSensorState(VerisureSensorJSON obj) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE);
        ChannelUID huid = new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY);
        ChannelUID luid = new ChannelUID(getThing().getUID(), CHANNEL_LASTUPDATE);
        ChannelUID loid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        String val = obj.getTemperature().substring(0, obj.getTemperature().length() - 6).replace(",", ".");

        DecimalType number = new DecimalType(val);
        updateState(cuid, number);
        if (obj.getHumidity() != null && obj.getHumidity().length() > 1) {
            val = obj.getHumidity().substring(0, obj.getHumidity().indexOf("%")).replace(",", ".");
            DecimalType hnumber = new DecimalType(val);
            updateState(huid, hnumber);
        }
        StringType lastUpdate = new StringType(obj.getTimestamp());
        updateState(luid, lastUpdate);
        StringType location = new StringType(obj.getLocation());
        updateState(loid, location);
    }

    private void updateLockState(VerisureAlarmJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
        String lockstatus = status.getStatus();
        updateState(cuid, new StringType(lockstatus));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_DOORLOCK);
        if ("locked".equals(lockstatus)) {
            updateState(cuid, OnOffType.ON);
        } else if ("unlocked".equals(lockstatus)) {
            updateState(cuid, OnOffType.OFF);
        } else if ("pending".equals(lockstatus)) {
            // Schedule another refresh.
            this.scheduleImmediateRefresh();
        }

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGERNAME);
        updateState(cuid, new StringType(status.getName()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP);
        updateState(cuid, new StringType(status.getDate()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(status.getLocation()));

        cuid = new ChannelUID(getThing().getUID(), VerisureBindingConstants.CHANNEL_STATUS_LOCALIZED);
        updateState(cuid, new StringType(status.getLabel()));
    }

    private void updateDoorWindowState(VerisureDoorWindowsJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
        updateState(cuid, new StringType(status.getState()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LABEL);
        updateState(cuid, new StringType(status.getArea()));

    }

    private void updateSmartPlugState(VerisureSmartPlugJSON status) {

    }

    private void updateUserPresenceState(VerisureUserTrackingJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATIONNAME);
        updateState(cuid, new StringType(status.getUserLocatonName()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_WEBACCOUNT);
        updateState(cuid, new StringType(status.getWebAccount()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATIONSTATUS);
        updateState(cuid, new StringType(status.getUserLocationStatus()));

        cuid = new ChannelUID(getThing().getUID(), CHANNEL_ISHOME);
        if (status.getUserLocationStatus() != null && status.getUserLocationStatus().equals("HOME")) {
            updateState(cuid, OnOffType.ON);
        } else {
            updateState(cuid, OnOffType.OFF);
        }
    }

    @Override
    public void onDeviceStateChanged(VerisureObjectJSON updateObject) {
        if (updateObject.getId().equals(this.id)) {
            update(updateObject);
        }
    }

    @Override
    public void onDeviceRemoved(VerisureObjectJSON updateObject) {
        if (updateObject.getId().equals(this.id)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Object Removed");
        }
    }

    @Override
    public void onDeviceAdded(VerisureObjectJSON updateObject) {
    }
}
