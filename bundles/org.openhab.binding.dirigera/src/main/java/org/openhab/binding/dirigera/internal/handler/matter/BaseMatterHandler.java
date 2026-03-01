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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.openhab.binding.dirigera.internal.Constants.*;
import static org.openhab.binding.dirigera.internal.interfaces.Model.*;
import static org.openhab.binding.dirigera.internal.model.MatterModel.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.GatewayException;
import org.openhab.binding.dirigera.internal.interfaces.BaseDevice;
import org.openhab.binding.dirigera.internal.interfaces.DebugHandler;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.binding.dirigera.internal.interfaces.PowerListener;
import org.openhab.binding.dirigera.internal.model.MatterModel;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BaseMatterHandler} for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BaseMatterHandler extends BaseThingHandler implements BaseDevice, DebugHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseMatterHandler.class);
    private final List<PowerListener> powerListeners = new ArrayList<>();
    private @Nullable Gateway gateway;

    // cache to handle each refresh command properly
    protected Map<String, MatterModel> deviceModelMap = new TreeMap<>();
    protected Map<String, State> channelStateMap = new HashMap<>();
    protected Map<String, LinkHandler> linkHandlerMap = new ConcurrentHashMap<>();

    protected BaseDeviceConfiguration config = new BaseDeviceConfiguration();
    protected State requestedPowerState = UnDefType.UNDEF;
    protected State currentPowerState = UnDefType.UNDEF;
    protected boolean customDebug = false;
    protected boolean disposed = true;
    protected boolean online = false;
    protected BaseDevice child;

    public BaseMatterHandler(final Thing thing) {
        super(thing);
        child = this;
    }

    /**
     * Set child handler for registration in gateway
     *
     * @param child as BaseDevice
     */
    protected void setChildHandler(BaseDevice child) {
        this.child = child;
    }

    @Override
    public void initialize() {
        Instant startTime = Instant.now();
        disposed = false;
        config = getConfigAs(BaseDeviceConfiguration.class);

        // first get bridge as Gateway
        if (!getGateway()) {
            return;
        }
        // check if thing type matches with model
        if (!checkHandler()) {
            // if handler doesn't match model status will be set to offline and it will stay until correction
            return;
        }

        configure();
        initializeCache();
        initLinks();
        JSONArray allDeviceUpadtes = initChannels();
        initThingProperties(allDeviceUpadtes);
        if (!linkHandlerMap.isEmpty()) {
            gateway().updateLinks();
        }
        logger.trace("{} initialize took {} ms", thing.getLabel(),
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * Setup the model for this device. Collect relations and build the model based on device type and id
     */
    protected void configure() {
        // check if device is already configured
        if (deviceModelMap.isEmpty()) {
            String relationId = gateway().model().getRelationId(config.id);
            if (config.id.equals(relationId)) {
                String deviceType = gateway().model().getDeviceType(config.id);
                deviceModelMap.put(config.id, new MatterModel(config.id, deviceType));
            } else {
                Map<String, String> connectedDevices = gateway().model().getRelations(relationId);
                connectedDevices.forEach((key, value) -> {
                    deviceModelMap.put(key, new MatterModel(key, value));
                });
            }
        }
    }

    /**
     * Initialize state cache for all mapped properties and channels
     */
    private void initializeCache() {
        deviceModelMap.forEach((deviceId, deviceModel) -> {
            deviceModel.getStatusProperties().forEach((key, value) -> {
                channelStateMap.put(value.getString(MatterModel.CHANNEL_KEY_CHANNEL_NAME), UnDefType.UNDEF);
            });
        });
    }

    /**
     * Get the current status with all attributes of this device and build channels according to the present attributes
     *
     * @return updates for all related devices as JSONArray for reuse
     */
    private JSONArray initChannels() {
        JSONArray allUpdates = new JSONArray();
        // ensure all devices got an update
        deviceModelMap.forEach((deviceId, deviceModel) -> {
            JSONObject deviceUpdate = gateway().api().readDevice(deviceId);
            allUpdates.put(deviceUpdate);
            createChannels(deviceUpdate);
            gateway().registerDevice(child, deviceId);
            handleUpdate(deviceUpdate);
        });
        return allUpdates;
    }

    /**
     * Initialization of link channels with link candidate types
     */
    private void initLinks() {
        deviceModelMap.forEach((deviceId, deviceModel) -> {
            List<String> candidates = deviceModel.getLinkCandidates();
            if (!candidates.isEmpty()) {
                createChannelIfNecessary(CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
                createChannelIfNecessary(CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES, CoreItemFactory.STRING);
                linkHandlerMap.put(deviceId, new LinkHandler(this, deviceId, candidates));
            }
        });
    }

    /**
     * Initialize thing properties from all updates
     *
     * @param deviceUpdates as JSONArray for all related devices
     */
    private void initThingProperties(JSONArray deviceUpdates) {
        Map<String, String> properties = editProperties();
        deviceModelMap.forEach((deviceId, deviceModel) -> {
            deviceUpdates.forEach(update -> {
                JSONObject updateJson = (JSONObject) update;
                if (deviceId.equals(updateJson.optString(JSON_KEY_DEVICE_ID))) {
                    properties.putAll(deviceModel.getThingProperties(updateJson));
                }
            });
        });
        updateProperties(properties);
    }

    /**
     * Create channels according to present attributes
     *
     * @param deviceStatus as JSONObject
     */
    private void createChannels(JSONObject deviceStatus) {
        if (deviceStatus.has(JSON_KEY_ATTRIBUTES)) {
            JSONObject attributes = deviceStatus.getJSONObject(JSON_KEY_ATTRIBUTES);
            deviceModelMap.forEach((deviceId, deviceModel) -> {
                deviceModel.getStatusProperties().forEach((statusPropertyKey, statusPropertyJson) -> {
                    String deviceAttribute = statusPropertyJson.getString(CHANNEL_KEY_ATTRIBUTE);
                    if (attributes.has(deviceAttribute)) {
                        createChannelIfNecessary(statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_NAME),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_TYPE),
                                statusPropertyJson.optString(CHANNEL_KEY_ITEM_TYPE),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_LABEL),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_DESCRIPTION));
                    }
                });
            });
        }
    }

    /**
     * Get Gateway from Bridge of this Thing
     *
     * @return true if Gateway is available, false otherwise
     */
    protected boolean getGateway() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
                    logger.warn("{} found Gateway status {}", thing.getLabel(), gw.getThing().getStatus());
                    gateway = gw;
                    return true;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/dirigera.device.status.wrong-bridge-type");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/dirigera.device.missing-bridge-handler");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/dirigera.device.status.missing-bridge");
        }
        return false;
    }

    /**
     * Handle commands from openHAB
     * - RefreshType: update from cache
     * - links handled by LinkHandler if available
     * - other commands: build API requests from device model and send them
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (customDebug) {
            logger.info("{} handleCommand channel {} command {} {}", thing.getLabel(), channelUID.getAsString(),
                    command.toFullString(), command.getClass());
        }
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        } else {
            linkHandlerMap.forEach((deviceId, linkHandler) -> {
                linkHandler.handleCommand(channelUID, command);
            });
            String targetChannel = channelUID.getIdWithoutGroup();
            switch (targetChannel) {
                case CHANNEL_POWER_STATE:
                    if (command instanceof OnOffType onOff) {
                        // store requested power state to identify if power change was requested by OH or came from
                        // outside
                        requestedPowerState = onOff;
                    }
                    break;
            }
            Map<String, JSONObject> requests = new HashMap<>();
            deviceModelMap.forEach((deviceId, deviceModel) -> {
                Map<String, JSONObject> deviceUpdates = deviceModel.getRequestJson(targetChannel, command);
                requests.putAll(deviceUpdates);
            });

            if (!requests.isEmpty()) {
                requests.forEach((deviceId, request) -> {
                    sendPatch(deviceId, request);
                });
            } else {
                logger.debug("{} no API request for channel {} command {}", thing.getLabel(), targetChannel,
                        command.toFullString());
            }
        }
    }

    /**
     * Send PATCH to DIRIGERA API
     *
     * @param deviceId as String
     * @param patch as JSONObject
     * @return HTTP status code
     */
    protected int sendPatch(String deviceId, JSONObject patch) {
        int status = gateway().api().sendPatch(deviceId, patch);
        logger.debug("{} API call: Status {} payload {}", thing.getLabel(), status, patch);
        return status;
    }

    /**
     * Handle updates from attributes
     * - check reachable flag to determine ONLINE/OFFLINE status
     * - forward link update to LinkHandler if available
     * - check attributes for updates and device model will forward them to channels
     *
     * @param deviceUpdate as JSONObject
     */
    @Override
    public void handleUpdate(JSONObject deviceUpdate) {
        if (customDebug) {
            logger.info("{} handleUpdate JSON {}", thing.getLabel(), deviceUpdate);
        }
        // check reachable flag to determine ONLINE/OFFLINE status
        if (deviceUpdate.has(Model.JSON_KEY_REACHABLE)) {
            if (deviceUpdate.getBoolean(Model.JSON_KEY_REACHABLE)) {
                updateStatus(ThingStatus.ONLINE);
                online = true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.device.status.not-reachable");
                online = false;
            }
        }
        linkHandlerMap.forEach((deviceId, linkHandler) -> {
            if (deviceId.equals(deviceUpdate.optString(JSON_KEY_DEVICE_ID))) {
                linkHandler.handleUpdate(deviceUpdate);
            }
        });
        // now check attributes for updates
        Map<String, State> stateUpdates = new HashMap<>();
        deviceModelMap.forEach((deviceId, deviceModel) -> {
            Map<String, State> deviceUpdates = deviceModel.getAttributeUpdates(deviceUpdate);
            stateUpdates.putAll(deviceUpdates);
        });
        logger.trace("{} attribute updates {}", thing.getLabel(), stateUpdates);
        stateUpdates.forEach((channel, state) -> {
            if (UnDefType.NULL.equals(state)) {
                logger.debug("{} ignoring NULL state for channel {}", thing.getLabel(), channel);
            } else {
                updateState(new ChannelUID(thing.getUID(), channel), state);
            }
        });
    }

    /**
     * Create channel if not already present
     *
     * @param channelId as String
     * @param channelTypeUID as String - needs to present in channel-types.xml
     * @param itemType as String
     * @param label as String
     * @param description as String
     */
    protected synchronized void createChannelIfNecessary(String channelId, String channelTypeUID, String itemType,
            String label, String description) {
        if (thing.getChannel(channelId) == null) {
            if (customDebug) {
                logger.info("{} create Channel {} {} {}", thing.getLabel(), channelId, channelTypeUID, itemType);
            }
            // https://www.openhab.org/docs/developer/bindings/#updating-the-thing-structure
            ThingBuilder thingBuilder = editThing();

            // check for system type or binding type
            ChannelTypeUID channelType;
            String[] channelTypeParts = channelTypeUID.split("\\.");
            if (channelTypeParts.length > 1) {
                channelType = new ChannelTypeUID(channelTypeParts[0], channelTypeParts[1]);
            } else {
                channelType = new ChannelTypeUID(BINDING_ID, channelTypeParts[0]);
            }

            // check for trigger channel without item as trigger channel
            ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId))
                    .withType(channelType);
            if (!label.isBlank()) {
                channelBuilder = channelBuilder.withLabel(label);
            }
            if (!description.isBlank()) {
                channelBuilder = channelBuilder.withDescription(description);
            }
            if (!itemType.isBlank()) {
                channelBuilder = channelBuilder.withAcceptedItemType(itemType);
            } else {
                channelBuilder = channelBuilder.withKind(ChannelKind.TRIGGER);
            }
            updateThing(thingBuilder.withChannel(channelBuilder.build()).build());
        }
    }

    /**
     * Create channel if not already present without label and description
     *
     * @param channelId as String
     * @param channelTypeUID as String - needs to present in channel-types.xml
     * @param itemType as String
     */
    protected synchronized void createChannelIfNecessary(String channelId, String channelTypeUID, String itemType) {
        createChannelIfNecessary(channelId, channelTypeUID, itemType, "", "");
    }

    /**
     * Check if device is powered ON
     *
     * @return true if powered ON, false otherwise
     */
    protected boolean isPowered() {
        return OnOffType.ON.equals(currentPowerState) && online;
    }

    /**
     * Wrapper for updateState to to store values in cache and special treatment for power state
     */
    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        // special treatment power - inform listeners about power state and check if it was request by OH or it came
        // from outside
        if (CHANNEL_POWER_STATE.equals(channelUID.getIdWithoutGroup())) {
            currentPowerState = state;
            synchronized (powerListeners) {
                if (online) {
                    boolean requested = currentPowerState.equals(requestedPowerState);
                    powerListeners.forEach(listener -> {
                        listener.powerChanged((OnOffType) currentPowerState, requested);
                    });
                    requestedPowerState = UnDefType.UNDEF;
                }
            }
        }
        channelStateMap.put(channelUID.getIdWithoutGroup(), state);
        if (!disposed) {
            if (customDebug) {
                logger.info("{} updateState {} {}", thing.getLabel(), channelUID, state);
            }
            super.updateState(channelUID, state);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        online = false;
        gateway().unregisterDevice(child, config.id);
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        gateway().deleteDevice(child, config.id);
        super.handleRemoval();
    }

    /**
     * Get Gateway connected to this device
     *
     * @return Gateway object, throws GatewayException if not available
     */
    public Gateway gateway() {
        Gateway gwlocalGateway = gateway;
        if (gwlocalGateway != null) {
            return gwlocalGateway;
        } else {
            throw new GatewayException(thing.getUID() + " has no Gateway defined");
        }
    }

    /**
     * Handler sanity check if thing type matches with model. Devices added by discovery will have correct type, but
     * errors can happen if textual configuration of thing type uid doesn't match with with configured device id
     *
     * @ return true if thing type matches with model, false otherwise
     */
    @Override
    public boolean checkHandler() {
        ThingTypeUID modelTTUID = gateway().model().identifyDeviceFromModel(config.id);
        if (!thing.getThingTypeUID().equals(modelTTUID)) {
            // check if id is present in model
            if (THING_TYPE_NOT_FOUND.equals(modelTTUID)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "@text/dirigera.device.status.id-not-found" + " [\"" + config.id + "\"]");
            } else {
                // String message = "Handler " + thing.getThingTypeUID() + " doesn't match with model " + modelTTUID;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/dirigera.device.status.ttuid-mismatch" + " [\"" + thing.getThingTypeUID() + "\",\""
                                + modelTTUID + "\"]");
            }
            return false;
        }
        return true;
    }

    /**
     * Evaluates if this device is a controller or sensor
     *
     * @return boolean
     */
    protected boolean isControllerOrSensor() {
        for (MatterModel deviceModel : deviceModelMap.values()) {
            if (TYPE_SENSOR.equalsIgnoreCase(deviceModel.getType())
                    || deviceModel.getDeviceType().contains(TYPE_CONTROLLER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if device or relations has the given type
     *
     * @param queryDeviceType
     * @return true if type is present, false otherwise
     */
    protected boolean hasType(String queryDeviceType) {
        for (MatterModel deviceModel : deviceModelMap.values()) {
            if (deviceModel.getDeviceType().equals(queryDeviceType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all device ids for the given device type
     *
     * @param queryDeviceType
     * @return List of device ids which are matching the query type
     */
    protected List<String> getIdsFor(String queryDeviceType) {
        return deviceModelMap.entrySet().stream()
                .filter(entry -> entry.getValue().getDeviceType().equals(queryDeviceType)).map(Map.Entry::getKey)
                .toList();
    }

    /**
     * ## Link handling forwarded to LinkHandler if available
     */

    /**
     * Update cycle of links starts, no calls in between until updateLinksDone
     */
    @Override
    public void updateLinksStart() {
        linkHandlerMap.forEach((deviceId, linkHandler) -> linkHandler.updateLinksStart());
    }

    /**
     * Get real links from device updates. Delivers a copy due to concurrent access.
     *
     * @return links attached to this device
     */
    @Override
    public List<String> getLinks() {
        List<String> links = new ArrayList<>();
        linkHandlerMap.forEach((deviceId, linkHandler) -> {
            linkHandler.getLinks().forEach(link -> {
                if (!links.contains(link)) {
                    links.add(link);
                }
            });
        });
        return links;
    }

    /**
     * Adds a soft link towards the device which has the link stored in his attributes
     *
     * @param device id of the device which contains this link
     */
    @Override
    public void addSoftlink(String linkSourceId, String linkTargetId) {
        LinkHandler linkHandler = linkHandlerMap.get(linkTargetId);
        if (linkHandler != null) {
            linkHandler.addSoftlink(linkSourceId);
        }
    }

    /**
     * Collect all informations from link handlers, combine them and update state and command options
     */
    @Override
    public void updateLinksDone() {
        List<CommandOption> linkCommandOptions = new ArrayList<>();
        List<CommandOption> linkCandidateCommandOptions = new ArrayList<>();
        linkHandlerMap.forEach((deviceId, linkHandler) -> {
            merge(linkHandler.getLinkOptions(), linkCommandOptions);
            merge(linkHandler.getCandidateOptions(), linkCandidateCommandOptions);
        });

        ChannelUID linkChannelUUID = new ChannelUID(getThing().getUID(), CHANNEL_LINKS);
        gateway().getCommandProvider().setCommandOptions(linkChannelUUID, linkCommandOptions);
        String links = linkCommandOptions.stream().map(CommandOption::getLabel).toList().toString();
        logger.trace("{} links {}", getThing().getLabel(), links);
        updateState(linkChannelUUID, StringType.valueOf(links));

        ChannelUID candidateChannelUUID = new ChannelUID(getThing().getUID(), CHANNEL_LINK_CANDIDATES);
        gateway().getCommandProvider().setCommandOptions(candidateChannelUUID, linkCandidateCommandOptions);
        String candidates = linkCandidateCommandOptions.stream().map(CommandOption::getLabel).toList().toString();
        updateState(candidateChannelUUID, StringType.valueOf(candidates));
    }

    private void merge(List<CommandOption> source, List<CommandOption> target) {
        source.forEach(commandOption -> {
            String label = commandOption.getLabel();
            if (!target.contains(commandOption) && label != null) {
                target.add(commandOption);
            }
        });
    }

    public void addPowerListener(PowerListener listener) {
        synchronized (powerListeners) {
            powerListeners.add(listener);
        }
    }

    public void removePowerListener(PowerListener listener) {
        synchronized (powerListeners) {
            powerListeners.remove(listener);
        }
    }

    /**
     * ### Debug commands for console access
     */

    @Override
    public String getJSON() {
        if (THING_TYPE_SCENE.equals(thing.getThingTypeUID())) {
            return gateway().api().readScene(config.id).toString();
        } else {
            return gateway().api().readDevice(config.id).toString();
        }
    }

    @Override
    public String getToken() {
        return gateway().getToken();
    }

    @Override
    public void setDebug(boolean debug, boolean all) {
        if (all) {
            ((DebugHandler) gateway()).setDebug(debug, all);
        } else {
            customDebug = debug;
        }
    }

    @Override
    public String getDeviceId() {
        return config.id;
    }

    @Override
    @Nullable
    public ThingHandlerCallback getCallback() {
        return super.getCallback();
    }
}
