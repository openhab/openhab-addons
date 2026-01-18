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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private List<PowerListener> powerListeners = new ArrayList<>();
    private @Nullable Gateway gateway;

    // cache to handle each refresh command properly
    protected Map<String, MatterModel> deviceConfigMap = new TreeMap<>();
    protected Map<String, State> channelStateMap = new HashMap<>();

    /*
     * hardlinks initialized with invalid links because the first update shall trigger a link update. If it's declared
     * as empty no link update will be triggered. This is necessary for startup phase.
     */
    protected List<String> hardLinks = new ArrayList<>(Arrays.asList("undef"));
    protected List<String> softLinks = new ArrayList<>();
    protected final List<String> linkCandidateTypes = new CopyOnWriteArrayList<>();

    protected BaseDeviceConfiguration config = new BaseDeviceConfiguration();
    protected State requestedPowerState = UnDefType.UNDEF;
    protected State currentPowerState = UnDefType.UNDEF;
    protected boolean customDebug = true;
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
        JSONArray allDeviceUpadtes = initChannels();
        initLinks();
        initThingProperties(allDeviceUpadtes);
        logger.trace("BaseMatterHandler initialize took {} ms", Duration.between(startTime, Instant.now()).toMillis());
    }

    protected void configure() {
        // check if device is already configured
        if (deviceConfigMap.isEmpty()) {
            String relationId = gateway().model().getRelationId(config.id);
            if (config.id.equals(relationId)) {
                String deviceType = gateway().model().getDeviceType(config.id);
                deviceConfigMap.put(config.id, new MatterModel(config.id, deviceType));
            } else {
                Map<String, String> connectedDevices = gateway().model().getRelations(relationId);
                connectedDevices.forEach((key, value) -> {
                    deviceConfigMap.put(key, new MatterModel(key, value));
                });
            }
        }
    }

    /**
     * Initialize state cache for all mapped properties
     *
     * @param properties array with channel definitions
     */
    private void initializeCache() {
        deviceConfigMap.forEach((deviceId, matterConfig) -> {
            matterConfig.getStatusProperties().forEach((key, value) -> {
                channelStateMap.put(value.getString(MatterModel.CHANNEL_KEY_CHANNEL_NAME), UnDefType.UNDEF);
            });
        });
    }

    private JSONArray initChannels() {
        JSONArray allUpdates = new JSONArray();
        deviceConfigMap.forEach((deviceId, config) -> {
            JSONObject deviceUpdate = gateway().api().readDevice(deviceId);
            allUpdates.put(deviceUpdate);
            createChannels(deviceUpdate);
            gateway().registerDevice(child, deviceId);
            handleUpdate(deviceUpdate);
        });
        return allUpdates;
    }

    private void initLinks() {
        deviceConfigMap.forEach((deviceId, config) -> {
            List<String> candidates = config.getLinkCandidates();
            if (!candidates.isEmpty()) {
                createChannelIfNecessary(CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
                createChannelIfNecessary(CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES, CoreItemFactory.STRING);
            }
            linkCandidateTypes.addAll(candidates);
        });
    }

    private void initThingProperties(JSONArray updates) {
        Map<String, String> properties = editProperties();
        deviceConfigMap.forEach((deviceId, config) -> {
            updates.forEach(update -> {
                JSONObject updateJson = (JSONObject) update;
                if (deviceId.equals(updateJson.optString(JSON_KEY_DEVICE_ID))) {
                    properties.putAll(config.getThingProperties(updateJson));
                }
            });
        });
        updateProperties(properties);
    }

    private void createChannels(JSONObject values) {
        if (values.has(JSON_KEY_ATTRIBUTES)) {
            JSONObject attributes = values.getJSONObject(JSON_KEY_ATTRIBUTES);
            deviceConfigMap.forEach((deviceId, matterConfig) -> {
                matterConfig.getStatusProperties().forEach((statusPropertyKey, statusPropertyJson) -> {
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

    protected boolean getGateway() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
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
     * Handling of basic commands which are the same for many devices
     * - RefreshType for all channels
     * - Startup behavior for lights and plugs
     * - Power state for lights and plugs
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (customDebug) {
            logger.info("DIRIGERA {} handleCommand channel {} command {} {}", thing.getUID(), channelUID.getAsString(),
                    command.toFullString(), command.getClass());
        }
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        } else {
            String targetChannel = channelUID.getIdWithoutGroup();
            // some special treatments for poweer and links
            switch (targetChannel) {
                case CHANNEL_POWER_STATE:
                    if (command instanceof OnOffType onOff) {
                        requestedPowerState = onOff;
                    }
                    break;
                case CHANNEL_LINKS:
                    if (customDebug) {
                        logger.info("DIRIGERA BASE_MATTER_HANDLER {} remove connection {}", thing.getLabel(),
                                command.toFullString());
                    }
                    if (command instanceof StringType string) {
                        linkUpdate(string.toFullString(), false);
                    }
                    break;
                case CHANNEL_LINK_CANDIDATES:
                    if (customDebug) {
                        logger.info("DIRIGERA BASE_MATTER_HANDLER {} add link {}", thing.getLabel(),
                                command.toFullString());
                    }
                    if (command instanceof StringType string) {
                        linkUpdate(string.toFullString(), true);
                    }
                    break;
            }
            Map<String, JSONObject> requests = new HashMap<>();
            deviceConfigMap.forEach((deviceId, matterConfig) -> {
                Map<String, JSONObject> deviceUpdates = matterConfig.getRequestJson(targetChannel, command);
                requests.putAll(deviceUpdates);
            });

            if (!requests.isEmpty()) {
                requests.forEach((deviceId, request) -> {
                    sendPatch(deviceId, request);
                });
            } else {
                logger.debug("DIRIGERA BASE_MATTER_HANDLER {} no API request for channel {} command {}", thing.getUID(),
                        targetChannel, command.toFullString());
            }
        }
    }

    /**
     * Wrapper function to respect customDebug flag
     *
     * @param attributes
     * @return status
     */
    protected int sendPatch(String deviceId, JSONObject patch) {
        int status = gateway().api().sendPatch(deviceId, patch);
        if (customDebug) {
            logger.info("DIRIGERA BASE_MATTER_HANDLER {} API call: Status {} payload {}", thing.getUID(), status,
                    patch);
        }
        return status;
    }

    /**
     * Handle updates from attributes
     *
     * @param update JSON
     */
    @Override
    public void handleUpdate(JSONObject update) {
        if (customDebug) {
            logger.info("DIRIGERA BASE_MATTER_HANDLER {} handleUpdate JSON {}", thing.getUID(), update);
        }
        // check reachable flag to determine ONLINE/OFFLINE status
        if (update.has(Model.JSON_KEY_REACHABLE)) {
            if (update.getBoolean(Model.JSON_KEY_REACHABLE)) {
                updateStatus(ThingStatus.ONLINE);
                online = true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.device.status.not-reachable");
                online = false;
            }
        }
        // check if links has changed
        if (update.has(ATTRIBUTES_KEY_REMOTE_LINKS)) {
            JSONArray remoteLinks = update.getJSONArray(ATTRIBUTES_KEY_REMOTE_LINKS);
            List<String> updateList = new ArrayList<>();
            remoteLinks.forEach(link -> {
                updateList.add(link.toString());
            });
            Collections.sort(updateList);
            Collections.sort(hardLinks);
            if (!hardLinks.equals(updateList)) {
                hardLinks = updateList;
                // just update internal link list and let the gateway update do all updates regarding soft links
                gateway().updateLinks();
            }
        }
        // now check attributes for updates
        Map<String, State> updates = new HashMap<>();
        deviceConfigMap.forEach((deviceId, matterConfig) -> {
            Map<String, State> deviceUpdates = matterConfig.getAttributeUpdates(update);
            updates.putAll(deviceUpdates);
        });
        logger.trace("DIRIGERA BASE_MATTER_HANDLER {} attribute updates {}", thing.getUID(), updates);
        updates.forEach((channel, state) -> {
            if (UnDefType.NULL.equals(state)) {
                logger.debug("DIRIGERA BASE_MATTER_HANDLER {} ignoring NULL state for channel {}", thing.getUID(),
                        channel);
            } else {
                updateState(new ChannelUID(thing.getUID(), channel), state);
            }
        });
    }

    protected synchronized void createChannelIfNecessary(String channelId, String channelTypeUID, String itemType,
            String label, String description) {
        if (thing.getChannel(channelId) == null) {
            if (customDebug) {
                logger.info("DIRIGERA BASE_MATTER_HANDLER {} create Channel {} {} {}", thing.getUID(), channelId,
                        channelTypeUID, itemType);
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

    protected synchronized void createChannelIfNecessary(String channelId, String channelTypeUID, String itemType) {
        createChannelIfNecessary(channelId, channelTypeUID, itemType, "", "");
    }

    protected boolean isPowered() {
        return OnOffType.ON.equals(currentPowerState) && online;
    }

    /**
     * Update cache for refresh, then update state
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
                logger.info("DIRIGERA {} updateState {} {}", thing.getUID(), channelUID, state);
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

    public Gateway gateway() {
        Gateway gwlocalGateway = gateway;
        if (gwlocalGateway != null) {
            return gwlocalGateway;
        } else {
            throw new GatewayException(thing.getUID() + " has no Gateway defined");
        }
    }

    @Override
    public boolean checkHandler() {
        // cross check if configured thing type is matching with the model
        // if handler is taken from discovery this will do no harm
        // but if it's created manually mismatch can happen
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
        for (MatterModel matterConfig : deviceConfigMap.values()) {
            if (matterConfig.getDeviceType().contains(TYPE_SENSOR)
                    || matterConfig.getDeviceType().contains(TYPE_CONTROLLER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update cycle of gateway is done
     */
    @Override
    public void updateLinksStart() {
        softLinks.clear();
    }

    /**
     * Get real links from device updates. Delivers a copy due to concurrent access.
     *
     * @return links attached to this device
     */
    @Override
    public List<String> getLinks() {
        return new ArrayList<String>(hardLinks);
    }

    private void linkUpdate(String linkedDeviceId, boolean add) {
        /**
         * link has to be set to target device like light or outlet, not to the device which triggers an action like
         * lightController or motionSensor
         */
        String targetDevice = "";
        String triggerDevice = "";
        List<String> linksToSend = new ArrayList<>();
        if (isControllerOrSensor()) {
            // request needs to be sent to target device
            targetDevice = linkedDeviceId;
            triggerDevice = config.id;
            // get current links
            JSONObject deviceData = gateway().model().getAllFor(targetDevice, MODEL_KEY_DEVICES);
            if (deviceData.has(ATTRIBUTES_KEY_REMOTE_LINKS)) {
                JSONArray jsonLinks = deviceData.getJSONArray(ATTRIBUTES_KEY_REMOTE_LINKS);
                jsonLinks.forEach(link -> {
                    linksToSend.add(link.toString());
                });
                if (customDebug) {
                    logger.info("DIRIGERA BASE_MATTER_HANDLER {} links for {} {}", thing.getLabel(),
                            gateway().model().getCustonNameFor(targetDevice), linksToSend);
                }
                // this is sensor branch so add link of sensor
                if (add) {
                    if (!linksToSend.contains(triggerDevice)) {
                        linksToSend.add(triggerDevice);
                    } else {
                        if (customDebug) {
                            logger.info("DIRIGERA BASE_MATTER_HANDLER {} already linked {}", thing.getLabel(),
                                    gateway().model().getCustonNameFor(triggerDevice));
                        }
                    }
                } else {
                    if (linksToSend.contains(triggerDevice)) {
                        linksToSend.remove(triggerDevice);
                    } else {
                        if (customDebug) {
                            logger.info("DIRIGERA BASE_MATTER_HANDLER {} no link to remove {}", thing.getLabel(),
                                    gateway().model().getCustonNameFor(triggerDevice));
                        }
                    }
                }
            } else {
                if (customDebug) {
                    logger.info("DIRIGERA BASE_MATTER_HANDLER {} has no remoteLinks", thing.getLabel());
                }
            }
        } else {
            // send update to this device
            targetDevice = config.id;
            triggerDevice = linkedDeviceId;
            if (add) {
                hardLinks.add(triggerDevice);
            } else {
                hardLinks.remove(triggerDevice);
            }
            linksToSend.addAll(hardLinks);
        }
        JSONArray newLinks = new JSONArray(linksToSend);
        JSONObject attributes = new JSONObject();
        attributes.put(ATTRIBUTES_KEY_REMOTE_LINKS, newLinks);
        gateway().api().sendPatch(targetDevice, attributes);
        // after api command remoteLinks property will be updated and trigger new linkUpadte
    }

    /**
     * Adds a soft link towards the device which has the link stored in his attributes
     *
     * @param device id of the device which contains this link
     */
    @Override
    public void addSoftlink(String id) {
        if (!softLinks.contains(id) && !config.id.equals(id)) {
            softLinks.add(id);
        }
    }

    /**
     * Update cycle of gateway is done
     */
    @Override
    public void updateLinksDone() {
        logger.trace("DIRIGERA BASE_MATTER_HANDLER {} updateLinksDone hardLinks {} softLinks {}", thing.getLabel(),
                hardLinks, softLinks);
        if (hasLinksOrCandidates()) {
            updateLinks();
            // The candidates needs to be evaluated by child class
            // - blindController needs blinds and vice versa
            // - soundCotroller needs speakers and vice versa
            // - lightController needs light and outlet and vice versa
            // So assure "linkCandidateTypes" are overwritten by child class with correct types
            updateCandidateLinks();
        }
    }

    protected void updateLinks() {
        List<String> display = new ArrayList<>();
        List<CommandOption> linkCommandOptions = new ArrayList<>();
        List<String> allLinks = new ArrayList<>();
        allLinks.addAll(hardLinks);
        allLinks.addAll(softLinks);
        Collections.sort(allLinks);
        allLinks.forEach(link -> {
            String customName = gateway().model().getCustonNameFor(link);
            if (!gateway().isKnownDevice(link)) {
                // if device isn't present in OH attach this suffix
                customName += " (!)";
            }
            display.add(customName);
            linkCommandOptions.add(new CommandOption(link, customName));
        });
        ChannelUID channelUUID = new ChannelUID(thing.getUID(), CHANNEL_LINKS);
        gateway().getCommandProvider().setCommandOptions(channelUUID, linkCommandOptions);
        logger.trace("DIRIGERA BASE_MATTER_HANDLER {} links {}", thing.getLabel(), display);
        updateState(channelUUID, StringType.valueOf(display.toString()));
    }

    public void updateCandidateLinks() {
        List<String> possibleCandidates = gateway().model().getDevicesForTypes(linkCandidateTypes);
        List<String> candidates = new ArrayList<>();
        possibleCandidates.forEach(entry -> {
            if (!hardLinks.contains(entry) && !softLinks.contains(entry)) {
                candidates.add(entry);
            }
        });

        List<String> display = new ArrayList<>();
        List<CommandOption> candidateOptions = new ArrayList<>();
        Collections.sort(candidates);
        candidates.forEach(candidate -> {
            String customName = gateway().model().getCustonNameFor(candidate);
            if (!gateway().isKnownDevice(candidate)) {
                // if device isn't present in OH attach this suffix
                customName += " (!)";
            }
            display.add(customName);
            candidateOptions.add(new CommandOption(candidate, customName));
        });
        ChannelUID channelUUID = new ChannelUID(thing.getUID(), CHANNEL_LINK_CANDIDATES);
        gateway().getCommandProvider().setCommandOptions(channelUUID, candidateOptions);
        updateState(channelUUID, StringType.valueOf(display.toString()));
    }

    protected boolean hasType(String queryDeviceType) {
        for (MatterModel matterConfig : deviceConfigMap.values()) {
            if (matterConfig.getDeviceType().equals(queryDeviceType)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> getIdsFor(String deviceTypeOccupancySensor) {
        List<String> ids = new ArrayList<>();
        deviceConfigMap.forEach((deviceId, matterConfig) -> {
            if (matterConfig.getDeviceType().equals(deviceTypeOccupancySensor)) {
                ids.add(deviceId);
            }
        });
        return ids;
    }

    /**
     * Check is any outgoing or incoming links or candidates are available
     *
     * @return true if one of the above conditions is true
     */
    private boolean hasLinksOrCandidates() {
        return (!hardLinks.isEmpty() || !softLinks.isEmpty()
                || !gateway().model().getDevicesForTypes(linkCandidateTypes).isEmpty());
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
     * Debug commands for console access
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

    /**
     * for unit testing
     */
    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }
}
