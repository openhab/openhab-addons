/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.handler;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.senseenergy.internal.actions.SenseEnergyMonitorActions;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApi;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApiException;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyDatagram;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyDatagramListener;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyWebSocket;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyWebSocketListener;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiDevice;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiMonitor;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiMonitorInfo;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiMonitorStatus;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetRealtime;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetSysInfo;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyWebSocketDevice;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyWebSocketRealtimeUpdate;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SenseEnergyBridgeHandler} is the handler for Sense API and connects it
 * to the webservice.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyMonitorHandler extends BaseBridgeHandler
        implements SenseEnergyWebSocketListener, SenseEnergyDatagramListener {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyMonitorHandler.class);

    private static final int MAX_RESPONSES_PER_REQUEST = 5;
    private static final int SENSE_DATAGRAM_BCAST_PORT = 9999;

    private static final String CHANNEL_PROPERTY_ID = "sense-id";
    private static final String CHANNEL_PROPERTY_LABEL = "sense-label";

    private long id;
    @Nullable
    private SenseEnergyApiMonitorStatus apiMonitorStatus;

    private SenseEnergyWebSocket webSocket;
    private SenseEnergyDatagram datagram;

    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    public enum DeviceType {
        DISCOVERED_DEVICE,
        SELF_REPORTING_DEVICE,
        PROXY_DEVICE;

        public static String getChannelGroup(DeviceType deviceType) {
            return switch (deviceType) {
                case PROXY_DEVICE -> CHANNEL_GROUP_PROXY_DEVICES;
                case SELF_REPORTING_DEVICE -> CHANNEL_GROUP_SELF_REPORTING_DEVICES;
                default -> CHANNEL_GROUP_DISCOVERED_DEVICES;
            };
        }
    }

    // Map of all device types from the api
    private Map<String, SenseEnergyApiDevice> senseDevices = Collections.emptyMap();
    // DeviceTypes deduced from the senseDevices
    private Map<String, DeviceType> senseDevicesType = new HashMap<String, DeviceType>();
    // Keep track of which devices are on so we can send trigger when devices are turned on/off
    private Set<String> devicesOn = Collections.emptySet();

    private static final Set<String> GENERATED_CHANNEL_GROUPS = Set.of(CHANNEL_GROUP_DISCOVERED_DEVICES,
            CHANNEL_GROUP_SELF_REPORTING_DEVICES, CHANNEL_GROUP_PROXY_DEVICES);

    // counter to slow down updates to openHAB for every power update
    private int countRealTimeUpdate;

    private Iterator<Thing> roundRobinIterator = Collections.emptyIterator();

    boolean solarConfigured = true;

    public SenseEnergyMonitorHandler(final Bridge thing, final WebSocketClient webSocketClient,
            final ChannelGroupTypeRegistry channelGroupTypeRegistry, final ChannelTypeRegistry channelTypeRegistry) {
        super(thing);

        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
        this.channelTypeRegistry = channelTypeRegistry;

        this.webSocket = new SenseEnergyWebSocket(this, webSocketClient);
        this.datagram = new SenseEnergyDatagram(this);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SenseEnergyMonitorActions.class);
    }

    public long getId() {
        return id;
    }

    @Override
    public void initialize() {
        id = ((Number) getThing().getConfiguration().get(PARAM_MONITOR_ID)).intValue();

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::goOnline);
    }

    public void goOnline() {
        if (getThing().getStatus() == ThingStatus.ONLINE || !checkBridgeStatus()) {
            return;
        }

        try {
            SenseEnergyApiMonitor apiMonitor = getApi().getMonitorOverview(id);
            this.solarConfigured = apiMonitor.solarConfigured;
            apiMonitorStatus = getApi().getMonitorStatus(id);
            refreshDevices();
        } catch (SenseEnergyApiException e) {
            handleApiException(e);
            return;
        }

        ThingBuilder thingBuilder = editThing();
        ThingUID thingUID = getThing().getUID();

        if (solarConfigured) {
            thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_GROUP_GENERAL, CHANNEL_LEG_1_POWER));
            thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_GROUP_GENERAL, CHANNEL_LEG_2_POWER));
        } else {
            thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_GROUP_GENERAL, CHANNEL_MAIN_POWER));
            thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_GROUP_GENERAL, CHANNEL_SOLAR_POWER));
        }

        reconcileDiscoveredDeviceChannels(thingBuilder);
        updateThing(thingBuilder.build());
        updateProperties();

        try {
            webSocket.start(id, getApi().getAccessToken());
        } catch (InterruptedException | ExecutionException | IOException | URISyntaxException e) {
            handleApiException(e);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void heartbeat() {
        ThingStatus thingStatus = getThing().getStatus();

        if (thingStatus == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            goOnline(); // only attempt to goOnline if not a configuration error
            return;
        }

        if (thingStatus != ThingStatus.ONLINE) {
            return;
        }

        logger.trace("SenseEnergyMonitorHandler: heartbeat");
        refreshDevices();
        reconcileDiscoveredDeviceChannels(null);

        if (!webSocket.isRunning()) {
            logger.debug("heartbeat: webSocket not running");
            try {
                webSocket.restart(getApi().getAccessToken());
            } catch (InterruptedException | ExecutionException | IOException | URISyntaxException e) {
                handleApiException(e);
            }
        }

        checkDatagramStatus();
    }

    public void handleApiException(Exception e) {
        if (e instanceof SenseEnergyApiException apiException) {
            switch (apiException.severity) {
                case TRANSIENT:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                    break;
                case CONFIG:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
                    break;
                case FATAL:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.getMessage());
                    break;
                case DATA:
                    logger.warn("Data exception: {}", e.toString());
                    break;
                default:
                    logger.warn("SenseEnergyApiException: {}", e.toString());
                    break;
            }
        } else {
            logger.warn("Unhandled Exception", e);
        }
    }

    @Override
    public void dispose() {
        webSocket.stop();
        datagram.stop();
    }

    public void updateProperties() {
        updateProperty(PROPERTY_MONITOR_SOLAR_CONFIGURED, Boolean.toString(solarConfigured));

        SenseEnergyApiMonitorStatus localMonitorStatus = apiMonitorStatus;
        if (localMonitorStatus != null && localMonitorStatus.monitorInfo != null) {
            SenseEnergyApiMonitorInfo info = Objects.requireNonNull(localMonitorStatus.monitorInfo);
            updateProperty(PROPERTY_MONITOR_IP_ADDRESS, Objects.requireNonNullElse(info.ipAddress, ""));
            updateProperty(PROPERTY_MONITOR_VERSION, Objects.requireNonNullElse(info.version, ""));
            updateProperty(PROPERTY_MONITOR_SERIAL, Objects.requireNonNullElse(info.serial, ""));
            updateProperty(PROPERTY_MONITOR_SSID, Objects.requireNonNullElse(info.ssid, ""));
            updateProperty(PROPERTY_MONITOR_MAC, Objects.requireNonNullElse(info.mac, ""));
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    public boolean checkBridgeStatus() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return false;
        }

        SenseEnergyBridgeHandler bridgeHandler = (SenseEnergyBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return false;
        }

        if (bridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        return true;
    }

    @Nullable
    SenseEnergyBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        return (bridge != null) ? (SenseEnergyBridgeHandler) bridge.getHandler() : null;
    }

    public SenseEnergyApi getApi() {
        SenseEnergyBridgeHandler handler = Objects.requireNonNull(getBridgeHandler(),
                "Invalid state where handler is null");
        return handler.getApi();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channelGroup = channelUID.getGroupId();
            if (channelGroup == null) {
                logger.debug("Channel does not have a group ID: {}", channelUID);
                return;
            }

            if (GENERATED_CHANNEL_GROUPS.contains(channelGroup)) {
                Channel channel = getThing().getChannel(channelUID);
                if (channel == null) {
                    logger.debug("Channel does not exist: {}", channelUID);
                    return;
                }

                String senseID = channel.getProperties().get(CHANNEL_PROPERTY_ID);
                if (senseID == null) {
                    logger.debug("Channel does not have a senseID property: {}", channelUID);
                    return;
                }

                if (!devicesOn.contains(senseID)) {
                    updateState(channelUID, new QuantityType<>(0, Units.WATT));
                }
            }
        }
    }

    /**
     * Deduces the device type based by examining the properties and identifying if the device is a proxy device
     * in openHAB.
     *
     * @param apiDevice The device for which the type needs to be deduced.
     * @return The deduced DeviceType.
     */
    private DeviceType deduceDeviceType(SenseEnergyApiDevice apiDevice) {
        if (!apiDevice.tags.ssiEnabled) {
            return DeviceType.DISCOVERED_DEVICE;
        }

        SenseEnergyProxyDeviceHandler proxyHandler = getProxyDeviceByMAC(apiDevice.tags.deviceID);
        return (proxyHandler != null) ? DeviceType.PROXY_DEVICE : DeviceType.SELF_REPORTING_DEVICE;
    }

    /**
     * Refreshes the list of devices by retrieving them from the API and then updating the map of DeviceTypes.
     */
    private void refreshDevices() {
        logger.trace("refreshDevices");
        try {
            senseDevices = getApi().getDevices(id);

            senseDevices.entrySet().stream() //
                    .filter(e -> !senseDevicesType.containsKey(e.getKey())) //
                    .forEach(e -> senseDevicesType.put(e.getKey(), deduceDeviceType(e.getValue())));
        } catch (SenseEnergyApiException e) {
            handleApiException(e);
        }
    }

    /*
     * Reconciles the discovered device channels to stay in sync with the discovered device list to ensure
     * all the channels in the channel template exist for every sense devices.
     *
     * @param thingBuilder to update if already editing, otherwise will open
     */
    public void reconcileDiscoveredDeviceChannels(@Nullable ThingBuilder thingBuilder) {
        ChannelGroupType channelGroupType = Objects
                .requireNonNull(channelGroupTypeRegistry.getChannelGroupType(CHANNEL_GROUP_TYPE_DEVICE_TEMPLATE));
        List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();

        Set<String> senseIDs = new HashSet<>(senseDevices.keySet());
        senseIDs.remove("solar"); // don't create solar as a separate channel

        logger.trace("Reconciling channels with Sense device, channel count: {}", senseIDs.size());

        boolean channelsUpdated = false;
        ThingBuilder localBuilder = (thingBuilder != null) ? thingBuilder : editThing();

        // reconcile every channel type that is in the group template
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = Objects
                    .requireNonNull(channelTypeRegistry.getChannelType(channelDefinition.getChannelTypeUID()));
            // create set of IDs of existing channels of the TypeUID from the template.
            @SuppressWarnings("null")
            Set<String> existingChannels = getThing().getChannels().stream() //
                    .filter(ch -> GENERATED_CHANNEL_GROUPS.contains(ch.getUID().getGroupId())) //
                    .filter(ch -> ch.getChannelTypeUID() != null && ch.getChannelTypeUID().equals(channelType.getUID())) //
                    .map(ch -> ch.getProperties().get(CHANNEL_PROPERTY_ID)) //
                    .filter(Objects::nonNull) //
                    .collect(Collectors.toSet());

            Set<String> allChannels = new HashSet<>(existingChannels);
            allChannels.addAll(senseIDs);

            for (String senseID : allChannels) {
                DeviceType deviceType = senseDevicesType.getOrDefault(senseID, DeviceType.DISCOVERED_DEVICE);

                ChannelUID channelUID = makeDeviceChannelUID(deviceType, senseID, channelDefinition.getId());
                if (existingChannels.contains(senseID) && !senseIDs.contains(senseID)) { // remove outdated channel
                    localBuilder.withoutChannel(channelUID);
                    channelsUpdated = true;
                } else if (existingChannels.contains(senseID) && senseIDs.contains(senseID)) { // update existing
                    String deviceName = Objects.requireNonNull(senseDevices.get(senseID)).name;
                    channelsUpdated |= updateGeneratedChannel(localBuilder, channelUID, deviceName);
                } else if (!existingChannels.contains(senseID) && senseIDs.contains(senseID)) { // add new channel
                    addGeneratedChannel(localBuilder, channelUID, Objects.requireNonNull(senseDevices.get(senseID)),
                            channelDefinition, channelType);
                    channelsUpdated = true;
                }
            }
        }

        if (thingBuilder == null) {
            updateThing(localBuilder.build());
        }

        if (channelsUpdated) {
            triggerChannel(new ChannelUID(getThing().getUID(), CHANNEL_GROUP_GENERAL, CHANNEL_DEVICES_UPDATED_TRIGGER));
        }
    }

    public void addGeneratedChannel(ThingBuilder builder, ChannelUID channelUID, SenseEnergyApiDevice apiDevice,
            ChannelDefinition channelDefinition, ChannelType channelType) {
        Channel channel = ChannelBuilder.create(channelUID)
                .withDescription(Objects.requireNonNull(channelDefinition.getDescription())) //
                .withLabel(apiDevice.name + ": " + channelDefinition.getLabel()) //
                .withProperties(Map.of(CHANNEL_PROPERTY_ID, apiDevice.id, CHANNEL_PROPERTY_LABEL, apiDevice.name)) //
                .withAcceptedItemType(channelType.getItemType()) //
                .withType(channelDefinition.getChannelTypeUID()) //
                .withKind(channelType.getKind()) //
                .withDefaultTags(channelType.getTags()) //
                .build();
        builder.withChannel(channel);
    }

    /*
     * updates channel label of an existing channel by removing it and then added it back
     *
     * @param thingBuilder
     * 
     * @param channelUID
     * 
     * @param label
     * 
     * @return whether channel needed to be updated
     */
    public boolean updateGeneratedChannel(ThingBuilder thingBuilder, ChannelUID channelUID, String label) {
        Channel channel = getThing().getChannel(channelUID);

        if (channel == null) {
            return false;
        }

        Map<String, String> properties = channel.getProperties();

        String currentLabel = properties.get(CHANNEL_PROPERTY_LABEL);
        if (label.equals(currentLabel)) {
            return false;
        }

        thingBuilder.withoutChannel(channelUID);

        Map<String, String> newProperties = new HashMap<String, String>(properties);
        newProperties.put(CHANNEL_PROPERTY_LABEL, label);

        Channel newChannel = ChannelBuilder.create(channel).withProperties(newProperties).withLabel(label).build();

        thingBuilder.withChannel(newChannel);

        return true;
    }

    /*
     * create a channelUID in the designated/consistent format
     *
     * @param senseID
     * 
     * @param channelID
     * 
     * @return
     */
    public ChannelUID makeDeviceChannelUID(DeviceType deviceType, String senseID, String channelID) {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(getThing().getUID(),
                DeviceType.getChannelGroup(deviceType));

        return new ChannelUID(channelGroupUID, senseID + "-" + channelID);
    }

    /*
     * helper function to update channel state
     *
     * @param channelGroup
     * 
     * @param channel
     * 
     * @param value
     * 
     * @param unit
     */
    public void updateChannel(String channelGroup, String channel, float value, Unit<?> unit) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelGroup, channel);
        updateState(channelUID, new QuantityType<>(value, unit));
    }

    /*
     * start/stop datagram when a proxy device is ONLINE or when they all are OFFLINE
     *
     * @param proxyDeviceHandler
     * 
     * @param thingStatus
     */
    public void childStatusChange(SenseEnergyProxyDeviceHandler proxyDeviceHandler, ThingStatus thingStatus) {
        if (thingStatus == ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.ONLINE) {
            throw new IllegalStateException("Child should never go ONLINE w/o the bridge being online");
        }

        checkDatagramStatus();
    }

    /**
     * start/stop and check datagram status depending on whether there are proxy devices or not
     */
    public void checkDatagramStatus() {
        final String datagramListenerThreadName = "OH-binding-" + getThing().getUID().getAsString();

        boolean childOnline = getThing().getThings().stream() //
                .filter(t -> t.getThingTypeUID().equals(PROXY_DEVICE_THING_TYPE) && t.getStatus() == ThingStatus.ONLINE) //
                .findAny() //
                .isPresent();

        if (childOnline && !datagram.isRunning()) {
            try {
                datagram.start(SENSE_DATAGRAM_BCAST_PORT, datagramListenerThreadName);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                logger.warn("Unable to start datagram: {}", e.getLocalizedMessage());
            }
        }

        if (!childOnline && datagram.isRunning()) {
            datagram.stop();
        }
    }

    /*
     * forms and sends response on request for update on power for proxy device
     *
     * @param handler of proxy device
     * 
     * @param socketAddress of Sense monitor where power update should be sent
     */
    public boolean sendResponse(SenseEnergyProxyDeviceHandler handler, SocketAddress socketAddress) {
        SenseEnergyDatagramGetSysInfo getSysInfo = new SenseEnergyDatagramGetSysInfo();
        SenseEnergyDatagramGetRealtime getRealtime = new SenseEnergyDatagramGetRealtime();

        boolean shouldRespond = handler.formPowerResponse(getSysInfo, getRealtime);

        if (!shouldRespond) {
            return false;
        }

        try {
            this.datagram.sendResponse(socketAddress, getSysInfo, getRealtime);
        } catch (IOException e) {
            logger.debug("Unable to send datagram response: {}", e.getLocalizedMessage());
        }

        return true;
    }

    /**** Datagram listener functions *****/

    /*
     * handles request for responding to power queries from Sense device. Will limit the number of responses per request
     * so as not to overload the Sense monitor. Uses a round robin to ensure all devices have equal opportunity to
     * respond.
     */
    @Override
    public void requestReceived(SocketAddress socketAddress) {
        int maxResponses = MAX_RESPONSES_PER_REQUEST;

        // restart from beginning if RR has reached end
        if (!roundRobinIterator.hasNext() && !getThing().getThings().isEmpty()) {
            roundRobinIterator = getThing().getThings().iterator();
        }

        while (roundRobinIterator.hasNext() && maxResponses > 0) {
            if (roundRobinIterator.next().getHandler() instanceof SenseEnergyProxyDeviceHandler handler) {
                if (sendResponse(handler, socketAddress)) {
                    maxResponses--;
                }
            }
        }
    }

    /***** SenseEnergyeWSListener interfaces *****/

    @Override
    public void onWebSocketConnect() {
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("onWebSocketClose ({}), {}", statusCode, reason);
        try {
            webSocket.restartWithBackoff(getApi().getAccessToken());
        } catch (InterruptedException | ExecutionException | IOException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.warn("Exeception when restarting webSocket: {}", e.getMessage());
            // will retry at next heartbeat
        }
    }

    @Override
    public void onWebSocketError(String msg) {
        logger.debug("onWebSocketError {}", msg);
        // restart will occur on onWebSocketClose
    }

    @Override
    public void onWebSocketRealtimeUpdate(SenseEnergyWebSocketRealtimeUpdate update) {
        // message comes in MANY times a second, reduce frequency to be good citizen in openHAB
        countRealTimeUpdate = (countRealTimeUpdate == 0) ? 10 : countRealTimeUpdate - 1;
        if (countRealTimeUpdate != 0) {
            return;
        }

        if (solarConfigured) {
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_POTENTIAL_1, update.voltage[0], Units.VOLT);
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_POTENTIAL_2, update.voltage[1], Units.VOLT);

            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_MAIN_POWER, update.w, Units.WATT);
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_SOLAR_POWER, update.solarW, Units.WATT);

            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_FREQUENCY, update.hz, Units.HERTZ);
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GRID_POWER, update.gridW, Units.WATT);
        } else {
            logger.warn("Non solar configured monitors are not currently supported");
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_POTENTIAL_1, update.voltage[0], Units.VOLT);
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_POTENTIAL_2, update.voltage[1], Units.VOLT);

            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_FREQUENCY, update.hz, Units.HERTZ);
        }

        updateDiscoveredDevicesStatus(update.devices);
    }

    /*
     * updates the power state for discovered devices. sends trigger for devices which turn on/off. if necessary,
     * reconciles the channels if there are new discovered devices
     *
     * @param devices array retrieved in websocket update
     */
    public void updateDiscoveredDevicesStatus(SenseEnergyWebSocketDevice[] devices) {
        Set<String> updateDevicesOn = new HashSet<>();

        for (SenseEnergyWebSocketDevice device : devices) {
            // include in the "ON' devices - must be done before reconcileDeviceChannels to prevent recursive loop
            updateDevicesOn.add(device.id);

            // check if device channels need to be updated because there is a new device
            if (!senseDevices.containsKey(device.id)) {
                reconcileDiscoveredDeviceChannels(null);
            }

            DeviceType deviceType = senseDevicesType.getOrDefault(device.id, DeviceType.DISCOVERED_DEVICE);

            // Send trigger if device just turned on
            if (!this.devicesOn.contains(device.id)) {
                triggerChannel(makeDeviceChannelUID(deviceType, device.id, CHANNEL_DEVICE_TRIGGER), "ON");
                logger.trace("Discovered device turned on: {}({})", device.name, device.id);
            }

            ChannelUID channelUID = makeDeviceChannelUID(deviceType, device.id, CHANNEL_DEVICE_POWER);
            if (isLinked(channelUID)) {
                updateState(channelUID, new QuantityType<>(device.w, Units.WATT));
            }
        }

        // if was ON before and not ON now, update state to 0 and send trigger
        Set<String> wasOnNowOff = new HashSet<>(this.devicesOn);
        wasOnNowOff.removeAll(updateDevicesOn);
        for (String id : wasOnNowOff) {
            DeviceType deviceType = senseDevicesType.getOrDefault(id, DeviceType.DISCOVERED_DEVICE);
            updateState(makeDeviceChannelUID(deviceType, id, CHANNEL_DEVICE_POWER), new QuantityType<>(0, Units.WATT));
            triggerChannel(makeDeviceChannelUID(deviceType, id, CHANNEL_DEVICE_TRIGGER), "OFF");
            logger.trace("Discovered device turned off: {}", id);
        }
        this.devicesOn = updateDevicesOn;
    }

    @Nullable
    public SenseEnergyProxyDeviceHandler getProxyDeviceByMAC(String macAddress) {
        return getThing().getThings().stream() //
                .filter(t -> t.getThingTypeUID().equals(PROXY_DEVICE_THING_TYPE)) //
                .map(t -> (SenseEnergyProxyDeviceHandler) t.getHandler()) //
                .filter(Objects::nonNull) //
                .filter(h -> h.getMAC().equals(macAddress)) //
                .findFirst() //
                .orElse(null);
    }
}
