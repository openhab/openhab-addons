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
package org.openhab.binding.tapocontrol.internal.devices.rf;

import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.wifi.hub.TapoHubHandler;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO Basic Child-Device-Handler
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public abstract class TapoChildDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoChildDeviceHandler.class);
    protected final TapoErrorHandler deviceError = new TapoErrorHandler();
    protected final String uid;
    protected final String deviceId;
    protected @NonNullByDefault({}) TapoHubHandler hub;
    private TapoChildDeviceData deviceInfo = new TapoChildDeviceData();
    private Map<String, Object> oldStates = new HashMap<>();

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    protected TapoChildDeviceHandler(Thing thing) {
        super(thing);
        uid = getThing().getUID().getAsString();
        deviceId = getValueOrDefault(getThing().getProperties().get(CHILD_REPRESENTATION_PROPERTY), "");
    }

    /***********************************
     *
     * INIT AND SETTINGS
     *
     ************************************/

    /**
     * INITIALIZE DEVICE
     */
    @Override
    public void initialize() {
        logger.trace("({}) Initializing thing ", uid);
        Bridge bridgeThing = getBridge();
        if (bridgeThing != null) {
            if (bridgeThing.getHandler() instanceof TapoHubHandler tapoHubHandler) {
                this.hub = tapoHubHandler;
            }
            activateDevice();
        } else {
            deviceError.raiseError(ERR_CONFIG_NO_BRIDGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, deviceError.getMessage());
        }
    }

    /**
     * ACTIVATE DEVICE
     */
    private void activateDevice() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        if (hub.getChild(deviceId).isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * handle command sent to device
     *
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* perform actions */
        if (command instanceof RefreshType) {
            setDeviceData();
        } else {
            logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channelUID.getId());
        }
    }

    /***********************************
     *
     * DEVICE PROPERTIES
     *
     ************************************/

    /**
     * refresh child properties and data from hub data
     */
    public void setDeviceData() {
        setDeviceData(hub.getChild(deviceId));
    }

    /*
     * refresh child properties and data from hub data
     */
    public void setDeviceData(TapoChildDeviceData deviceInfo) {
        this.deviceInfo = deviceInfo;
        triggerEvents(deviceInfo);
        devicePropertiesChanged(deviceInfo);
        handleConnectionState();
    }

    /**
     * handle device state by connector error
     */
    private void handleConnectionState() {
        if (deviceInfo.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /**
     * UPDATE PROPERTIES
     *
     * @param deviceInfo ChildDeviceData
     */
    protected void devicePropertiesChanged(TapoChildDeviceData deviceInfo) {
        logger.trace("({}) devicePropertiesChanged ", uid);
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_SIGNAL_STRENGTH),
                getDecimalType(deviceInfo.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_BATTERY_LOW), getOnOffType(deviceInfo.batteryIsLow()));
    }

    /**
     * Fires events on {@link TapoChildDeviceData} changes.
     */
    protected void triggerEvents(TapoChildDeviceData deviceInfo) {
        if (checkForStateChange(CHANNEL_BATTERY_LOW, deviceInfo.batteryIsLow())) {
            if (deviceInfo.batteryIsLow()) {
                triggerChannel(getChannelID(CHANNEL_GROUP_DEVICE, EVENT_BATTERY_LOW), EVENT_STATE_BATTERY_LOW);
            }
        }
    }

    /***********************************
     *
     * CHANNELS
     *
     ************************************/
    /**
     * Get ChannelID including group
     *
     * @param group String channel-group
     * @param channel String channel-name
     * @return String channelID
     */
    protected String getChannelID(String group, String channel) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CHANNEL_GROUP_THING_SET.contains(thingTypeUID) && group.length() > 0) {
            return group + "#" + channel;
        }
        return channel;
    }

    /**
     * Checks if the state changed since the last channel update.
     *
     * @param stateName the name of the state (channel)
     * @param comparator comparison value
     * @return <code>true</code> if changed, <code>false</code> if not or no old value exists
     */
    protected boolean checkForStateChange(String stateName, Object comparator) {
        if (oldStates.get(stateName) == null) {
            oldStates.put(stateName, comparator);
        } else if (!comparator.equals(oldStates.get(stateName))) {
            oldStates.put(stateName, comparator);
            return true;
        }
        return false;
    }
}
