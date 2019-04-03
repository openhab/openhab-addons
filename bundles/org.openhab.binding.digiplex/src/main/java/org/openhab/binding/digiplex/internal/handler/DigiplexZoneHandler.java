/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.digiplex.internal.handler;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;
import static org.openhab.binding.digiplex.internal.handler.TypeUtils.openClosedFromBoolean;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.digiplex.internal.DigiplexBindingConstants;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneStatusRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneStatusResponse;
import org.openhab.binding.digiplex.internal.communication.events.ZoneEvent;
import org.openhab.binding.digiplex.internal.communication.events.ZoneEventType;
import org.openhab.binding.digiplex.internal.communication.events.ZoneStatusEvent;

/**
 * The {@link DigiplexZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class DigiplexZoneHandler extends BaseThingHandler {

    @Nullable
    private DigiplexBridgeHandler bridgeHandler;
    private DigiplexZoneMessageHandler messageHandler = new DigiplexZoneMessageHandler();;
    private int zoneNo;
    private int areaNo = 0; // not known at the beginning (protocol limitation)
    private OpenClosedType status = OpenClosedType.CLOSED;
    private StringType extendedStatus = new StringType("CLOSED");
    private OpenClosedType alarm = OpenClosedType.CLOSED;
    private OpenClosedType fireAlarm = OpenClosedType.CLOSED;
    private OpenClosedType supervisionLost = OpenClosedType.CLOSED;
    private OpenClosedType lowBattery = OpenClosedType.CLOSED;
    private State lastTriggered = UnDefType.NULL;

    public DigiplexZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case ZONE_STATUS:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_STATUS, status);
                }
                break;
            case ZONE_EXTENDED_STATUS:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_EXTENDED_STATUS, extendedStatus);
                }
                break;
            case ZONE_ALARM:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_ALARM, alarm);
                }
                break;
            case ZONE_FIRE_ALARM:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_FIRE_ALARM, fireAlarm);
                }
                break;
            case ZONE_SUPERVISION_LOST:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_SUPERVISION_LOST, supervisionLost);
                }
                break;
            case ZONE_LOW_BATTERY:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_LOW_BATTERY, lowBattery);
                }
                break;
            case ZONE_LAST_TRIGGERED:
                if (command == RefreshType.REFRESH) {
                    updateState(ZONE_LAST_TRIGGERED, lastTriggered);
                }
                break;
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        this.bridgeHandler = (DigiplexBridgeHandler) bridge.getHandler();

        String nodeParm = getThing().getProperties().get(DigiplexBindingConstants.PROPERTY_ZONE_NO);
        zoneNo = Integer.parseInt(nodeParm);
        String areaParm = getThing().getProperties().get(DigiplexBindingConstants.PROPERTY_AREA_NO);
        if (areaParm != null) {
            areaNo = Integer.parseInt(areaParm);
        }

        bridgeHandler.registerMessageHandler(messageHandler);

        DigiplexRequest request = new ZoneStatusRequest(zoneNo);
        bridgeHandler.sendRequest(request);

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateChannels(boolean allChannels) {
        updateState(ZONE_STATUS, status);
        updateState(ZONE_EXTENDED_STATUS, extendedStatus);
        updateState(ZONE_LAST_TRIGGERED, lastTriggered);
        if (allChannels) {
            updateState(ZONE_ALARM, alarm);
            updateState(ZONE_FIRE_ALARM, fireAlarm);
            updateState(ZONE_SUPERVISION_LOST, supervisionLost);
            updateState(ZONE_LOW_BATTERY, lowBattery);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void handleRemoval() {
        if (messageHandler != null) {
            bridgeHandler.unregisterMessageHandler(messageHandler);
        }
        super.handleRemoval();
    }

    @SuppressWarnings("null")
    @Override
    public void bridgeStatusChanged(ThingStatusInfo thingStatusInfo) {
        if (thingStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, thingStatusInfo.getStatusDetail());
        } else if (thingStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            DigiplexRequest request = new ZoneStatusRequest(zoneNo);
            bridgeHandler.sendRequest(request);
        }
    }

    private void updateAreaNo(int areaNo) {
        if (this.areaNo == 0) {
            this.areaNo = areaNo;
            getThing().setProperty(DigiplexBindingConstants.PROPERTY_AREA_NO, Integer.toString(areaNo));
        }
    }

    private class DigiplexZoneMessageHandler implements DigiplexMessageHandler {

        @Override
        public void handleZoneStatusResponse(ZoneStatusResponse response) {
            if (response.zoneNo == DigiplexZoneHandler.this.zoneNo) {
                status = response.status.toOpenClosedType();
                extendedStatus = new StringType(response.status.toString());
                alarm = openClosedFromBoolean(response.alarm);
                fireAlarm = openClosedFromBoolean(response.fireAlarm);
                supervisionLost = openClosedFromBoolean(response.supervisionLost);
                lowBattery = openClosedFromBoolean(response.lowBattery);
                updateChannels(true);
            }
        }

        @Override
        public void handleZoneStatusEvent(ZoneStatusEvent event) {
            if (event.getZoneNo() == DigiplexZoneHandler.this.zoneNo) {
                status = event.getStatus().toOpenClosedType();
                extendedStatus = new StringType(event.getStatus().toString());
                lastTriggered = new DateTimeType(ZonedDateTime.now());
                updateChannels(false);
                updateAreaNo(event.getAreaNo());
            }
        }

        @Override
        public void handleZoneEvent(@NonNull ZoneEvent event) {
            if (event.getZoneNo() == DigiplexZoneHandler.this.zoneNo) {
                switch (event.getType()) {
                    case ALARM:
                    case ALARM_RESTORE:
                        alarm = openClosedFromBoolean(event.getType() == ZoneEventType.ALARM);
                        updateState(ZONE_ALARM, alarm);
                        break;
                    case FIRE_ALARM:
                    case FIRE_ALARM_RESTORE:
                        fireAlarm = openClosedFromBoolean(event.getType() == ZoneEventType.FIRE_ALARM);
                        updateState(ZONE_FIRE_ALARM, fireAlarm);
                        break;
                    case LOW_BATTERY:
                    case LOW_BATTERY_RESTORE:
                        lowBattery = openClosedFromBoolean(event.getType() == ZoneEventType.LOW_BATTERY);
                        updateState(ZONE_LOW_BATTERY, lowBattery);
                        break;
                    case SUPERVISION_TROUBLE:
                    case SUPERVISION_TROUBLE_RESTORE:
                        supervisionLost = openClosedFromBoolean(event.getType() == ZoneEventType.SUPERVISION_TROUBLE);
                        updateState(ZONE_SUPERVISION_LOST, supervisionLost);
                        break;
                    default:
                        break;

                }
                updateAreaNo(event.getAreaNo());
            }
        }
    }
}
