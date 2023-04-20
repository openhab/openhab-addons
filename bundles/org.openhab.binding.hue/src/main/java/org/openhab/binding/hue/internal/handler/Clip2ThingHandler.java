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
package org.openhab.binding.hue.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.config.Clip2ThingConfig;
import org.openhab.binding.hue.internal.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.dto.clip2.Effects;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.EffectType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for things based on CLIP 2 'device', 'room', or 'zone resources.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class Clip2ThingHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(HueBindingConstants.THING_TYPE_DEVICE,
            HueBindingConstants.THING_TYPE_ROOM, HueBindingConstants.THING_TYPE_ZONE);

    private final Logger logger = LoggerFactory.getLogger(Clip2ThingHandler.class);

    /**
     * A map of Resources whose state contributes to the overall state of this thing. It is a map between the resource
     * ID (string) and a Resource object containing the last known state. e.g. the state of a LIGHT Resource contributes
     * to the overall state of a DEVICE thing, or the state of a GROUPED_LIGHT Resource contributes to the overall state
     * of a ROOM or ZONE thing.
     */
    private final Map<String, Resource> contributorsCache = new ConcurrentHashMap<>();

    /**
     * A map of Resource IDs which are targets for commands to be sent. It is a map between the type of command
     * (ResourcesType) and the resource ID to which the command shall be sent. e.g. a LIGHT on command shall be sent to
     * the respective LIGHT resource ID.
     */
    private final Map<ResourceType, String> commandResourceIds = new ConcurrentHashMap<>();

    /**
     * Button devices contain one or more physical buttons, each of which is represented by a BUTTON Resource with its
     * own unique resource ID, and a respective controlId that indicates which button it is in the device. e.g. a dimmer
     * pad has four buttons (controlId's 1..4) each represented by a BUTTON Resource with a unique resource ID. This is
     * a map between the resource ID and its respective controlId.
     */
    private final Map<String, Integer> controlIds = new ConcurrentHashMap<>();

    /**
     * A list of channel IDs that are supported by this thing. e.g. an on/off light may support 'switch' and
     * 'zigbeeStatus' channels, whereas a complex light may support 'switch', 'brightness', 'color', 'color temperature'
     * and 'zigbeeStatus' channels.
     */
    private final List<String> supportedChannelIds = new CopyOnWriteArrayList<>();

    /**
     * A list of scene Resources that are supported by this thing.
     */
    private final List<Resource> supportedScenes = new CopyOnWriteArrayList<>();

    /**
     * A map of scene names versus Resource IDs for the scenes that are associated with this thing.
     */
    private final Map<String, String> sceneCommandResourceIds = new ConcurrentHashMap<>();

    /**
     * A list of API v1 thing channel UIDs that are linked to items. It is used in the process of replicating the
     * Item/Channel links from a legacy v1 thing to this API v2 thing.
     */
    private final List<ChannelUID> legacyLinkedChannelUIDs = new CopyOnWriteArrayList<>();

    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;
    private final Clip2StateDescriptionProvider stateDescriptionProvider;

    private String resourceId = "?";
    private Resource thisResource;

    private boolean disposing;
    private boolean hasConnectivityIssue;
    private boolean updateScenesDone;
    private boolean updatePropertiesDone;
    private boolean updateDependenciesDone;

    private @Nullable ScheduledFuture<?> updateContributorsTask;

    public Clip2ThingHandler(Thing thing, Clip2StateDescriptionProvider stateDescriptionProvider,
            ThingRegistry thingRegistry, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (HueBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.DEVICE);
        } else if (HueBindingConstants.THING_TYPE_ROOM.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.ROOM);
        } else if (HueBindingConstants.THING_TYPE_ZONE.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.ZONE);
        } else {
            throw new IllegalArgumentException("Wrong thing type " + thingTypeUID.getAsString());
        }

        this.thingRegistry = thingRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void dispose() {
        logger.debug("{} -> dispose()", resourceId);
        disposing = true;
        ScheduledFuture<?> task = updateContributorsTask;
        if (Objects.nonNull(task)) {
            task.cancel(true);
        }
        updateContributorsTask = null;
        supportedScenes.clear();
        legacyLinkedChannelUIDs.clear();
        sceneCommandResourceIds.clear();
        supportedChannelIds.clear();
        commandResourceIds.clear();
        contributorsCache.clear();
        controlIds.clear();
    }

    /**
     * Get the bridge handler.
     *
     * @throws AssetNotLoadedException if the handler does not exist.
     */
    private Clip2BridgeHandler getBridgeHandler() throws AssetNotLoadedException {
        Bridge bridge = getBridge();
        if (Objects.nonNull(bridge)) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof Clip2BridgeHandler) {
                return (Clip2BridgeHandler) handler;
            }
        }
        throw new AssetNotLoadedException("Bridge handler missing");
    }

    /**
     * Do a double lookup to get the cached resource that matches the given ResourceType.
     *
     * @param resourceType the type to search for.
     * @return the Resource, or null if not found.
     */
    private @Nullable Resource getCachedResource(ResourceType resourceType) {
        String commandResourceId = commandResourceIds.get(resourceType);
        return Objects.nonNull(commandResourceId) ? contributorsCache.get(commandResourceId) : null;
    }

    /**
     * Return a ResourceReference to this handler's resource.
     *
     * @return a ResourceReference instance.
     */
    public ResourceReference getResourceReference() {
        return new ResourceReference().setId(thisResource.getId()).setType(thisResource.getType());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            if ((thing.getStatus() == ThingStatus.ONLINE) && updateDependenciesDone) {
                ScheduledFuture<?> task = updateContributorsTask;
                if (Objects.isNull(task) || !task.isDone()) {
                    updateContributorsTask = scheduler.schedule(() -> {
                        try {
                            updateContributors();
                        } catch (ApiException | AssetNotLoadedException e) {
                            // exceptions will not occur here since thing is already online
                        }
                    }, 3, TimeUnit.SECONDS);
                }
            }
            return;
        }

        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.warn("{} -> handleCommand() channelUID:{} does not exist", resourceId, channelUID);
            return;
        }

        ResourceType lightResourceType = thisResource.getType() == ResourceType.DEVICE ? ResourceType.LIGHT
                : ResourceType.GROUPED_LIGHT;

        Resource putResource = null;
        String putResourceId = null;

        switch (channelUID.getId()) {
            case HueBindingConstants.CHANNEL_2_ALERT:
                putResource = new Resource(lightResourceType).setAlert(command, getCachedResource(lightResourceType));
                scheduler.schedule(() -> updateState(channelUID, new StringType(ActionType.NO_ACTION.name())), 5,
                        TimeUnit.SECONDS);
                break;

            case HueBindingConstants.CHANNEL_2_EFFECT:
                putResource = new Resource(lightResourceType).setEffect(command, getCachedResource(lightResourceType));
                break;

            case HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE:
                putResource = new Resource(lightResourceType).setColorTemperaturePercent(command,
                        getCachedResource(ResourceType.LIGHT));
                break;

            case HueBindingConstants.CHANNEL_2_COLOR_TEMP_KELVIN:
                putResource = new Resource(lightResourceType).setColorTemperatureKelvin(command);
                break;

            case HueBindingConstants.CHANNEL_2_COLOR:
                putResource = new Resource(lightResourceType).setColor(command, getCachedResource(lightResourceType));
                break;

            case HueBindingConstants.CHANNEL_2_BRIGHTNESS:
                putResource = new Resource(lightResourceType).setBrightness(command);
                break;

            case HueBindingConstants.CHANNEL_2_SWITCH:
                putResource = new Resource(lightResourceType).setSwitch(command);
                break;

            case HueBindingConstants.CHANNEL_2_TEMPERATURE_ENABLED:
                putResource = new Resource(ResourceType.TEMPERATURE).setEnabled(command);
                break;

            case HueBindingConstants.CHANNEL_2_MOTION_ENABLED:
                putResource = new Resource(ResourceType.MOTION).setEnabled(command);
                break;

            case HueBindingConstants.CHANNEL_2_LIGHT_LEVEL_ENABLED:
                putResource = new Resource(ResourceType.LIGHT_LEVEL).setEnabled(command);
                break;

            case HueBindingConstants.CHANNEL_2_SCENE:
                if (command instanceof StringType) {
                    putResourceId = sceneCommandResourceIds.get(((StringType) command).toString());
                    if (Objects.nonNull(putResourceId)) {
                        putResource = new Resource(ResourceType.SCENE).setRecall();
                    }
                }
                break;

            default:
                logger.warn("{} -> handleCommand() channelUID:{} not supported", resourceId, channelUID);
                return;
        }

        if (putResource == null) {
            logger.warn("{} -> handleCommand() command:{} not supported on channelUID:{}", resourceId, command,
                    channelUID);
            return;
        }

        putResourceId = Objects.nonNull(putResourceId) ? putResourceId : commandResourceIds.get(putResource.getType());
        if (putResourceId == null) {
            logger.warn("{} -> handleCommand() channelUID:{}, command:{}, putResourceType:{} => missing resource ID",
                    resourceId, channelUID, command, putResource.getType());
            return;
        }

        putResource.setId(putResourceId);
        logger.debug("{} -> handleCommand() put resource {}", resourceId, putResource);

        try {
            getBridgeHandler().putResource(putResource);
        } catch (ApiException | AssetNotLoadedException e) {
            logger.warn("{} -> handleCommand() error {}", resourceId, e.getMessage(), e);
        }
    }

    @Override
    public void initialize() {
        Clip2ThingConfig config = getConfigAs(Clip2ThingConfig.class);

        String resourceId = config.resourceId;
        if (Objects.isNull(resourceId) || resourceId.isEmpty()) {
            logger.debug("{} -> initialize() configuration resourceId is bad", resourceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.api2.conf-error-resource-id-bad");
            return;
        }
        thisResource.setId(resourceId);
        this.resourceId = resourceId;
        logger.debug("{} -> initialize()", resourceId);

        updateThingFromLegacy();
        updateStatus(ThingStatus.UNKNOWN);

        disposing = false;
        hasConnectivityIssue = false;
        updateScenesDone = false;
        updatePropertiesDone = false;
        updateDependenciesDone = false;
    }

    /**
     * Update the channel state depending on a new resource sent from the bridge.
     *
     * @param resource a Resource object containing the new state.
     */
    public void onResource(Resource resource) {
        if (!disposing) {
            boolean resourceConsumed = false;
            if (thisResource.getId().equals(resource.getId())) {
                if (resource.hasFullState()) {
                    thisResource = resource;
                    if (!updatePropertiesDone) {
                        updateProperties(resource);
                        resourceConsumed = true;
                    }
                }
                if (!updateDependenciesDone) {
                    resourceConsumed = true;
                    scheduler.submit(() -> updateDependencies());
                }
            }
            String cacheId = resource.getId();
            Resource cacheResource = contributorsCache.get(cacheId);
            if (Objects.nonNull(cacheResource)) {
                resourceConsumed = true;
                resource.copyMissingFieldsFrom(cacheResource);
                updateChannels(resource);
                contributorsCache.put(cacheId, resource);
            }
            if (resourceConsumed) {
                logger.debug("{} -> onResource() consumed resource {}", resourceId, resource);
            }
        }
    }

    /**
     * Process the incoming Resource to initialize the alert channel.
     *
     * @param resource a Resource possibly with an Alerts element.
     */
    private void updateAlertChannel(Resource resource) {
        Alerts alerts = resource.getAlerts();
        if (Objects.nonNull(alerts)) {
            List<StateOption> stateOptions = alerts.getActionValues().stream().map(action -> action.name())
                    .map(actionId -> new StateOption(actionId, actionId)).collect(Collectors.toList());
            if (!stateOptions.isEmpty()) {
                stateDescriptionProvider.setStateOptions(
                        new ChannelUID(thing.getUID(), HueBindingConstants.CHANNEL_ALERT), stateOptions);
                logger.debug("{} -> updateAlerts() found {} associated alerts", resourceId, stateOptions.size());
            }
        }
    }

    /**
     * If this v2 thing has a matching v1 legacy thing in the system, then for each channel in the v1 thing that
     * corresponds to an equivalent channel in this v2 thing, and for all items that are linked to the v1 channel,
     * create a new channel/item link between that item and the respective v2 channel in this thing.
     */
    private void updateChannelItemLinksFromLegacy() {
        if (!disposing) {
            legacyLinkedChannelUIDs.forEach(legacyLinkedChannelUID -> {
                String targetChannelId = HueBindingConstants.REPLICATE_CHANNEL_ID_MAP
                        .get(legacyLinkedChannelUID.getId());
                if (Objects.nonNull(targetChannelId)) {
                    Channel targetChannel = thing.getChannel(targetChannelId);
                    if (Objects.nonNull(targetChannel)) {
                        ChannelUID uid = targetChannel.getUID();
                        itemChannelLinkRegistry.getLinkedItems(legacyLinkedChannelUID).forEach(linkedItem -> {
                            String item = linkedItem.getName();
                            if (!itemChannelLinkRegistry.isLinked(item, uid)) {
                                logger.info(
                                        "{} -> updateChannelItemLinksFromLegacy() created link between Channel:{} and Item:{}",
                                        resourceId, uid, item);
                                itemChannelLinkRegistry.add(new ItemChannelLink(item, uid));
                            }
                        });
                    }
                }
            });
            legacyLinkedChannelUIDs.clear();
        }
    }

    /**
     * Set the active list of channels by removing any that had initially been created by the thing XML declaration, but
     * which in fact did not have data returned from the bridge i.e. channels which are not in the supportedChannelIds
     * list. Also warn if there are channels in the supportedChannelIds set which are not in the thing.
     */
    private void updateChannelList() {
        if (!disposing) {
            logger.debug("{} -> updateChannelList() supportedChannelIds.size():{}", resourceId,
                    supportedChannelIds.size());

            // warn about any missing channels
            supportedChannelIds.stream().filter(channelId -> Objects.isNull(thing.getChannel(channelId)))
                    .forEach(channelId -> logger.warn(
                            "{} -> updateChannelList() required channel '{}' missing => please recreate thing!",
                            resourceId, channelId));

            // get list of unused channels
            List<Channel> unusedChannels = thing.getChannels().stream()
                    .filter(channel -> !supportedChannelIds.contains(channel.getUID().getId()))
                    .collect(Collectors.toList());

            // remove any unused channels
            if (!unusedChannels.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    unusedChannels.stream().map(channel -> channel.getUID().getId()).forEach(channelId -> logger
                            .debug("{} -> updateChannelList() removing unused channel '{}'", resourceId, channelId));
                }
                updateThing(editThing().withoutChannels(unusedChannels).build());
            }
        }
    }

    /**
     * Update the state of the existing channels.
     *
     * @param resource the Resource containing the new channel state.
     * @return true if the channel was found and updated.
     */
    private boolean updateChannels(Resource resource) {
        logger.debug("{} -> updateChannels() from resource {}", resourceId, resource);
        boolean fullUpdate = resource.hasFullState();
        switch (resource.getType()) {
            case BUTTON:
                if (fullUpdate) {
                    supportedChannelIds.add(HueBindingConstants.CHANNEL_2_BUTTON_LAST_EVENT);
                }
                resource.addControlIdToMap(controlIds);
                State buttonState = resource.getButtonEventState(controlIds);
                updateState(HueBindingConstants.CHANNEL_2_BUTTON_LAST_EVENT, buttonState, fullUpdate);
                break;

            case DEVICE_POWER:
                updateState(HueBindingConstants.CHANNEL_2_BATTERY_LEVEL, resource.getBatteryLevelState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_BATTERY_LOW, resource.getBatteryLowState(), fullUpdate);
                break;

            case LIGHT:
                if (fullUpdate) {
                    updateEffectChannel(resource);
                }
                updateState(HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE, resource.getColorTemperaturePercentState(),
                        fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_COLOR_TEMP_KELVIN, resource.getColorTemperatureKelvinState(),
                        fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_COLOR, resource.getColorState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_EFFECT, resource.getEffectState(), fullUpdate);
                // fall through for brightness and switch channels

            case GROUPED_LIGHT:
                if (fullUpdate) {
                    updateAlertChannel(resource);
                }
                updateState(HueBindingConstants.CHANNEL_2_BRIGHTNESS, resource.getBrightnessState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_SWITCH, resource.getSwitch(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_ALERT, resource.getAlertState(), fullUpdate);
                break;

            case LIGHT_LEVEL:
                updateState(HueBindingConstants.CHANNEL_2_LIGHT_LEVEL, resource.getLightLevelState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_LIGHT_LEVEL_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case MOTION:
                updateState(HueBindingConstants.CHANNEL_2_MOTION, resource.getMotionState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_MOTION_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case RELATIVE_ROTARY:
                if (fullUpdate) {
                    supportedChannelIds.add(HueBindingConstants.CHANNEL_2_ROTARY_STEPS);
                }
                updateState(HueBindingConstants.CHANNEL_2_ROTARY_STEPS, resource.getRotaryStepsState(), fullUpdate);
                break;

            case TEMPERATURE:
                updateState(HueBindingConstants.CHANNEL_2_TEMPERATURE, resource.getTemperatureState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_TEMPERATURE_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case ZIGBEE_CONNECTIVITY:
                updateConnectivityState(resource);
                updateState(HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS, resource.getZigbeeState(), fullUpdate);
                break;

            case SCENE:
                updateState(HueBindingConstants.CHANNEL_SCENE, resource.getSceneState(), fullUpdate);
                break;

            default:
                return false;
        }
        if (thisResource.getType() == ResourceType.DEVICE) {
            updateState(HueBindingConstants.CHANNEL_2_LAST_UPDATED, new DateTimeType(), fullUpdate);
        }
        return true;
    }

    /**
     * Check the Zigbee connectivity and set the thing online status accordingly. If the thing is offline then set all
     * its channel states to undefined, otherwise execute a refresh command to update channels to the latest current
     * state.
     *
     * @param resource a Resource that potentially contains the Zigbee connectivity state.
     */
    private void updateConnectivityState(Resource resource) {
        ZigbeeStatus zigbeeStatus = resource.getZigbeeStatus();
        if (Objects.nonNull(zigbeeStatus)) {
            logger.debug("{} -> updateConnectivityState() thingStatus:{}, zigbeeStatus:{}", resourceId,
                    thing.getStatus(), zigbeeStatus);
            hasConnectivityIssue = zigbeeStatus != ZigbeeStatus.CONNECTED;
            if (hasConnectivityIssue) {
                if (thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/offline.api2.comm-error.zigbee-connectivity-issue");
                    // change all channel states, except the Zigbee channel itself, to undefined
                    supportedChannelIds.stream()
                            .filter(channelId -> !HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS.equals(channelId))
                            .forEach(channelId -> updateState(channelId, UnDefType.UNDEF));
                }
            } else if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                // one single refresh command will update all channels
                Channel zigbeeChannel = thing.getChannel(HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS);
                if (Objects.nonNull(zigbeeChannel)) {
                    handleCommand(zigbeeChannel.getUID(), RefreshType.REFRESH);
                }
            }
        }
    }

    /**
     * Execute a series of HTTP GET commands to fetch the resource data for all resources that contribute to the thing
     * state.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    private void updateContributors() throws ApiException, AssetNotLoadedException {
        logger.debug("{} -> updateContributors() called for {} contributors", resourceId, contributorsCache.size());
        ResourceReference reference = new ResourceReference();
        for (Entry<String, Resource> entry : contributorsCache.entrySet()) {
            updateResource(reference.setId(entry.getKey()).setType(entry.getValue().getType()));
        }
    }

    /**
     * Get all resources needed for building the thing state. Build the forward / reverse contributor lookup maps. Set
     * up the final list of channels in the thing.
     */
    private synchronized void updateDependencies() {
        if (!disposing && !updateDependenciesDone) {
            logger.debug("{} -> updateDependencies()", resourceId);
            try {
                updateLookups();
                updateContributors();
                updateChannelList();
                updateChannelItemLinksFromLegacy();
                if (!hasConnectivityIssue) {
                    updateStatus(ThingStatus.ONLINE);
                }
                updateDependenciesDone = true;
            } catch (ApiException e) {
                logger.debug("{} -> updateDependencies() {}", resourceId, e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (AssetNotLoadedException e) {
                logger.debug("{} -> updateDependencies() {}", resourceId, e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.api2.conf-error-assets-not-loaded");
            }
        }
    }

    /**
     * Process the incoming Resource to initialize the effects channel.
     *
     * @param resource a Resource possibly with an Effects element.
     */
    public void updateEffectChannel(Resource resource) {
        Effects effects = resource.getEffects();
        if (Objects.nonNull(effects)) {
            List<StateOption> stateOptions = effects.getStatusValues().stream()
                    .map(effect -> EffectType.of(effect).name()).map(effectId -> new StateOption(effectId, effectId))
                    .collect(Collectors.toList());
            if (!stateOptions.isEmpty()) {
                stateDescriptionProvider.setStateOptions(
                        new ChannelUID(thing.getUID(), HueBindingConstants.CHANNEL_EFFECT), stateOptions);
                logger.debug("{} -> updateEffects() found {} effects", resourceId, stateOptions.size());
            }
        }
    }

    /**
     * Initialize the lookup maps of resources that contribute to the thing state.
     */
    private void updateLookups() {
        if (!disposing) {
            logger.debug("{} -> updateLookups()", resourceId);
            if (!updateScenesDone) {
                logger.warn("{} -> updateLookups() scene list not initialized", resourceId);
            }
            contributorsCache.clear();
            commandResourceIds.clear();
            sceneCommandResourceIds.clear();

            // get supported services
            List<ResourceReference> services = thisResource.getServiceReferences();

            // add supported services to contributorsCache
            contributorsCache.putAll(services.stream()
                    .collect(Collectors.toMap(ResourceReference::getId, r -> new Resource(r.getType()))));

            // add associated scenes to contributorsCache
            contributorsCache
                    .putAll(supportedScenes.stream().collect(Collectors.toMap(Resource::getId, Function.identity())));

            // add supported services to commandResourceIds
            commandResourceIds.putAll(services.stream() // use a 'mergeFunction' to prevent duplicates
                    .collect(Collectors.toMap(ResourceReference::getType, ResourceReference::getId, (r1, r2) -> r1)));

            // add associated scenes to sceneCommandResourceIds
            sceneCommandResourceIds
                    .putAll(supportedScenes.stream().collect(Collectors.toMap(Resource::getName, Resource::getId)));
        }
    }

    /**
     * Update the primary device properties.
     *
     * @param resource a Resource object containing the property data.
     */
    private synchronized void updateProperties(Resource resource) {
        if (!disposing && !updatePropertiesDone) {

            logger.debug("{} -> updateProperties()", resourceId);
            Map<String, String> properties = new HashMap<>(thing.getProperties());

            // resource data
            properties.put(HueBindingConstants.PROPERTY_RESOURCE_ID, thisResource.getId());
            properties.put(HueBindingConstants.PROPERTY_RESOURCE_TYPE, thisResource.getType().toString());
            properties.put(HueBindingConstants.PROPERTY_RESOURCE_NAME, thisResource.getName());

            // owner information
            ResourceReference owner = thisResource.getOwner();
            if (Objects.nonNull(owner)) {
                String ownerId = owner.getId();
                if (Objects.nonNull(ownerId)) {
                    properties.put(HueBindingConstants.PROPERTY_OWNER, ownerId);
                }
                ResourceType ownerType = owner.getType();
                properties.put(HueBindingConstants.PROPERTY_OWNER_TYPE, ownerType.toString());
            }

            // metadata
            MetaData metaData = thisResource.getMetaData();
            if (Objects.nonNull(metaData)) {
                properties.put(HueBindingConstants.PROPERTY_RESOURCE_ARCHETYPE, metaData.getArchetype().toString());
            }

            // product data
            ProductData productData = thisResource.getProductData();
            if (Objects.nonNull(productData)) {
                // standard properties
                properties.put(Thing.PROPERTY_MODEL_ID, productData.getModelId());
                properties.put(Thing.PROPERTY_VENDOR, productData.getManufacturerName());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, productData.getSoftwareVersion());
                String hardwarePlatformType = productData.getHardwarePlatformType();
                if (Objects.nonNull(hardwarePlatformType)) {
                    properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwarePlatformType);
                }

                // hue specific properties
                properties.put(HueBindingConstants.PROPERTY_PRODUCT_NAME, productData.getProductName());
                properties.put(HueBindingConstants.PROPERTY_PRODUCT_ARCHETYPE,
                        productData.getProductArchetype().toString());
                properties.put(HueBindingConstants.PROPERTY_PRODUCT_CERTIFIED, productData.getCertified().toString());
            }

            thing.setProperties(properties);
            updatePropertiesDone = true;
        }
    }

    /**
     * Execute an HTTP GET command to fetch the resources data for the referenced resource.
     *
     * @param reference to the required resource.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    private void updateResource(ResourceReference reference) throws ApiException, AssetNotLoadedException {
        logger.debug("{} -> updateResource() from resource {}", resourceId, reference);
        getBridgeHandler().getResources(reference).getResources().stream().forEach(resource -> onResource(resource));
    }

    /**
     * Process the incoming list of scene resources to find those scenes which are associated with this thing. And if
     * there are any, include a scene channel in the supported channel list, populate its respective state options list,
     * and store the scenes in our temporary associated scenes list.
     *
     * @param scenes the full list of scene resources.
     */
    public synchronized void updateScenes(List<Resource> scenes) {
        if (!disposing && !updateScenesDone) {
            ResourceReference thisReference = getResourceReference();

            supportedScenes.clear();
            supportedScenes.addAll(scenes.stream().filter(scene -> thisReference.equals(scene.getGroup()))
                    .collect(Collectors.toList()));

            if (!supportedScenes.isEmpty()) {
                supportedChannelIds.add(HueBindingConstants.CHANNEL_SCENE);
                stateDescriptionProvider
                        .setStateOptions(new ChannelUID(thing.getUID(), HueBindingConstants.CHANNEL_SCENE),
                                supportedScenes.stream().map(scene -> scene.getName())
                                        .map(sceneId -> new StateOption(sceneId, sceneId))
                                        .collect(Collectors.toList()));
                logger.debug("{} -> updateScenes() found {} associated scenes", resourceId, supportedScenes.size());
            }
            updateScenesDone = true;
        }
    }

    /**
     * Update the channel state, and if appropriate add the channel id to the set of supportedChannelIds. Note: the
     * particular 'UnDefType.UNDEF' value of the state argument is used to specially indicate the undefined state, but
     * yet that its channel shall nevertheless continue to be present in the thing.
     *
     * @param channelID the id of the channel.
     * @param state the new state of the channel.
     * @param fullUpdate if true always update the channel, otherwise only update if state is not 'UNDEF'.
     */
    private void updateState(String channelID, State state, boolean fullUpdate) {
        logger.debug("{} -> updateState() channelID:{}, state:{}, fullUpdate:{}", resourceId, channelID, state,
                fullUpdate);
        boolean isDefined = state != UnDefType.NULL;
        if (fullUpdate || isDefined) {
            updateState(channelID, state);
        }
        if (fullUpdate && isDefined) {
            supportedChannelIds.add(channelID);
        }
    }

    /**
     * Check if a PROPERTY_LEGACY_THING_UID value was set by the discovery process, and if so, clone the legacy thing's
     * settings into this thing.
     */
    private void updateThingFromLegacy() {
        if (isInitialized()) {
            logger.warn("{} -> updateThingFromLegacy() was called after handler was initialized.", resourceId);
            return;
        }
        Map<String, String> properties = thing.getProperties();
        String legacyThingUID = properties.get(HueBindingConstants.PROPERTY_LEGACY_THING_UID);
        if (Objects.nonNull(legacyThingUID)) {
            Thing legacyThing = thingRegistry.get(new ThingUID(legacyThingUID));
            if (Objects.nonNull(legacyThing)) {
                ThingBuilder editBuilder = editThing();

                String location = legacyThing.getLocation();
                if (Objects.nonNull(location) && !location.isBlank()) {
                    editBuilder = editBuilder.withLocation(location);
                }

                // save list of legacyLinkedChannelUIDs for use after channel list is initialised
                legacyLinkedChannelUIDs.clear();
                legacyLinkedChannelUIDs.addAll(legacyThing.getChannels().stream().map(Channel::getUID)
                        .filter(uid -> HueBindingConstants.REPLICATE_CHANNEL_ID_MAP.containsKey(uid.getId())
                                && itemChannelLinkRegistry.isLinked(uid))
                        .collect(Collectors.toList()));

                Map<String, String> newProperties = new HashMap<>(properties);
                newProperties.remove(HueBindingConstants.PROPERTY_LEGACY_THING_UID);

                updateThing(editBuilder.withProperties(newProperties).build());
            }
        }
    }
}
