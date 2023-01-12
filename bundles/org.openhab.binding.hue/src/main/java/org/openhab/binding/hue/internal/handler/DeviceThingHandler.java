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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.config.DeviceConfig;
import org.openhab.binding.hue.internal.dto.clip2.ColorTemperature2;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigBeeStatus;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for things based on CLIP 2 device resources.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class DeviceThingHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(HueBindingConstants.THING_TYPE_DEVICE);

    private final Logger logger = LoggerFactory.getLogger(DeviceThingHandler.class);

    private Resource thisResource = new Resource(ResourceType.DEVICE);
    private final Map<String, Resource> contributorsCache = new ConcurrentHashMap<>();
    private final Map<ResourceType, String> commandResourceIds = new ConcurrentHashMap<>();
    private final Map<String, Integer> controlIds = new ConcurrentHashMap<>();
    private final Set<String> supportedChannelIds = ConcurrentHashMap.newKeySet(32);

    private boolean disposing;
    private boolean hasConnectivityIssue;
    private boolean updatePropertiesDone;
    private boolean updateDependenciesDone;

    private @Nullable ScheduledFuture<?> updateContributorsTask;

    public DeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        logger.debug("dispose() called");
        disposing = true;
        ScheduledFuture<?> task = updateContributorsTask;
        if (Objects.nonNull(task) && !task.isCancelled()) {
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

        Resource newResource;
        switch (channelUID.getId()) {
            case HueBindingConstants.CHANNEL_COLORTEMPERATURE:
                newResource = new Resource(ResourceType.LIGHT).setColorTemperaturePercent(command,
                        mirekSchemaFrom(ResourceType.LIGHT));
                break;

            case HueBindingConstants.CHANNEL_COLORTEMPERATURE_ABS:
                newResource = new Resource(ResourceType.LIGHT).setColorTemperatureKelvin(command);
                break;

            case HueBindingConstants.CHANNEL_COLOR:
                newResource = new Resource(ResourceType.LIGHT).setColor(command);
                break;

            case HueBindingConstants.CHANNEL_BRIGHTNESS:
                newResource = new Resource(ResourceType.LIGHT).setBrightness(command);
                break;

            case HueBindingConstants.CHANNEL_SWITCH:
                newResource = new Resource(ResourceType.LIGHT).setSwitch(command);
                break;

            case HueBindingConstants.CHANNEL_TEMPERATURE_ENABLED:
                newResource = new Resource(ResourceType.TEMPERATURE).setEnabled(command);
                break;

            case HueBindingConstants.CHANNEL_MOTION_ENABLED:
                newResource = new Resource(ResourceType.MOTION).setEnabled(command);
                break;

            case HueBindingConstants.CHANNEL_LIGHT_LEVEL_ENABLED:
                newResource = new Resource(ResourceType.LIGHT_LEVEL).setEnabled(command);
                break;

            default:
                return; // <= nota bene !!
        }

        String resourceId = commandResourceIds.get(newResource.getType());
        if (Objects.nonNull(resourceId)) {
            try {
                getBridgeHandler().putResource(newResource.setId(resourceId));
            } catch (ApiException | AssetNotLoadedException e) {
                logger.warn("handleCommand() error {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize() called");
        DeviceConfig config = getConfigAs(DeviceConfig.class);

        String resourceId = config.resourceId;
        if (Objects.isNull(resourceId) || resourceId.isEmpty()) {
            logger.debug("initialize() configuration resourceId is bad");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.clip2.conf-error-resource-id-bad");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        thisResource.setId(resourceId);

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
            } else {
                String cacheId = resource.getId();
                Resource cacheResource = contributorsCache.get(cacheId);
                if (Objects.nonNull(cacheResource)) {
                    resource.copyMissingFieldsFrom(cacheResource);
                    updateChannels(resource);
                    contributorsCache.put(cacheId, resource);
                }
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
                    supportedChannelIds.add(HueBindingConstants.CHANNEL_BUTTON_LAST_EVENT);
                }
                resource.addControlIdToMap(controlIds);
                State buttonState = resource.getButtonEventState(controlIds);
                updateState(HueBindingConstants.CHANNEL_BUTTON_LAST_EVENT, buttonState, fullUpdate);
                break;

            case DEVICE_POWER:
                updateState(HueBindingConstants.CHANNEL_BATTERY_LEVEL, resource.getBatteryLevelState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_BATTERY_LOW, resource.getBatteryLowState(), fullUpdate);
                break;

            case LIGHT:
                updateState(HueBindingConstants.CHANNEL_COLORTEMPERATURE,
                        resource.getColorTemperaturePercentState(mirekSchemaFrom(resource)), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_COLORTEMPERATURE_ABS, resource.getColorTemperatureKelvinState(),
                        fullUpdate);
                updateState(HueBindingConstants.CHANNEL_COLOR, resource.getColorState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_BRIGHTNESS, resource.getBrightnessState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_SWITCH, resource.getSwitch(), fullUpdate);
                break;

            case LIGHT_LEVEL:
                updateState(HueBindingConstants.CHANNEL_LIGHT_LEVEL, resource.getLightLevelState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_LIGHT_LEVEL_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case MOTION:
                updateState(HueBindingConstants.CHANNEL_MOTION, resource.getMotionState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_MOTION_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case RELATIVE_ROTARY:
                if (fullUpdate) {
                    supportedChannelIds.add(HueBindingConstants.CHANNEL_ROTARY_STEPS);
                }
                updateState(HueBindingConstants.CHANNEL_ROTARY_STEPS, resource.getRotaryStepsState(), fullUpdate);
                break;

            case TEMPERATURE:
                updateState(HueBindingConstants.CHANNEL_TEMPERATURE, resource.getTemperatureState(), fullUpdate);
                updateState(HueBindingConstants.CHANNEL_TEMPERATURE_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case ZIGBEE_CONNECTIVITY:
                updateConnectivityState(resource);
                updateState(HueBindingConstants.CHANNEL_ZIGBEE_STATUS, resource.getZigBeeState(), fullUpdate);
                break;

            default:
                return false;
        }
        updateState(HueBindingConstants.CHANNEL_LAST_UPDATED, new DateTimeType(), fullUpdate);
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
        ZigBeeStatus zigBeeStatus = resource.getZigBeeStatus();
        if (Objects.nonNull(zigBeeStatus)) {
            logger.debug("updateConnectivityState() thingStatus:{}, zigBeeStatus:{}", thing.getStatus(), zigBeeStatus);
            hasConnectivityIssue = zigBeeStatus != ZigBeeStatus.CONNECTED;
            if (hasConnectivityIssue) {
                if (thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.clip2.communication-error.zigbee-connectivity-issue");
                    // change all channel states, except the ZigBee channel itself, to undefined
                    for (String channelId : supportedChannelIds) {
                        if (!HueBindingConstants.CHANNEL_ZIGBEE_STATUS.equals(channelId)) {
                            updateState(channelId, UnDefType.UNDEF);
                        }
                    }
                }
            } else if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                // one single refresh command will update all channels
                Channel zigBeeChannel = thing.getChannel(HueBindingConstants.CHANNEL_ZIGBEE_STATUS);
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

        // actualise the properties
        Map<String, String> properties = new HashMap<>();

        // resource data
        properties.put(HueBindingConstants.PROPERTY_RESOURCE_ID, thisResource.getId());
        properties.put(HueBindingConstants.PROPERTY_RESOURCE_TYPE, thisResource.getType().toString());
        properties.put(HueBindingConstants.PROPERTY_RESOURCE_NAME, thisResource.getName());

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
     * Update the channel state, and if appropriate add the channel id to the set of supportedChannelIds.
     *
     * @param channelID the id of the channel.
     * @param state the new state of the channel.
     * @param fullUpdate if true always update the channel, otherwise only update if state is not 'UNDEF'.
     */
    private void updateState(String channelID, State state, boolean fullUpdate) {
        logger.debug("updateState() channelID:{}, state:{}, fullUpdate:{}", channelID, state, fullUpdate);
        boolean isDefined = state != UnDefType.UNDEF;
        if (fullUpdate || isDefined) {
            updateState(channelID, state);
        }
        if (fullUpdate && isDefined) {
            supportedChannelIds.add(channelID);
        }
    }
}
