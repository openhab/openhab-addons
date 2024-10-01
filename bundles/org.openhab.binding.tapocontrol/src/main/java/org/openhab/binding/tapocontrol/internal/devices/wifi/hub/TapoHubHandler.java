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
package org.openhab.binding.tapocontrol.internal.devices.wifi.hub;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildList;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
import org.openhab.binding.tapocontrol.internal.discovery.TapoChildDiscoveryService;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TapoHubHandler} is responsible for handling commands, which are
 * sent to the child devices of the hub with a bridge.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoHubHandler extends TapoBaseDeviceHandler implements BridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoHubHandler.class);
    protected TapoHubData hubData = new TapoHubData();
    private TapoChildList tapoChildsList = new TapoChildList();
    private @NonNullByDefault({}) TapoChildDiscoveryService discoveryService;
    private List<Thing> tapoChildThings = new ArrayList<>();

    public TapoHubHandler(Thing thing) {
        super(thing);
        logger.trace("{} Hub initialized", uid);
    }

    /**
     * Activate Device
     */
    @Override
    protected void activateDevice() {
        super.activateDevice();
        discoveryService.setBackGroundDiscovery(deviceConfig.backgroundDiscovery);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} Hub doesn't handle command: {}", uid, command);
    }

    @Override
    public void dispose() {
        logger.trace("{} Hub disposed ", uid);
        super.dispose();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.trace("({}) childHandlerInitialized '{}'", uid, childThing.getUID());
        tapoChildThings.add(childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.trace("({}) childHandlerDisposed '{}'", uid, childThing.getUID());
        tapoChildThings.remove(childThing);
    }

    /***********************************
     *
     * CHILD DISCOVERY SERVICE
     *
     ************************************/

    /**
     * ACTIVATE DISCOVERY SERVICE
     */

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TapoChildDiscoveryService.class);
    }

    /**
     * Set DiscoveryService
     * 
     * @param discoveryService
     */
    public void setDiscoveryService(TapoChildDiscoveryService discoveryService) {
        discoveryService.setBackGroundDiscovery(deviceConfig.backgroundDiscovery);
        this.discoveryService = discoveryService;
    }

    /****************************
     * PUBLIC FUNCTIONS
     ****************************/

    /**
     * query device Properties
     */
    @Override
    public void queryDeviceData() {
        deviceError.reset();
        if (isLoggedIn(LOGIN_RETRIES)) {
            List<TapoRequest> requests = new ArrayList<>();
            requests.add(new TapoRequest(DEVICE_CMD_GETINFO));
            requests.add(new TapoRequest(DEVICE_CMD_GETCHILDDEVICELIST));
            connector.sendMultipleRequest(requests);
        }
    }

    /**
     * Function called by {@link org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector} if new data were
     * received
     * 
     * @param queryCommand command where new data belong to
     */
    @Override
    public void newDataResult(String queryCommand) {
        super.newDataResult(queryCommand);
        switch (queryCommand) {
            case DEVICE_CMD_GETINFO:
                hubData = connector.getResponseData(TapoHubData.class);
                updateChannels(hubData);
                break;
            case DEVICE_CMD_GETCHILDDEVICELIST:
                tapoChildsList = connector.getResponseData(TapoChildList.class);
                updateChildDevices(tapoChildsList);
                break;
            default:
                responsePasstrough(connector.getResponseData(TapoResponse.class));
                break;
        }
    }

    /****************************
     * CHILD THINGS
     ****************************/

    /**
     * Update all Child-Things
     */
    public void updateChildThings() {
        for (Thing thing : tapoChildThings) {
            updateChild(thing);
        }
    }

    /**
     * Update Child single child with special representationProperty
     *
     * @param thingTypeToUpdate ThingTypeUID of Thing to update
     * @param representationProperty Name of representationProperty
     * @param propertyValue Value of representationProperty
     */
    public void updateChild(ThingTypeUID thingTypeToUpdate, String representationProperty, String propertyValue) {
        for (Thing thing : tapoChildThings) {
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();
            if (thingTypeToUpdate.equals(thingTypeUID)) {
                String thingProperty = thing.getProperties().get(representationProperty);
                if (propertyValue.equals(thingProperty)) {
                    updateChild(thing);
                }
            }
        }
    }

    /**
     * Update Child-Thing (send refreshCommand)
     *
     * @param thing - Thing to update
     */
    public void updateChild(Thing thing) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            ChannelUID cUid = new ChannelUID(thing.getUID(), "any");
            handler.handleCommand(cUid, RefreshType.REFRESH);
        }
    }

    /**
     * Set State of all clients
     *
     * @param thingStatus new ThingStatus
     */
    public void updateChildStates(ThingStatus thingStatus) {
        for (Thing thing : tapoChildThings) {
            updateChildState(thing, thingStatus);
        }
    }

    /**
     * Set State of a Thing
     *
     * @param thing Thing to update
     * @param thingStatus new ThingStatus
     */
    public void updateChildState(Thing thing, ThingStatus thingStatus) {
        logger.trace("{} set child states to {} by hub", uid, thingStatus);
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            if (ThingStatus.OFFLINE.equals(thingStatus)) {
                handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.BRIDGE_OFFLINE, ""));
            } else {
                handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.NONE, ""));
            }
        }
    }

    /****************************
     * UPDATE HUB CHANNELS
     ****************************/

    /**
     * Update Channels
     */
    protected void updateChannels(TapoHubData hubData) {
        updateState(getChannelID(CHANNEL_GROUP_ALARM, CHANNEL_ALARM_ACTIVE), getOnOffType(hubData.alarmIsActive()));
        updateState(getChannelID(CHANNEL_GROUP_ALARM, CHANNEL_ALARM_SOURCE), getStringType(hubData.getAlarmSource()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(hubData.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(hubData.isOverheated()));
    }

    /**
     * Update channels of childdevices
     */
    protected void updateChildDevices(TapoChildList childList) {
        tapoChildsList = childList;
        if (discoveryService.isBackgroundDiscoveryEnabled()) {
            discoveryService.thingsDiscovered(childList.getChildDeviceList());
        }
        /* update children */
        updateChildThings();
    }

    /****************************
     * HUB GETTERS
     ****************************/

    public ThingUID getUID() {
        return getThing().getUID();
    }

    public List<TapoChildDeviceData> getChildDevices() {
        return tapoChildsList.getChildDeviceList();
    }

    public TapoChildDeviceData getChild(String deviceSerial) {
        List<TapoChildDeviceData> childDeviceList = tapoChildsList.getChildDeviceList();
        for (int i = 0; i <= childDeviceList.size(); i++) {
            TapoChildDeviceData child = childDeviceList.get(i);
            if (child.getDeviceId().equals(deviceSerial)) {
                return child;
            }
        }
        logger.debug("child not found in deviceList '{}'", deviceSerial);
        return new TapoChildDeviceData();
    }
}
