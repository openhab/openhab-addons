/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.digiplex.internal.DigiplexAreaConfiguration;
import org.openhab.binding.digiplex.internal.DigiplexBindingConstants;
import org.openhab.binding.digiplex.internal.communication.AreaArmDisarmResponse;
import org.openhab.binding.digiplex.internal.communication.AreaArmRequest;
import org.openhab.binding.digiplex.internal.communication.AreaDisarmRequest;
import org.openhab.binding.digiplex.internal.communication.AreaQuickArmRequest;
import org.openhab.binding.digiplex.internal.communication.AreaStatus;
import org.openhab.binding.digiplex.internal.communication.AreaStatusRequest;
import org.openhab.binding.digiplex.internal.communication.AreaStatusResponse;
import org.openhab.binding.digiplex.internal.communication.ArmType;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexRequest;
import org.openhab.binding.digiplex.internal.communication.events.AreaEvent;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link DigiplexAreaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class DigiplexAreaHandler extends BaseThingHandler {

    private @Nullable DigiplexAreaConfiguration config;
    private @Nullable DigiplexBridgeHandler bridgeHandler;
    private DigiplexAreaMessageHandler visitor = new DigiplexAreaMessageHandler();
    private int areaNo;
    private OpenClosedType armed = OpenClosedType.CLOSED;
    private StringType status = AreaStatus.DISARMED.toStringType();
    private OpenClosedType zoneInMemory = OpenClosedType.CLOSED;
    private OpenClosedType trouble = OpenClosedType.CLOSED;
    private OpenClosedType ready = OpenClosedType.CLOSED;
    private OpenClosedType inProgramming = OpenClosedType.CLOSED;
    private OpenClosedType alarm = OpenClosedType.CLOSED;
    private OpenClosedType strobe = OpenClosedType.CLOSED;
    private StringType lastCommandResult = new StringType();

    private @Nullable ScheduledFuture<?> refreshTask;

    public DigiplexAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case AREA_STATUS:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_STATUS, status);
                }
                break;
            case AREA_ARMED:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_ARMED, armed);
                }
                break;
            case AREA_ZONE_IN_MEMORY:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_ZONE_IN_MEMORY, zoneInMemory);
                }
                break;
            case AREA_TROUBLE:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_TROUBLE, trouble);
                }
                break;
            case AREA_READY:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_READY, ready);
                }
                break;
            case AREA_IN_PROGRAMMING:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_IN_PROGRAMMING, inProgramming);
                }
                break;
            case AREA_ALARM:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_ALARM, alarm);
                }
                break;
            case AREA_STROBE:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_STROBE, strobe);
                }
                break;
            case AREA_CONTROL:
                if (command == RefreshType.REFRESH) {
                    updateState(AREA_CONTROL, lastCommandResult);
                } else if (command instanceof StringType) {
                    processControlCommand(((StringType) command).toString());
                }
                break;
        }
    }

    @SuppressWarnings("null")
    private void processControlCommand(String command) {
        if (command.length() < 2) {
            updateControlChannel(COMMAND_FAIL);
            return;
        }

        char commandType = command.charAt(0);
        char commandSubType = command.charAt(1);
        switch (commandType) {
            case 'A':
                bridgeHandler.sendRequest(
                        new AreaArmRequest(areaNo, ArmType.fromMessage(commandSubType), command.substring(2)));
                break;
            case 'Q':
                bridgeHandler.sendRequest(new AreaQuickArmRequest(areaNo, ArmType.fromMessage(commandSubType)));
                break;
            case 'D':
                bridgeHandler.sendRequest(new AreaDisarmRequest(areaNo, command.substring(1)));
                break;
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(DigiplexAreaConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        bridgeHandler = (DigiplexBridgeHandler) bridge.getHandler();

        String areaParm = getThing().getProperties().get(DigiplexBindingConstants.PROPERTY_AREA_NO);
        if (areaParm != null) {
            areaNo = Integer.parseInt(areaParm);
        }
        bridgeHandler.registerMessageHandler(visitor);

        updateStatus(ThingStatus.ONLINE);

        refreshTask = scheduler.scheduleWithFixedDelay(() -> {
            sendStatusUpdateRequest();
        }, 0, config.refreshPeriod, TimeUnit.SECONDS);
    }

    private void updateChannelsAfterStatusResponse() {
        updateState(AREA_STATUS, status);
        updateState(AREA_ARMED, armed);
        updateState(AREA_ZONE_IN_MEMORY, zoneInMemory);
        updateState(AREA_TROUBLE, trouble);
        updateState(AREA_READY, ready);
        updateState(AREA_IN_PROGRAMMING, inProgramming);
        updateState(AREA_ALARM, alarm);
        updateState(AREA_STROBE, strobe);
    }

    @SuppressWarnings("null")
    @Override
    public void handleRemoval() {
        if (visitor != null) {
            bridgeHandler.unregisterMessageHandler(visitor);
        }
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        super.handleRemoval();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo thingStatusInfo) {
        if (thingStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, thingStatusInfo.getStatusDetail());
        } else if (thingStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            sendStatusUpdateRequest();
        }
    }

    private synchronized void updateControlChannel(StringType response) {
        lastCommandResult = response;
        updateState(AREA_CONTROL, lastCommandResult);
    }

    @SuppressWarnings("null")
    private void sendStatusUpdateRequest() {
        DigiplexRequest request = new AreaStatusRequest(areaNo);
        bridgeHandler.sendRequest(request);
    }

    private class DigiplexAreaMessageHandler implements DigiplexMessageHandler {

        @Override
        public void handleAreaStatusResponse(AreaStatusResponse response) {
            if (response.success && response.areaNo == DigiplexAreaHandler.this.areaNo) {
                status = new StringType(response.status.toString());
                armed = response.status.toOpenClosedType();
                zoneInMemory = openClosedFromBoolean(response.zoneInMemory);
                trouble = openClosedFromBoolean(response.trouble);
                ready = openClosedFromBoolean(response.ready);
                inProgramming = openClosedFromBoolean(response.inProgramming);
                alarm = openClosedFromBoolean(response.alarm);
                strobe = openClosedFromBoolean(response.strobe);
                updateChannelsAfterStatusResponse();
            }
        }

        @Override
        public void handleArmDisarmAreaResponse(AreaArmDisarmResponse response) {
            if (response.areaNo == DigiplexAreaHandler.this.areaNo) {
                if (response.success) {
                    updateControlChannel(COMMAND_OK);
                } else {
                    updateControlChannel(COMMAND_FAIL);
                }
            }
        }

        @Override
        public void handleAreaEvent(AreaEvent event) {
            if (event.isForArea(DigiplexAreaHandler.this.areaNo)) {
                switch (event.getType()) {
                    case READY: // TODO: not sure what it means. Let's send status update request
                    case DISARMED: // in case of disarm we want to ensure that all other channels are updated as well
                        sendStatusUpdateRequest();
                        break;
                    case ALARM_STROBE:
                        strobe = OpenClosedType.OPEN;
                        updateState(AREA_STROBE, strobe);
                        // no break intentionally
                    case ALARM_FIRE:
                    case ALARM_AUDIBLE:
                    case ALARM_IN_MEMORY:
                    case ALARM_SILENT:
                        alarm = OpenClosedType.OPEN;
                        updateState(AREA_ALARM, alarm);
                        break;
                    case ARMED:
                    case ARMED_FORCE:
                    case ARMED_INSTANT:
                    case ARMED_STAY:
                        armed = OpenClosedType.OPEN;
                        updateState(AREA_ARMED, armed);
                        break;
                    case SYSTEM_IN_TROUBLE:
                        trouble = OpenClosedType.OPEN;
                        updateState(AREA_TROUBLE, trouble);
                        break;
                    case ZONES_BYPASSED:
                    case ENTRY_DELAY:
                    case EXIT_DELAY:
                    default:
                        break;

                }
                // update status separately, for more concise logic
                Optional<AreaStatus> tempStatus = Optional.empty();
                switch (event.getType()) {
                    case ARMED:
                        tempStatus = Optional.of(AreaStatus.ARMED);
                        break;
                    case ARMED_FORCE:
                        tempStatus = Optional.of(AreaStatus.ARMED_FORCE);
                        break;
                    case ARMED_INSTANT:
                        tempStatus = Optional.of(AreaStatus.ARMED_INSTANT);
                        break;
                    case ARMED_STAY:
                        tempStatus = Optional.of(AreaStatus.ARMED_STAY);
                        break;
                    case DISARMED:
                        tempStatus = Optional.of(AreaStatus.DISARMED);
                        break;
                    default:
                        break;
                }
                tempStatus.ifPresent(s -> updateState(AREA_STATUS, s.toStringType()));
            }
        }
    }
}
