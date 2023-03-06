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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.config.Clip2ThingConfig;
import org.openhab.binding.hue.internal.dto.clip2.ColorTemperature2;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.core.library.types.DateTimeType;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for things based on CLIP 2 'device' or 'grouped light' resources.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class Clip2ThingHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(HueBindingConstants.THING_TYPE_DEVICE,
            HueBindingConstants.THING_TYPE_GROUPED_LIGHT);

    private final Logger logger = LoggerFactory.getLogger(Clip2ThingHandler.class);

    /**
     * A cached map of associated resource DTO objects whose state contributes to the overall state of this thing. It is
     * a map between the resourceId (string) and an actual resource DTO object containing the last known state. e.g. the
     * state of a LIGHT resource contributes to the overall state of a DEVICE thing.
     */
    private final Map<String, Resource> contributorsCache = new ConcurrentHashMap<>();

    /**
     * A map of resourceIds which are targets for commands to be sent. It is a map between the type of command and the
     * resourceId to which the command shall be sent. e.g. a LIGHT on command shall be sent to the respective LIGHT
     * resourceId.
     */
    private final Map<ResourceType, String> commandResourceIds = new ConcurrentHashMap<>();

    /**
     * Button devices contain one or more physical buttons, each of which is represented by a BUTTON resource DTO with
     * its own unique resourceId, and a respective controlId that indicates which button it is in the device. e.g. a
     * dimmer pad has four buttons (controlId's 1..4) each represented by a BUTTON resource DTO with a unique
     * resourceId. This is a map between the resourceId and its respective controlId.
     */
    private final Map<String, Integer> controlIds = new ConcurrentHashMap<>();

    /**
     * This is the set of channel ids that are actually supported by this device. e.g. an on/off light may support
     * 'switch' and 'zigbeeStatus' channels, whereas a complex light may support 'switch', 'brightness', 'color', 'color
     * temperature' and 'zigbeeStatus' channels.
     */
    private final Set<String> supportedChannelIds = ConcurrentHashMap.newKeySet(32);

    /**
     * A list of v1 thing channel UIDs that are linked to items. It is used in the process of replicating the
     * Item/Channel links from a legacy v1 thing to this v2 thing.
     */
    private final List<ChannelUID> legacyLinkedChannelUIDs = new ArrayList<>();

    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    private Resource thisResource;

    private boolean disposing;
    private boolean hasConnectivityIssue;
    private boolean updatePropertiesDone;
    private boolean updateDependenciesDone;

    private @Nullable ScheduledFuture<?> updateContributorsTask;

    public Clip2ThingHandler(Thing thing, ThingRegistry thingRegistry,
            ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (HueBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.DEVICE);
        } else if (HueBindingConstants.THING_TYPE_GROUPED_LIGHT.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.GROUPED_LIGHT);
        } else {
            throw new IllegalArgumentException("Wrong thing type " + thingTypeUID.getAsString());
        }

        this.thingRegistry = thingRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void dispose() {
        logger.debug("dispose() called");
        disposing = true;
        ScheduledFuture<?> task = updateContributorsTask;
        if (Objects.nonNull(task)) {
            task.cancel(true);
        }
        updateContributorsTask = null;
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

        Resource putResource;
        ResourceType putResourceType = thisResource.getType() == ResourceType.DEVICE ? ResourceType.LIGHT
                : ResourceType.GROUPED_LIGHT;

        switch (channelUID.getId()) {
            case HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE:
                putResource = new Resource(putResourceType).setColorTemperaturePercent(command,
                        mirekSchemaFrom(putResourceType));
                break;

            case HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE_ABS:
                putResource = new Resource(putResourceType).setColorTemperatureKelvin(command);
                break;

            case HueBindingConstants.CHANNEL_COLOR:
                putResource = new Resource(putResourceType).setColor(command);
                break;

            case HueBindingConstants.CHANNEL_BRIGHTNESS:
                putResource = new Resource(putResourceType).setBrightness(command);
                break;

            case HueBindingConstants.CHANNEL_SWITCH:
                putResource = new Resource(putResourceType).setSwitch(command);
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

            default:
                logger.warn("handleCommand() unknown channel, channelUID:{}", channelUID);
                return; // <= nota bene !!
        }

        putResourceType = putResource.getType();
        String putResourceId = commandResourceIds.get(putResourceType);
        if (Objects.nonNull(putResourceId)) {
            try {
                getBridgeHandler().putResource(putResource.setId(putResourceId));
            } catch (ApiException | AssetNotLoadedException e) {
                logger.warn("handleCommand() error {}", e.getMessage(), e);
            }
        } else {
            logger.warn("handleCommand() resource not found, putResourceType:{}, channelUID:{}, command:{}",
                    putResourceType, channelUID, command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize() called");
        Clip2ThingConfig config = getConfigAs(Clip2ThingConfig.class);

        String resourceId = config.resourceId;
        if (Objects.isNull(resourceId) || resourceId.isEmpty()) {
            logger.debug("initialize() configuration resourceId is bad");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.clip2.conf-error-resource-id-bad");
            return;
        }
        thisResource.setId(resourceId);

        updateThingFromLegacy();
        updateStatus(ThingStatus.UNKNOWN);

        disposing = false;
        hasConnectivityIssue = false;
        updatePropertiesDone = false;
        updateDependenciesDone = false;
    }

    /**
     * Check the if the given resource has a MirekSchema, and if not check the contributors cache to see if it contains
     * a resource that matches the passed resource's id. In either case return that respective schema. And if not,
     * return a default schema comprising the default static mirek MIN and MAX constant values.
     *
     * @param resource the reference resource.
     * @return the MirekSchema.
     */
    private MirekSchema mirekSchemaFrom(Resource resource) {
        MirekSchema schema = resource.getMirekSchema();
        if (Objects.isNull(schema)) {
            Resource cacheResource = contributorsCache.get(resource.getId());
            if (Objects.nonNull(cacheResource)) {
                ColorTemperature2 colorTemperature = cacheResource.getColorTemperature();
                if (Objects.nonNull(colorTemperature)) {
                    schema = colorTemperature.getMirekSchema();
                }
            }
        }
        return Objects.nonNull(schema) ? schema : new MirekSchema();
    }

    /**
     * Check the commandResourceIds to see if we have a command resource id for the given resource type, and if so
     * return its respective MirekSchema, and if not return a default schema comprising the default static mirek MIN and
     * MAX constant values.
     *
     * @param resourceType the reference resource type.
     * @return the MirekSchema.
     */
    private MirekSchema mirekSchemaFrom(ResourceType resourceType) {
        String resourceId = commandResourceIds.get(resourceType);
        if (Objects.nonNull(resourceId)) {
            return mirekSchemaFrom(new Resource(resourceType).setId(resourceId));
        }
        return new MirekSchema();
    }

    /**
     * Update the channel state depending on a new resource sent from the bridge.
     *
     * @param resource a Resource object containing the new state.
     */
    public void onResource(Resource resource) {
        if (!disposing) {
            logger.debug("onResource(..) {}", resource);
            if (thisResource.getId().equals(resource.getId())) {
                if (resource.hasFullState()) {
                    thisResource = resource;
                    if (!updatePropertiesDone) {
                        updateProperties(resource);
                    }
                }
                if (!updateDependenciesDone) {
                    scheduler.submit(() -> updateDependencies());
                }
            }
            String cacheId = resource.getId();
            Resource cacheResource = contributorsCache.get(cacheId);
            if (Objects.nonNull(cacheResource)) {
                resource.copyMissingFieldsFrom(cacheResource);
                updateChannels(resource);
                contributorsCache.put(cacheId, resource);
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
            synchronized (legacyLinkedChannelUIDs) {
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
                                    logger.info("Created link between Channel:{} and Item:{}", uid, item);
                                    itemChannelLinkRegistry.add(new ItemChannelLink(item, uid));
                                }
                            });
                        }
                    }
                });
                legacyLinkedChannelUIDs.clear();
            }
        }
    }

    /**
     * Set the active list of channels by removing any that had initially been created by the thing XML declaration, but
     * which in fact did not have data returned from the bridge i.e. channels which are not in the supportedChannelIds
     * set.
     */
    private void updateChannelList() {
        if (!disposing) {
            logger.debug("updateChannelList() called");
            if (!supportedChannelIds.isEmpty()) {
                for (Channel channel : thing.getChannels()) {
                    String channelId = channel.getUID().getId();
                    if (!supportedChannelIds.contains(channelId)) {
                        logger.debug("setChannels() unused channel '{}' removed from {}", channelId, thing.getUID());
                        updateThing(editThing().withoutChannels(channel).build());
                    }
                }
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
        logger.debug("updateChannels() {}", resource);
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
            case GROUPED_LIGHT:
                updateState(HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE,
                        resource.getColorTemperaturePercentState(mirekSchemaFrom(resource)), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_COLOR_TEMPERATURE_ABS,
                        resource.getColorTemperatureKelvinState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_COLOR, resource.getColorState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_BRIGHTNESS, resource.getBrightnessState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_SWITCH, resource.getSwitch(), fullUpdate);
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
                updateState(HueBindingConstants.CHANNEL_TEMPERATURE, resource.getTemperatureState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_2_TEMPERATURE_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case ZIGBEE_CONNECTIVITY:
                updateConnectivityState(resource);
                updateState(HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS, resource.getZigBeeState(), fullUpdate);
                break;

            default:
                return false;
        }
        updateState(HueBindingConstants.CHANNEL_2_LAST_UPDATED, new DateTimeType(), fullUpdate);
        return true;
    }

    /**
     * Check the ZigBee connectivity and set the thing online status accordingly. If the thing is offline then set all
     * its channel states to undefined, otherwise execute a refresh command to update channels to the latest current
     * state.
     *
     * @param resource a Resource that potentially contains the ZigBee connectivity state.
     */
    private void updateConnectivityState(Resource resource) {
        ZigbeeStatus zigBeeStatus = resource.getZigBeeStatus();
        if (Objects.nonNull(zigBeeStatus)) {
            logger.debug("updateConnectivityState() thingStatus:{}, zigBeeStatus:{}", thing.getStatus(), zigBeeStatus);
            hasConnectivityIssue = zigBeeStatus != ZigbeeStatus.CONNECTED;
            if (hasConnectivityIssue) {
                if (thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.clip2.communication-error.zigbee-connectivity-issue");
                    // change all channel states, except the ZigBee channel itself, to undefined
                    for (String channelId : supportedChannelIds) {
                        if (!HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS.equals(channelId)) {
                            updateState(channelId, UnDefType.UNDEF);
                        }
                    }
                }
            } else if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                // one single refresh command will update all channels
                Channel zigBeeChannel = thing.getChannel(HueBindingConstants.CHANNEL_2_ZIGBEE_STATUS);
                if (Objects.nonNull(zigBeeChannel)) {
                    handleCommand(zigBeeChannel.getUID(), RefreshType.REFRESH);
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
        logger.debug("updateContributors() called for {} contributors", contributorsCache.size());
        ResourceReference reference = new ResourceReference();
        for (Entry<String, Resource> entry : contributorsCache.entrySet()) {
            updateResource(reference.setId(entry.getKey()).setType(entry.getValue().getType()));
        }
    }

    /**
     * Get all resources needed for building the thing state. Build the forward / reverse contributor lookup maps. Set
     * up the final list of channels in the thing.
     */
    private void updateDependencies() {
        if (!disposing && !updateDependenciesDone) {
            logger.debug("updateDependencies() called");
            try {
                updateLookups();
                updateContributors();
                updateChannelList();
                updateChannelItemLinksFromLegacy();
                updateDependenciesDone = true;
                if (!hasConnectivityIssue) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (ApiException e) {
                logger.debug("updateDependencies() {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error");
            } catch (AssetNotLoadedException e) {
                logger.debug("updateDependencies() {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.clip2.conf-error-assets-not-loaded");
            }
        }
    }

    /**
     * Initialize the lookup maps of resources that contribute to the thing state.
     */
    private void updateLookups() {
        if (!disposing) {
            logger.debug("updateLookups() called");
            List<ResourceReference> references = thisResource.getServiceReferences();
            if (thisResource.getType() == ResourceType.GROUPED_LIGHT) {
                // grouped light resource DTOs are directly their own contributor and command resources
                references = new ArrayList<>(references);
                references.add(new ResourceReference().setType(ResourceType.GROUPED_LIGHT).setId(thisResource.getId()));
            }
            contributorsCache.clear();
            contributorsCache.putAll(references.stream()
                    .collect(Collectors.toMap(ResourceReference::getId, r -> new Resource(r.getType()))));
            commandResourceIds.clear();
            commandResourceIds.putAll(references.stream() // use a 'mergeFunction' to prevent duplicates
                    .collect(Collectors.toMap(ResourceReference::getType, ResourceReference::getId, (r1, r2) -> r1)));
        }
    }

    /**
     * Update the primary device properties.
     *
     * @param resource a Resource object containing the property data.
     */
    private void updateProperties(Resource resource) {
        logger.debug("updateProperties() {}", resource);
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
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, productData.getSoftwareVersion().toString());
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

    /**
     * Execute an HTTP GET command to fetch the resources data for referenced resource.
     *
     * @param reference to the required resource.
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     */
    private void updateResource(ResourceReference reference) throws ApiException, AssetNotLoadedException {
        logger.debug("updateResource() {}", reference);
        for (Resource resource : getBridgeHandler().getResources(reference).getResources()) {
            onResource(resource);
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
        logger.debug("updateState() channelID:{}, state:{}, fullUpdate:{}", channelID, state, fullUpdate);
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
            logger.warn("updateThingFromLegacy() was called after handler was initialized.");
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
                synchronized (legacyLinkedChannelUIDs) {
                    legacyLinkedChannelUIDs.clear();
                    legacyLinkedChannelUIDs.addAll(legacyThing.getChannels().stream().map(Channel::getUID)
                            .filter(legacyChannelUID -> HueBindingConstants.REPLICATE_CHANNEL_ID_MAP.containsKey(
                                    legacyChannelUID.getId()) && itemChannelLinkRegistry.isLinked(legacyChannelUID))
                            .toList());
                }

                Map<String, String> newProperties = new HashMap<>(properties);
                newProperties.remove(HueBindingConstants.PROPERTY_LEGACY_THING_UID);

                updateThing(editBuilder.withProperties(newProperties).build());
            }
        }
    }
}
