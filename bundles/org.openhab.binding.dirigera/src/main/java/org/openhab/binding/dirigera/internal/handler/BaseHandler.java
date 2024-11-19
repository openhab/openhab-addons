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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.NoGatewayException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.interfaces.LightListener;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BaseHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private List<LightListener> powerListeners = new ArrayList<>();
    private @Nullable Gateway gateway;

    // to be overwritten by child class in order to route the updates to the right instance
    protected @Nullable BaseHandler child;

    // maps to route properties to channels and vice versa
    protected Map<String, String> property2ChannelMap;
    protected Map<String, String> channel2PropertyMap;

    // cache to handle each refresh command properly
    protected Map<String, State> channelStateMap;

    // JSONObject for all device data for debug purposes. Maybe deleted in release version
    protected JSONObject deviceData = new JSONObject();
    protected List<JSONObject> updates = new ArrayList<>();

    /*
     * hardlinks initialized with invalid links because the first update shall trigger a link update. If it's declared
     * as empty no link update will be triggered. This is necessary for startup phase.
     */
    protected List<String> hardLinks = new ArrayList<>(Arrays.asList("undef"));
    protected List<String> softLinks = new ArrayList<>();
    protected List<String> linkCandidateTypes = new ArrayList<>();

    /**
     * Lists for canReceive and can Send capabilities
     */
    protected List<String> receiveCapabilities = new ArrayList<>();
    protected List<String> sendCapabilities = new ArrayList<>();

    protected State requestedPowerState = UnDefType.UNDEF;
    protected State currentPowerState = UnDefType.UNDEF;
    protected BaseDeviceConfiguration config;
    protected String customName = "";
    protected String deviceType = "";
    protected boolean disposed = true;
    protected boolean online = false;

    public BaseHandler(Thing thing, Map<String, String> mapping) {
        super(thing);
        config = new BaseDeviceConfiguration();

        // mapping contains, reverse mapping for commands plus state cache
        property2ChannelMap = mapping;
        channel2PropertyMap = reverse(mapping);
        channelStateMap = initializeCache(mapping);
    }

    protected void setChildHandler(BaseHandler child) {
        this.child = child;
    }

    @Override
    public void initialize() {
        disposed = false;
        config = getConfigAs(BaseDeviceConfiguration.class);

        // first get bridge as Gateway
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
                    gateway = gw;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/dirigera.device.status.wrong-bridge-type");
                    return;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/dirigera.device.missing-bridge-handler");
                return;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/dirigera.device.status.missing-bridge");
            return;
        }

        if (!checkHandler()) {
            // if handler doesn't match model status will be set to offline and it will stay until correction
            return;
        }

        if (!config.id.isBlank()) {
            BaseHandler proxy = child;
            if (proxy != null) {
                gateway().registerDevice(proxy, config.id);
            }

            // fill canSend and canReceive capabilities
            Map<String, Object> modelProperties = gateway().model().getPropertiesFor(config.id);
            Object canReceiveCapabilities = modelProperties.get(Model.PROPERTY_CAN_RECEIVE);
            if (canReceiveCapabilities instanceof JSONArray jsonArray) {
                jsonArray.forEach(capability -> {
                    if (!receiveCapabilities.contains(capability.toString())) {
                        receiveCapabilities.add(capability.toString());
                    }
                });
            }
            Object canSendCapabilities = modelProperties.get(Model.PROPERTY_CAN_SEND);
            if (canSendCapabilities instanceof JSONArray jsonArray) {
                jsonArray.forEach(capability -> {
                    if (!sendCapabilities.contains(capability.toString())) {
                        sendCapabilities.add(capability.toString());
                    }
                });
            }

            TreeMap<String, String> handlerProperties = new TreeMap<>(editProperties());
            modelProperties.forEach((key, value) -> {
                handlerProperties.put(key, value.toString());
            });
            updateProperties(handlerProperties);
        }
    }

    /**
     * Handling of basic commands which are the same for many devices
     * - RefreshType for all channels
     * - Startup behavior for lights and plugs
     * - Power state for lights and plugs
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        } else {
            String targetChannel = channelUID.getIdWithoutGroup();
            String targetProperty = channel2PropertyMap.get(targetChannel);
            if (targetProperty != null) {
                switch (targetChannel) {
                    case CHANNEL_STARTUP_BEHAVIOR:
                        if (command instanceof DecimalType decimal) {
                            String behaviorCommand = STARTUP_BEHAVIOR_REVERSE_MAPPING.get(decimal.intValue());
                            if (behaviorCommand != null) {
                                JSONObject stqartupAttributes = new JSONObject();
                                stqartupAttributes.put(targetProperty, behaviorCommand);
                                gateway().api().sendAttributes(config.id, stqartupAttributes);
                            }
                            break;
                        }
                        break;
                    case CHANNEL_POWER_STATE:
                        if (command instanceof OnOffType onOff) {
                            logger.debug("DIRIGERA BASE_HANDLER {} OnOff command: Current / Wanted {} {}",
                                    thing.getLabel(), currentPowerState, onOff);
                            requestedPowerState = onOff;
                            if (!currentPowerState.equals(onOff)) {
                                JSONObject attributes = new JSONObject();
                                attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                                logger.trace("DIRIGERA BASE_HANDLER {} send to API {}", thing.getLabel(), attributes);
                                gateway().api().sendAttributes(config.id, attributes);
                            } else {
                                requestedPowerState = UnDefType.UNDEF;
                                logger.trace("DIRIGERA BASE_HANDLER Dismiss {} {}", thing.getLabel(), onOff);
                            }
                        }
                        break;
                    case CHANNEL_CUSTOM_NAME:
                        if (command instanceof StringType string) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, string.toString());
                            logger.trace("DIRIGERA BASE_HANDLER {} send to API {}", thing.getLabel(), attributes);
                            gateway().api().sendAttributes(config.id, attributes);
                        }
                        break;
                    case CHANNEL_LINKS:
                        logger.debug("DIRIGERA BASE_HANDLER {} remove connection {}", thing.getLabel(),
                                command.toFullString());
                        if (command instanceof StringType string) {
                            linkUpdate(string.toFullString(), false);
                        }
                        break;
                    case CHANNEL_LINK_CANDIDATES:
                        logger.debug("DIRIGERA BASE_HANDLER {} add link {}", thing.getLabel(), command.toFullString());
                        if (command instanceof StringType string) {
                            linkUpdate(string.toFullString(), true);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Handling generic channel updates for many devices.
     * If they are not present in child configuration they won't be triggered.
     * - Reachable flag for every device to evaluate ONLINE and OFFLINE states
     * - Over the air (OTA) updates channels
     * - Battery charge level
     * - Startup behavior for lights and plugs
     * - Power state for lights and plugs
     * - custom name
     *
     * @param update
     */
    public void handleUpdate(JSONObject update) {
        // check online offline for each device
        if (update.has(Model.REACHABLE)) {
            if (update.getBoolean(Model.REACHABLE)) {
                updateStatus(ThingStatus.ONLINE);
                online = true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.device.status.not-reachable");
                online = false;
            }
        }
        if (update.has(PROPERTY_DEVICE_TYPE) && deviceType.isBlank()) {
            deviceType = update.getString(PROPERTY_DEVICE_TYPE);
        }
        if (update.has(PROPERTY_REMOTE_LINKS)) {
            JSONArray remoteLinks = update.getJSONArray(PROPERTY_REMOTE_LINKS);
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
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            // check OTA for each device
            if (attributes.has(PROPERTY_OTA_STATUS)) {
                String otaStatusString = attributes.getString(PROPERTY_OTA_STATUS);
                Integer otaStatus = OTA_STATUS_MAP.get(otaStatusString);
                if (otaStatus != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), new DecimalType(otaStatus));
                } else {
                    logger.warn("DIRIGERA BASE_HANDLER {} Cannot decode ota status {}", thing.getLabel(),
                            otaStatusString);
                }
            }
            if (attributes.has(PROPERTY_OTA_STATE)) {
                String otaStateString = attributes.getString(PROPERTY_OTA_STATE);
                Integer otaState = OTA_STATE_MAP.get(otaStateString);
                if (otaState != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), new DecimalType(otaState));
                } else {
                    logger.warn("DIRIGERA BASE_HANDLER {} Cannot decode ota state {}", thing.getLabel(),
                            otaStateString);
                }
            }
            if (attributes.has(PROPERTY_OTA_PROGRESS)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS),
                        QuantityType.valueOf(attributes.getInt(PROPERTY_OTA_PROGRESS), Units.PERCENT));
            }
            // battery also common, not for all but sensors and remote controller
            if (attributes.has(PROPERTY_BATTERY_PERCENTAGE)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL),
                        QuantityType.valueOf(attributes.getInt(PROPERTY_BATTERY_PERCENTAGE), Units.PERCENT));
            }
            if (attributes.has(PROPERTY_STARTUP_BEHAVIOR)) {
                String startupString = attributes.getString(PROPERTY_STARTUP_BEHAVIOR);
                Integer startupValue = STARTUP_BEHAVIOR_MAPPING.get(startupString);
                if (startupValue != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR),
                            new DecimalType(startupValue));
                } else {
                    logger.warn("DIRIGERA BASE_HANDLER {} Cannot decode startup behavior {}", thing.getLabel(),
                            startupString);
                }
            }
            if (attributes.has(PROPERTY_POWER_STATE)) {
                currentPowerState = OnOffType.from(attributes.getBoolean(PROPERTY_POWER_STATE));
                logger.debug("DIRIGERA BASE_HANDLER {} OnOff update {}", thing.getLabel(), currentPowerState);
                updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), currentPowerState);
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
            if (attributes.has(PROPERTY_CUSTOM_NAME) && customName.isBlank()) {
                customName = attributes.getString(PROPERTY_CUSTOM_NAME);
                updateState(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME), StringType.valueOf(customName));
            }
        }

        // store data for development and analysis purposes
        if (deviceData.isEmpty()) {
            deviceData = new JSONObject(update.toString());
        } else {
            updates.add(new JSONObject(update.toString()));
            // keep last 10 updates for debugging
            if (updates.size() > 10) {
                updates.remove(0);
            }
        }
        deviceData.put("updates", new JSONArray(updates));
        updateState(new ChannelUID(thing.getUID(), CHANNEL_JSON), StringType.valueOf(deviceData.toString()));
    }

    protected boolean isPowered() {
        return OnOffType.ON.equals(currentPowerState) && online;
    }

    /**
     * Update cache for refresh, then update state
     */
    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        channelStateMap.put(channelUID.getIdWithoutGroup(), state);
        if (!disposed) {
            super.updateState(channelUID, state);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        online = false;
        BaseHandler proxy = child;
        if (proxy != null) {
            gateway().unregisterDevice(proxy, config.id);
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        BaseHandler proxy = child;
        if (proxy != null) {
            gateway().deleteDevice(proxy, config.id);
        }
        super.handleRemoval();
    }

    public Gateway gateway() {
        Gateway gw = gateway;
        if (gw != null) {
            return gw;
        } else {
            throw new NoGatewayException(thing.getUID() + " has no Gateway defined");
        }
    }

    protected boolean checkHandler() {
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

    private Map<String, State> initializeCache(Map<String, String> mapping) {
        final Map<String, State> stateMap = new HashMap<>();
        mapping.forEach((key, value) -> {
            stateMap.put(key, UnDefType.UNDEF);
        });
        return stateMap;
    }

    /**
     * Evaluates if this device is a controller or sensor
     *
     * @return boolean
     */
    protected boolean isControllerOrSensor() {
        return deviceType.toLowerCase().contains("sensor") || deviceType.toLowerCase().contains("controller");
    }

    /**
     * Handling of links
     */

    /**
     * Update cycle of gateway is done
     */
    public void updateLinksStart() {
        softLinks.clear();
    }

    /**
     * Get real links from device updates. Delivers a copy due to concurrent access.
     *
     * @return links attached to this device
     */
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
            JSONObject deviceData = gateway().model().getAllFor(targetDevice, PROPERTY_DEVICES);
            if (deviceData.has(PROPERTY_REMOTE_LINKS)) {
                JSONArray jsonLinks = deviceData.getJSONArray(PROPERTY_REMOTE_LINKS);
                jsonLinks.forEach(link -> {
                    linksToSend.add(link.toString());
                });
                logger.trace("DIRIGERA BASE_HANDLER {} links for {} {}", thing.getLabel(),
                        gateway().model().getCustonNameFor(targetDevice), linksToSend);
                // this is sensor branch so add link of sensor
                if (add) {
                    if (!linksToSend.contains(triggerDevice)) {
                        linksToSend.add(triggerDevice);
                    } else {
                        logger.trace("DIRIGERA BASE_HANDLER {} already linked {}", thing.getLabel(),
                                gateway().model().getCustonNameFor(triggerDevice));
                    }
                } else {
                    if (linksToSend.contains(triggerDevice)) {
                        linksToSend.remove(triggerDevice);
                    } else {
                        logger.trace("DIRIGERA BASE_HANDLER {} no link to remove {}", thing.getLabel(),
                                gateway().model().getCustonNameFor(triggerDevice));
                    }
                }
            } else {
                logger.trace("DIRIGERA BASE_HANDLER {} has no remoteLinks", thing.getLabel());
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
        attributes.put(PROPERTY_REMOTE_LINKS, newLinks);
        gateway().api().sendPatch(targetDevice, attributes);
        // after api command remoteLinks property will be updated and trigger new linkUpadte
    }

    /**
     * Adds a soft link towards the device which has the link stored in his attributes
     *
     * @param device id of the device which contains this link
     */
    public void addSoftlink(String id) {
        if (!softLinks.contains(id) && !config.id.equals(id)) {
            softLinks.add(id);
            logger.trace("DIRIGERA BASE_HANDLER {} softlink added for {}", thing.getLabel(),
                    gateway().model().getCustonNameFor(id));
        } else {
            logger.trace("DIRIGERA BASE_HANDLER {} softlink already established to {}", thing.getLabel(),
                    gateway().model().getCustonNameFor(id));
        }
    }

    /**
     * Update cycle of gateway is done
     */
    public void updateLinksDone() {
        if (linksSupported()) {
            updateLinks();
            // The candidates needs to be evaluated by child class
            // - blindController needs blinds and vice versa
            // - soundCotroller needs speakers and vice versa
            // - lightController needs light and outlet and vice versa
            // So assure "linkCandidateTypes" are overwritten by child class with correct types
            updateCandidateLinks();
        } else {
            if (!hardLinks.isEmpty() || !softLinks.isEmpty()) {
                logger.trace("DIRIGERA BASE_HANDLER {} Device doesn't support links hard {} soft {}", thing.getLabel(),
                        hardLinks.size(), softLinks.size());
            }
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
        logger.trace("DIRIGERA BASE_HANDLER {} links {}", thing.getLabel(), display);
        updateState(channelUUID, StringType.valueOf(display.toString()));
    }

    protected void updateCandidateLinks() {
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

    /**
     * Check if device is supporting link handling
     *
     * @return true is links are supported, false otherwise
     */
    public boolean linksSupported() {
        return channel2PropertyMap.containsKey(CHANNEL_LINKS);
    }

    public void addPowerListener(LightListener listener) {
        synchronized (powerListeners) {
            powerListeners.add(listener);
        }
    }

    public void removePowerListener(LightListener listener) {
        synchronized (powerListeners) {
            powerListeners.remove(listener);
        }
    }

    /**
     * for unit testing
     */
    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }
}
