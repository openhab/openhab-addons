/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;
import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAlarm;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAlarmEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcAlarm2;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlAlarmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlAlarmHandler extends NikoHomeControlBaseHandler implements NhcAlarmEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlAlarmHandler.class);

    private volatile @Nullable NhcAlarm nhcAlarm;

    public NikoHomeControlAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    void handleCommandSelection(ChannelUID channelUID, Command command) {
        NhcAlarm nhcAlarm = this.nhcAlarm;
        if (nhcAlarm == null) {
            logger.debug("alarm device with ID {} not initialized", deviceId);
            return;
        }

        logger.debug("handle command {} for {}", command, channelUID);

        if (REFRESH.equals(command)) {
            alarmEvent(nhcAlarm.getState());
            return;
        }

        if ((CHANNEL_ARM.equals(channelUID.getId()) || CHANNEL_ARMED.equals(channelUID.getId()))
                && command instanceof OnOffType s) {
            if (OnOffType.ON.equals(s)) {
                nhcAlarm.executeArm();
            } else {
                nhcAlarm.executeDisarm();
            }
        } else {
            logger.debug("unexpected command for channel {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        initialized = false;

        NikoHomeControlAlarmConfig config = getConfig().as(NikoHomeControlAlarmConfig.class);
        deviceId = config.alarmId;

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-bridge-handler");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if ((bridge != null) && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            // We need to do this in a separate thread because we may have to wait for the
            // communication to become active
            commStartThread = scheduler.submit(this::startCommunication);
        }
    }

    @Override
    synchronized void startCommunication() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());

        if (nhcComm == null) {
            return;
        }

        if (!nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
            return;
        }

        NhcAlarm nhcAlarm = nhcComm.getAlarmDevices().get(deviceId);
        if (nhcAlarm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.deviceId");
            return;
        }

        nhcAlarm.setEventHandler(this);

        updateProperties(nhcAlarm);

        String location = nhcAlarm.getLocation();
        if (thing.getLocation() == null) {
            thing.setLocation(location);
        }

        this.nhcAlarm = nhcAlarm;

        initialized = true;
        deviceInitialized();
    }

    @Override
    void refresh() {
        NhcAlarm alarm = nhcAlarm;
        if (alarm != null) {
            alarmEvent(alarm.getState());
        }
    }

    @Override
    public void dispose() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            NhcAlarm alarm = nhcComm.getAlarmDevices().get(deviceId);
            if (alarm != null) {
                alarm.unsetEventHandler();
            }
        }
        nhcAlarm = null;
        super.dispose();
    }

    private void updateProperties(NhcAlarm nhcAlarm) {
        Map<String, String> properties = new HashMap<>();

        if (nhcAlarm instanceof NhcAlarm2 alarm) {
            properties.put(PROPERTY_DEVICE_TYPE, alarm.getDeviceType());
            properties.put(PROPERTY_DEVICE_TECHNOLOGY, alarm.getDeviceTechnology());
            properties.put(PROPERTY_DEVICE_MODEL, alarm.getDeviceModel());
        }

        thing.setProperties(properties);
    }

    @Override
    public void alarmEvent(String state) {
        NhcAlarm nhcAlarm = this.nhcAlarm;
        if (nhcAlarm == null) {
            logger.debug(" alarm device with ID {} not initialized", deviceId);
            return;
        }

        updateState(CHANNEL_ARM,
                NHCOFF.equals(state) || NHCDETECTORPROBLEM.equals(state) ? OnOffType.OFF : OnOffType.ON);
        updateState(CHANNEL_ARMED,
                NHCOFF.equals(state) || NHCPREARMED.equals(state) || NHCDETECTORPROBLEM.equals(state) ? OnOffType.OFF
                        : OnOffType.ON);
        updateState(CHANNEL_STATE, StringType.valueOf(ALARMSTATES.get(state)));
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void alarmTriggerEvent() {
        NhcAlarm nhcAlarm = this.nhcAlarm;
        if (nhcAlarm == null) {
            logger.debug(" alarm device with ID {} not initialized", deviceId);
            return;
        }

        triggerChannel(CHANNEL_ALARM);
        updateStatus(ThingStatus.ONLINE);
    }
}
