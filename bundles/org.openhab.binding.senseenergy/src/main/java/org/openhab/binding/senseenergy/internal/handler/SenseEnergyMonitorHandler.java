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
import java.io.InterruptedIOException;
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
import java.util.concurrent.TimeoutException;
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

    static final private int MAX_RESPONSES_PER_REQUEST = 5;
    static final private int SENSE_DATAGRAM_BCAST_PORT = 9999;

    static final private String CHANNEL_PROPERTY_ID = "sense-id";
    static final private String CHANNEL_PROPERTY_LABEL = "sense-label";

    private long id;

    @Nullable
    private SenseEnergyApiMonitorStatus apiMonitorStatus;

    private SenseEnergyWebSocket webSocket;
    private SenseEnergyDatagram datagram;

    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    private Map<String, SenseEnergyApiDevice> senseDiscoveredDevices = Collections.emptyMap();
    private Set<String> discoveredDevicesOn = Collections.emptySet();
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
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            return;
        }

        if (!checkBridgeStatus()) {
            return;
        }

        SenseEnergyApiMonitor apiMonitor;
        try {
            if ((apiMonitor = getApi().getMonitorOverview(id)) == null) {
                return;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | SenseEnergyApiException e) {
            handleApiException(e);
            return;
        }

        this.solarConfigured = apiMonitor.solarConfigured;

        try {
            apiMonitorStatus = getApi().getMonitorStatus(id);
            senseDiscoveredDevices = getApi().getDevices(id);
        } catch (InterruptedException | TimeoutException | ExecutionException | SenseEnergyApiException e) {
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
        } catch (Exception e) {
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

        logger.debug("SenseEnergyMonitorHandler: heartbeat");
        try {
            senseDiscoveredDevices = getApi().getDevices(id);
        } catch (InterruptedException | TimeoutException | ExecutionException | SenseEnergyApiException e) {
            handleApiException(e);
            return;
        }
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
            if (apiException.isConfigurationIssue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        apiException.getLocalizedMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        apiException.getLocalizedMessage());
            }
        } else if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedIOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof TimeoutException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof ExecutionException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else {
            // capture in log since this is an unexpected exception
            logger.warn("Unhandled Exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.getLocalizedMessage());
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channelGroup = channelUID.getGroupId();

            if (channelGroup == null) {
                logger.debug("Channel does not have a group ID: {}", channelUID);
                return;
            }

            if (channelGroup.equals(CHANNEL_GROUP_DEVICES)) {
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

                if (!discoveredDevicesOn.contains(senseID)) {
                    updateState(channelUID, new QuantityType<>(0, Units.WATT));
                }
            }
        }
    }

    public SenseEnergyApi getApi() {
        Bridge bridge = Objects.requireNonNull(getBridge());
        SenseEnergyBridgeHandler handler = (SenseEnergyBridgeHandler) Objects.requireNonNull(bridge.getHandler());

        return handler.getApi();
    }

    /*
     * reconciles the discovered device channels to stay in sync with the discovered device list
     *
     * @param thingBuilder to update if already editing, otherwise will open
     */
    public void reconcileDiscoveredDeviceChannels(@Nullable ThingBuilder thingBuilder) {
        ChannelGroupType channelGroupType = Objects
                .requireNonNull(channelGroupTypeRegistry.getChannelGroupType(CHANNEL_GROUP_TYPE_DEVICE_TEMPLATE));
        List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
        boolean channelsUpdated = false;

        Set<String> senseIDs = new HashSet<>(senseDiscoveredDevices.keySet());
        senseIDs.remove("solar"); // don't create solar as a separate channel

        logger.trace("Reconciling channels with Sense device, channel count: {}", senseIDs.size());

        // reconcile every channel type that is in the group template
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = Objects
                    .requireNonNull(channelTypeRegistry.getChannelType(channelDefinition.getChannelTypeUID()));
            // create set of IDs with existing thing channels
            // @formatter:off
            // note @Nullable attribute seems unnecessary here, but had to include to prevent mismatch error when
            // built in maven
            // java.util.stream.@NonNull Collector<? super java.lang.@Nullable String,java.lang.Object,java.util.@NonNull Set<java.lang.@NonNull String>> - required
            // java.util.stream.@NonNull Collector<java.lang.@NonNull String,capture#of ?,java.util.@NonNull Set<java.lang.@NonNull String>>
            Set<@Nullable String> existingChannels = getThing().getChannelsOfGroup(CHANNEL_GROUP_DEVICES).stream()
                    .filter(ch -> Objects.requireNonNull(ch.getChannelTypeUID()).equals(channelType.getUID()))
                    .map(ch -> ch.getProperties().get(CHANNEL_PROPERTY_ID))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // @formatter:on

            // make a super-set of senseIDs for existing and devices from last API call to iterate over
            Set<String> allChannels = new HashSet<>(existingChannels);
            allChannels.addAll(senseIDs);

            ThingBuilder localBuilder = (thingBuilder != null) ? thingBuilder : editThing();

            // if channel already exists AND is no longer reported by sense --> remove
            // if channel already exists AND is still reported by sense --> update
            // if channel does not currently exist and is reported by sense --> add
            for (String senseID : allChannels) {
                ChannelUID channelUID = makeDiscoveredDeviceChannelUID(senseID, channelDefinition.getId());
                if (existingChannels.contains(senseID)) {
                    if (!senseIDs.contains(senseID)) { // need to remove
                        localBuilder.withoutChannel(channelUID);
                        channelsUpdated = true;
                    } else { // update label/etc as necessary
                        channelsUpdated = channelsUpdated || updateDiscoveredChannel(localBuilder, channelUID,
                                Objects.requireNonNull(senseDiscoveredDevices.get(senseID)).name);
                    }
                } else { // need to add
                    addDiscoveredChannel(localBuilder, channelUID,
                            Objects.requireNonNull(senseDiscoveredDevices.get(senseID)), channelDefinition,
                            channelType);
                    channelsUpdated = true;
                }
            }

            if (thingBuilder == null) {
                updateThing(localBuilder.build());
            }

            if (channelsUpdated) {
                triggerChannel(
                        new ChannelUID(getThing().getUID(), CHANNEL_GROUP_GENERAL, CHANNEL_DISCOVERED_DEVICES_UPDATED));
            }
        }
    }

    public void addDiscoveredChannel(ThingBuilder builder, ChannelUID channelUID, SenseEnergyApiDevice apiDevice,
            ChannelDefinition channelDefinition, ChannelType channelType) {
        // @formatter:off
        Channel channel = ChannelBuilder.create(channelUID)
                .withDescription(Objects.requireNonNull(channelDefinition.getDescription()))
                .withLabel(apiDevice.name + ": " + channelDefinition.getLabel())
                .withProperties(Map.of(CHANNEL_PROPERTY_ID, apiDevice.id, CHANNEL_PROPERTY_LABEL, apiDevice.name))
                .withAcceptedItemType(channelType.getItemType())
                .withType(channelDefinition.getChannelTypeUID())
                .withKind(channelType.getKind())
                .withDefaultTags(channelType.getTags())
                .build();
        // @formatter:on
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
     */
    public boolean updateDiscoveredChannel(ThingBuilder thingBuilder, ChannelUID channelUID, String label) {
        if (getThing().getChannel(channelUID) instanceof Channel channel) {
            Map<String, String> properties = channel.getProperties();

            String currentLabel = properties.get(CHANNEL_PROPERTY_LABEL);
            if (currentLabel != null && currentLabel.equals(label)) {
                return false;
            }

            Map<String, String> newProperties = new HashMap<String, String>(properties);

            thingBuilder.withoutChannel(channelUID);

            newProperties.put(CHANNEL_PROPERTY_LABEL, label);
            Channel newChannel = ChannelBuilder.create(channel).withProperties(newProperties).withLabel(label).build();

            thingBuilder.withChannel(newChannel);

            return true;
        }

        return false;
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
    public ChannelUID makeDiscoveredDeviceChannelUID(String senseID, String channelID) {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(getThing().getUID(), CHANNEL_GROUP_DEVICES);

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

    public void checkDatagramStatus() {
        final String datagramListenerThreadName = "OH-binding-" + getThing().getUID().getAsString();

        // @formatter:off
        boolean childOnline = getThing().getThings().stream()
                .filter(t -> t.getThingTypeUID().equals(PROXY_DEVICE_THING_TYPE) && t.getStatus() == ThingStatus.ONLINE)
                .findAny()
                .isPresent();
        // @formatter:on

        if (childOnline && !datagram.isRunning()) {
            datagram.stop();
            try {
                datagram.start(SENSE_DATAGRAM_BCAST_PORT, datagramListenerThreadName);
            } catch (IOException e) {
                handleApiException(e);
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

    @Override
    public void messageReceived(byte[] message) {
    }

    /***** SenseEnergyeWSListener interfaces *****/

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("onWebSocketClose ({}), {}", statusCode, reason);
        // will restart on heartbeat
    }

    @Override
    public void onWebSocketError(String msg) {
        // no action - let heartbeat restart webSocket
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
            // include in the "ON' devices - must be done before reconcileDiscoveredDeviceChannels to prevent recursive
            // loop
            updateDevicesOn.add(device.id);

            // check if device channels need to be updated because there is a new device
            if (!senseDiscoveredDevices.containsKey(device.id)) {
                reconcileDiscoveredDeviceChannels(null);
            }

            // Send trigger if device just turned on
            if (!this.discoveredDevicesOn.contains(device.id)) {
                triggerChannel(makeDiscoveredDeviceChannelUID(device.id, CHANNEL_DEVICE_TRIGGER), "ON");
                logger.trace("Discovered device turned on: {}({})", device.name, device.id);
            }

            ChannelUID channelUID = makeDiscoveredDeviceChannelUID(device.id, CHANNEL_DEVICE_POWER);
            if (isLinked(channelUID)) {
                updateState(channelUID, new QuantityType<>(device.w, Units.WATT));
            }
        }

        // if was ON before and not ON now, update state to 0 and send trigger
        Set<String> wasOnNowOff = new HashSet<>(this.discoveredDevicesOn);
        wasOnNowOff.removeAll(updateDevicesOn);
        for (String id : wasOnNowOff) {
            updateState(makeDiscoveredDeviceChannelUID(id, CHANNEL_DEVICE_POWER), new QuantityType<>(0, Units.WATT));
            triggerChannel(makeDiscoveredDeviceChannelUID(id, CHANNEL_DEVICE_TRIGGER), "OFF");
            logger.trace("Discovered device turned off: {}", id);
        }
        this.discoveredDevicesOn = updateDevicesOn;
    }
}
