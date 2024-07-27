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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.action.DynamicsActions;
import org.openhab.binding.hue.internal.api.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.Effects;
import org.openhab.binding.hue.internal.api.dto.clip2.Gamut2;
import org.openhab.binding.hue.internal.api.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.api.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.api.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.TimedEffects;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.EffectType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SceneRecallAction;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SmartSceneRecallAction;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.binding.hue.internal.config.Clip2ThingConfig;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DEVICE, THING_TYPE_ROOM,
            THING_TYPE_ZONE);

    private static final Set<ResourceType> SUPPORTED_SCENE_TYPES = Set.of(ResourceType.SCENE, ResourceType.SMART_SCENE);

    private static final Duration DYNAMICS_ACTIVE_WINDOW = Duration.ofSeconds(10);

    private static final String LK_WISER_DIMMER_MODEL_ID = "LK Dimmer";

    private final Logger logger = LoggerFactory.getLogger(Clip2ThingHandler.class);

    /**
     * A map of service Resources whose state contributes to the overall state of this thing. It is a map between the
     * resource ID (string) and a Resource object containing the last known state. e.g. a DEVICE thing may support a
     * LIGHT service whose Resource contributes to its overall state, or a ROOM or ZONE thing may support a
     * GROUPED_LIGHT service whose Resource contributes to the its overall state.
     */
    private final Map<String, Resource> serviceContributorsCache = new ConcurrentHashMap<>();

    /**
     * A map of Resource IDs which are targets for commands to be sent. It is a map between the type of command
     * (ResourcesType) and the resource ID to which the command shall be sent. e.g. a LIGHT 'on' command shall be sent
     * to the respective LIGHT resource ID.
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
     * The set of channel IDs that are supported by this thing. e.g. an on/off light may support 'switch' and
     * 'zigbeeStatus' channels, whereas a complex light may support 'switch', 'brightness', 'color', 'color temperature'
     * and 'zigbeeStatus' channels.
     */
    private final Set<String> supportedChannelIdSet = new HashSet<>();

    /**
     * A map of scene IDs versus scene Resources for the scenes that contribute to and command this thing. It is a map
     * between the resource ID (string) and a Resource object containing the scene's last known state.
     */
    private final Map<String, Resource> sceneContributorsCache = new ConcurrentHashMap<>();

    /**
     * A map of scene names versus scene Resources for the scenes that contribute to and command this thing. e.g. a
     * command for a scene named 'Energize' shall be sent to the respective SCENE resource ID.
     */
    private final Map<String, Resource> sceneResourceEntries = new ConcurrentHashMap<>();

    /**
     * A list of API v1 thing channel UIDs that are linked to items. It is used in the process of replicating the
     * Item/Channel links from a legacy v1 thing to this API v2 thing.
     */
    private final List<ChannelUID> legacyLinkedChannelUIDs = new CopyOnWriteArrayList<>();

    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;
    private final Clip2StateDescriptionProvider stateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;

    private String resourceId = "?";
    private Resource thisResource;
    private Duration dynamicsDuration = Duration.ZERO;
    private Instant dynamicsExpireTime = Instant.MIN;
    private Instant buttonGroupLastUpdated = Instant.MIN;

    private boolean disposing;
    private boolean hasConnectivityIssue;
    private boolean updateSceneContributorsDone;
    private boolean updateLightPropertiesDone;
    private boolean updatePropertiesDone;
    private boolean updateDependenciesDone;
    private boolean applyOffTransitionWorkaround;

    private @Nullable Future<?> alertResetTask;
    private @Nullable Future<?> dynamicsResetTask;
    private @Nullable Future<?> updateDependenciesTask;
    private @Nullable Future<?> updateServiceContributorsTask;

    public Clip2ThingHandler(Thing thing, Clip2StateDescriptionProvider stateDescriptionProvider,
            TimeZoneProvider timeZoneProvider, ThingRegistry thingRegistry,
            ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.DEVICE);
        } else if (THING_TYPE_ROOM.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.ROOM);
        } else if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            thisResource = new Resource(ResourceType.ZONE);
        } else {
            throw new IllegalArgumentException("Wrong thing type " + thingTypeUID.getAsString());
        }

        this.thingRegistry = thingRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Add a channel ID to the supportedChannelIdSet set. If the channel supports dynamics (timed transitions) then add
     * the respective channel as well.
     *
     * @param channelId the channel ID to add.
     */
    private void addSupportedChannel(String channelId) {
        if (!disposing && !updateDependenciesDone) {
            synchronized (supportedChannelIdSet) {
                logger.debug("{} -> addSupportedChannel() '{}' added to supported channel set", resourceId, channelId);
                supportedChannelIdSet.add(channelId);
                if (DYNAMIC_CHANNELS.contains(channelId)) {
                    clearDynamicsChannel();
                }
            }
        }
    }

    /**
     * Cancel the given task.
     *
     * @param cancelTask the task to be cancelled (may be null)
     * @param mayInterrupt allows cancel() to interrupt the thread.
     */
    private void cancelTask(@Nullable Future<?> cancelTask, boolean mayInterrupt) {
        if (Objects.nonNull(cancelTask)) {
            cancelTask.cancel(mayInterrupt);
        }
    }

    /**
     * Clear the dynamics channel parameters.
     */
    private void clearDynamicsChannel() {
        dynamicsExpireTime = Instant.MIN;
        dynamicsDuration = Duration.ZERO;
        updateState(CHANNEL_2_DYNAMICS, new QuantityType<>(0, MetricPrefix.MILLI(Units.SECOND)), true);
    }

    @Override
    public void dispose() {
        logger.debug("{} -> dispose()", resourceId);
        disposing = true;
        cancelTask(alertResetTask, true);
        cancelTask(dynamicsResetTask, true);
        cancelTask(updateDependenciesTask, true);
        cancelTask(updateServiceContributorsTask, true);
        alertResetTask = null;
        dynamicsResetTask = null;
        updateDependenciesTask = null;
        updateServiceContributorsTask = null;
        legacyLinkedChannelUIDs.clear();
        sceneContributorsCache.clear();
        sceneResourceEntries.clear();
        supportedChannelIdSet.clear();
        commandResourceIds.clear();
        serviceContributorsCache.clear();
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
        return Objects.nonNull(commandResourceId) ? serviceContributorsCache.get(commandResourceId) : null;
    }

    /**
     * Return a ResourceReference to this handler's resource.
     *
     * @return a ResourceReference instance.
     */
    public ResourceReference getResourceReference() {
        return new ResourceReference().setId(resourceId).setType(thisResource.getType());
    }

    /**
     * Register the 'DynamicsAction' service.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(DynamicsActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command commandParam) {
        if (RefreshType.REFRESH.equals(commandParam)) {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                refreshAllChannels();
            }
            return;
        }

        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} -> handleCommand() channelUID:{} does not exist", resourceId, channelUID);

            } else {
                logger.warn("Command received for channel '{}' which is not in thing '{}'.", channelUID,
                        thing.getUID());
            }
            return;
        }

        ResourceType lightResourceType = thisResource.getType() == ResourceType.DEVICE ? ResourceType.LIGHT
                : ResourceType.GROUPED_LIGHT;

        Resource putResource = null;
        String putResourceId = null;
        Command command = commandParam;
        String channelId = channelUID.getId();
        Resource cache = getCachedResource(lightResourceType);

        switch (channelId) {
            case CHANNEL_2_ALERT:
                putResource = Setters.setAlert(new Resource(lightResourceType), command, cache);
                cancelTask(alertResetTask, false);
                alertResetTask = scheduler.schedule(
                        () -> updateState(channelUID, new StringType(ActionType.NO_ACTION.name())), 10,
                        TimeUnit.SECONDS);
                break;

            case CHANNEL_2_EFFECT:
                putResource = Setters.setEffect(new Resource(lightResourceType), command, cache).setOnOff(OnOffType.ON);
                break;

            case CHANNEL_2_COLOR_TEMP_PERCENT:
                if (command instanceof IncreaseDecreaseType increaseDecreaseCommand && Objects.nonNull(cache)) {
                    command = translateIncreaseDecreaseCommand(increaseDecreaseCommand,
                            cache.getColorTemperaturePercentState());
                } else if (command instanceof OnOffType) {
                    command = OnOffType.OFF == command ? PercentType.ZERO : PercentType.HUNDRED;
                }
                putResource = Setters.setColorTemperaturePercent(new Resource(lightResourceType), command, cache);
                break;

            case CHANNEL_2_COLOR_TEMP_ABSOLUTE:
                putResource = Setters.setColorTemperatureAbsolute(new Resource(lightResourceType), command, cache);
                break;

            case CHANNEL_2_COLOR:
                putResource = new Resource(lightResourceType);
                if (command instanceof HSBType) {
                    HSBType color = ((HSBType) command);
                    putResource = Setters.setColorXy(putResource, color, cache);
                    command = color.getBrightness();
                }
                // NB fall through for handling of brightness and switch related commands !!

            case CHANNEL_2_BRIGHTNESS:
                putResource = Objects.nonNull(putResource) ? putResource : new Resource(lightResourceType);
                if (command instanceof IncreaseDecreaseType increaseDecreaseCommand && Objects.nonNull(cache)) {
                    command = translateIncreaseDecreaseCommand(increaseDecreaseCommand, cache.getBrightnessState());
                }
                if (command instanceof PercentType) {
                    PercentType brightness = (PercentType) command;
                    putResource = Setters.setDimming(putResource, brightness, cache);
                    Double minDimLevel = Objects.nonNull(cache) ? cache.getMinimumDimmingLevel() : null;
                    minDimLevel = Objects.nonNull(minDimLevel) ? minDimLevel : Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL;
                    command = OnOffType.from(brightness.doubleValue() >= minDimLevel);
                }
                // NB fall through for handling of switch related commands !!

            case CHANNEL_2_SWITCH:
                putResource = Objects.nonNull(putResource) ? putResource : new Resource(lightResourceType);
                putResource.setOnOff(command);
                applyDeviceSpecificWorkArounds(command, putResource);
                break;

            case CHANNEL_2_COLOR_XY_ONLY:
                putResource = Setters.setColorXy(new Resource(lightResourceType), command, cache);
                break;

            case CHANNEL_2_DIMMING_ONLY:
                putResource = Setters.setDimming(new Resource(lightResourceType), command, cache);
                break;

            case CHANNEL_2_ON_OFF_ONLY:
                putResource = new Resource(lightResourceType).setOnOff(command);
                applyDeviceSpecificWorkArounds(command, putResource);
                break;

            case CHANNEL_2_TEMPERATURE_ENABLED:
                putResource = new Resource(ResourceType.TEMPERATURE).setEnabled(command);
                break;

            case CHANNEL_2_MOTION_ENABLED:
                putResource = new Resource(ResourceType.MOTION).setEnabled(command);
                break;

            case CHANNEL_2_LIGHT_LEVEL_ENABLED:
                putResource = new Resource(ResourceType.LIGHT_LEVEL).setEnabled(command);
                break;

            case CHANNEL_2_SECURITY_CONTACT_ENABLED:
                putResource = new Resource(ResourceType.CONTACT).setEnabled(command);
                break;

            case CHANNEL_2_SCENE:
                if (command instanceof StringType) {
                    Resource scene = sceneResourceEntries.get(((StringType) command).toString());
                    if (Objects.nonNull(scene)) {
                        ResourceType putResourceType = scene.getType();
                        putResource = new Resource(putResourceType);
                        switch (putResourceType) {
                            case SCENE:
                                putResource.setRecallAction(SceneRecallAction.ACTIVE);
                                break;
                            case SMART_SCENE:
                                putResource.setRecallAction(SmartSceneRecallAction.ACTIVATE);
                                break;
                            default:
                                logger.debug("{} -> handleCommand() type '{}' is not a supported scene type",
                                        resourceId, putResourceType);
                                return;
                        }
                        putResourceId = scene.getId();
                    }
                }
                break;

            case CHANNEL_2_DYNAMICS:
                Duration clearAfter = Duration.ZERO;
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> durationMs = ((QuantityType<?>) command).toUnit(MetricPrefix.MILLI(Units.SECOND));
                    if (Objects.nonNull(durationMs) && durationMs.longValue() > 0) {
                        Duration duration = Duration.ofMillis(durationMs.longValue());
                        dynamicsDuration = duration;
                        dynamicsExpireTime = Instant.now().plus(DYNAMICS_ACTIVE_WINDOW);
                        clearAfter = DYNAMICS_ACTIVE_WINDOW;
                        logger.debug("{} -> handleCommand() dynamics setting {} valid for {}", resourceId, duration,
                                clearAfter);
                    }
                }
                cancelTask(dynamicsResetTask, false);
                dynamicsResetTask = scheduler.schedule(() -> clearDynamicsChannel(), clearAfter.toMillis(),
                        TimeUnit.MILLISECONDS);
                return;

            default:
                if (logger.isDebugEnabled()) {
                    logger.debug("{} -> handleCommand() channelUID:{} unknown", resourceId, channelUID);
                } else {
                    logger.warn("Command received for unknown channel '{}'.", channelUID);
                }
                return;
        }

        if (putResource == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} -> handleCommand() command:{} not supported on channelUID:{}", resourceId, command,
                        channelUID);
            } else {
                logger.warn("Command '{}' is not supported on channel '{}'.", command, channelUID);
            }
            return;
        }

        putResourceId = Objects.nonNull(putResourceId) ? putResourceId : commandResourceIds.get(putResource.getType());
        if (putResourceId == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "{} -> handleCommand() channelUID:{}, command:{}, putResourceType:{} => missing resource ID",
                        resourceId, channelUID, command, putResource.getType());
            } else {
                logger.warn("Command '{}' for channel '{}' cannot be processed by thing '{}'.", command, channelUID,
                        thing.getUID());
            }
            return;
        }

        if (DYNAMIC_CHANNELS.contains(channelId)) {
            if (Instant.now().isBefore(dynamicsExpireTime) && !dynamicsDuration.isZero()
                    && !dynamicsDuration.isNegative()) {
                if (ResourceType.SCENE == putResource.getType()) {
                    putResource.setRecallDuration(dynamicsDuration);
                } else if (CHANNEL_2_EFFECT == channelId) {
                    putResource.setTimedEffectsDuration(dynamicsDuration);
                } else {
                    putResource.setDynamicsDuration(dynamicsDuration);
                }
            }
        }

        putResource.setId(putResourceId);
        logger.debug("{} -> handleCommand() put resource {}", resourceId, putResource);

        try {
            Resources resources = getBridgeHandler().putResource(putResource);
            if (resources.hasErrors()) {
                logger.info("Command '{}' for thing '{}', channel '{}' succeeded with errors: {}", command,
                        thing.getUID(), channelUID, String.join("; ", resources.getErrors()));
            }
        } catch (ApiException | AssetNotLoadedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} -> handleCommand() error {}", resourceId, e.getMessage(), e);
            } else {
                logger.warn("Command '{}' for thing '{}', channel '{}' failed with error '{}'.", command,
                        thing.getUID(), channelUID, e.getMessage());
            }
        } catch (InterruptedException e) {
        }
    }

    private Command translateIncreaseDecreaseCommand(IncreaseDecreaseType command, State currentValue) {
        if (currentValue instanceof PercentType currentPercent) {
            int delta = command == IncreaseDecreaseType.INCREASE ? 10 : -10;
            double newPercent = Math.min(100.0, Math.max(0.0, currentPercent.doubleValue() + delta));
            return new PercentType(new BigDecimal(newPercent, Resource.PERCENT_MATH_CONTEXT));
        }

        return command;
    }

    private void refreshAllChannels() {
        if (!updateDependenciesDone) {
            return;
        }
        cancelTask(updateServiceContributorsTask, false);
        updateServiceContributorsTask = scheduler.schedule(() -> {
            try {
                updateServiceContributors();
            } catch (ApiException | AssetNotLoadedException e) {
                logger.debug("{} -> handleCommand() error {}", resourceId, e.getMessage(), e);
            } catch (InterruptedException e) {
            }
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * Apply device specific work-arounds needed for given command.
     *
     * @param command the handled command.
     * @param putResource the resource that will be adjusted if needed.
     */
    private void applyDeviceSpecificWorkArounds(Command command, Resource putResource) {
        if (command == OnOffType.OFF && applyOffTransitionWorkaround) {
            putResource.setDynamicsDuration(dynamicsDuration);
        }
    }

    /**
     * Handle a 'dynamics' command for the given channel ID for the given dynamics duration.
     *
     * @param channelId the ID of the target channel.
     * @param command the new target state.
     * @param duration the transition duration.
     */
    public synchronized void handleDynamicsCommand(String channelId, Command command, QuantityType<?> duration) {
        if (DYNAMIC_CHANNELS.contains(channelId)) {
            Channel dynamicsChannel = thing.getChannel(CHANNEL_2_DYNAMICS);
            Channel targetChannel = thing.getChannel(channelId);
            if (Objects.nonNull(dynamicsChannel) && Objects.nonNull(targetChannel)) {
                logger.debug("{} - handleDynamicsCommand() channelId:{}, command:{}, duration:{}", resourceId,
                        channelId, command, duration);
                handleCommand(dynamicsChannel.getUID(), duration);
                handleCommand(targetChannel.getUID(), command);
                return;
            }
        }
        logger.warn("Dynamics command '{}' for thing '{}', channel '{}' and duration'{}' failed.", command,
                thing.getUID(), channelId, duration);
    }

    @Override
    public void initialize() {
        Clip2ThingConfig config = getConfigAs(Clip2ThingConfig.class);

        String resourceId = config.resourceId;
        if (resourceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.api2.conf-error.resource-id-missing");
            return;
        }
        thisResource.setId(resourceId);
        this.resourceId = resourceId;
        logger.debug("{} -> initialize()", resourceId);

        updateThingFromLegacy();
        updateStatus(ThingStatus.UNKNOWN);

        dynamicsDuration = Duration.ZERO;
        dynamicsExpireTime = Instant.MIN;

        disposing = false;
        hasConnectivityIssue = false;
        updatePropertiesDone = false;
        updateDependenciesDone = false;
        updateLightPropertiesDone = false;
        updateSceneContributorsDone = false;

        Bridge bridge = getBridge();
        if (Objects.nonNull(bridge)) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof Clip2BridgeHandler) {
                ((Clip2BridgeHandler) bridgeHandler).childInitialized();
            }
        }
    }

    /**
     * Update the channel state depending on new resources sent from the bridge.
     *
     * @param resources a collection of Resource objects containing the new state.
     */
    public void onResources(Collection<Resource> resources) {
        boolean sceneActivated = resources.stream().anyMatch(r -> sceneContributorsCache.containsKey(r.getId())
                && (r.getSceneActive().orElse(false) || r.getSmartSceneActive().orElse(false)));
        for (Resource resource : resources) {
            // Skip scene deactivation when we have also received a scene activation.
            boolean updateChannels = !sceneActivated || !sceneContributorsCache.containsKey(resource.getId())
                    || resource.getSceneActive().orElse(false) || resource.getSmartSceneActive().orElse(false);
            onResource(resource, updateChannels);
        }
    }

    /**
     * Update the channel state depending on a new resource sent from the bridge.
     *
     * @param resource a Resource object containing the new state.
     */
    private void onResource(Resource resource) {
        onResource(resource, true);
    }

    /**
     * Update the channel state depending on a new resource sent from the bridge.
     *
     * @param resource a Resource object containing the new state.
     * @param updateChannels update channels (otherwise only update cache/properties).
     */
    private void onResource(Resource resource, boolean updateChannels) {
        if (disposing) {
            return;
        }
        boolean resourceConsumed = false;
        if (resourceId.equals(resource.getId())) {
            if (resource.hasFullState()) {
                thisResource = resource;
                if (!updatePropertiesDone) {
                    updateProperties(resource);
                    resourceConsumed = updatePropertiesDone;
                }
            }
            if (!updateDependenciesDone) {
                resourceConsumed = true;
                cancelTask(updateDependenciesTask, false);
                updateDependenciesTask = scheduler.submit(() -> updateDependencies());
            }
        } else {
            Resource cachedResource = getResourceFromCache(resource);
            if (cachedResource != null) {
                Setters.setResource(resource, cachedResource);
                resourceConsumed = updateChannels && updateChannels(resource);
                putResourceToCache(resource);
                if (ResourceType.LIGHT == resource.getType() && !updateLightPropertiesDone) {
                    updateLightProperties(resource);
                }
            }
        }
        if (resourceConsumed) {
            logger.debug("{} -> onResource() consumed resource {}", resourceId, resource);
        }
    }

    private void putResourceToCache(Resource resource) {
        if (SUPPORTED_SCENE_TYPES.contains(resource.getType())) {
            sceneContributorsCache.put(resource.getId(), resource);
        } else {
            serviceContributorsCache.put(resource.getId(), resource);
        }
    }

    private @Nullable Resource getResourceFromCache(Resource resource) {
        return SUPPORTED_SCENE_TYPES.contains(resource.getType()) //
                ? sceneContributorsCache.get(resource.getId())
                : serviceContributorsCache.get(resource.getId());
    }

    /**
     * Update the thing internal state depending on a full list of resources sent from the bridge. If the resourceType
     * is SCENE then call updateScenes(), otherwise if the resource refers to this thing, consume it via onResource() as
     * any other resource, or else if the resourceType nevertheless matches the thing type, set the thing state offline.
     *
     * @param resourceType the type of the resources in the list.
     * @param fullResources the full list of resources of the given type.
     */
    public void onResourcesList(ResourceType resourceType, List<Resource> fullResources) {
        if (resourceType == ResourceType.SCENE) {
            updateSceneContributors(fullResources);
        } else {
            fullResources.stream().filter(r -> resourceId.equals(r.getId())).findAny()
                    .ifPresentOrElse(r -> onResource(r), () -> {
                        if (resourceType == thisResource.getType()) {
                            logger.debug("{} -> onResourcesList() configuration error: unknown resourceId", resourceId);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                                    "@text/offline.api2.gone.resource-id-unknown");
                        }
                    });
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
                stateDescriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_2_ALERT), stateOptions);
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
                String targetChannelId = REPLICATE_CHANNEL_ID_MAP.get(legacyLinkedChannelUID.getId());
                if (Objects.nonNull(targetChannelId)) {
                    Channel targetChannel = thing.getChannel(targetChannelId);
                    if (Objects.nonNull(targetChannel)) {
                        ChannelUID uid = targetChannel.getUID();
                        itemChannelLinkRegistry.getLinkedItems(legacyLinkedChannelUID).forEach(linkedItem -> {
                            String item = linkedItem.getName();
                            if (!itemChannelLinkRegistry.isLinked(item, uid)) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "{} -> updateChannelItemLinksFromLegacy() item:{} linked to channel:{}",
                                            resourceId, item, uid);
                                } else {
                                    logger.info("Item '{}' linked to thing '{}' channel '{}'", item, thing.getUID(),
                                            targetChannelId);
                                }
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
     * which in fact did not have data returned from the bridge i.e. channels which are not in the supportedChannelIdSet
     *
     * Also warn if there are channels in the supportedChannelIdSet set which are not in the thing.
     *
     * Adjusts the channel list so that only the highest level channel is available in the normal channel list. If a
     * light supports the color channel, then it's brightness and switch can be commanded via the 'B' part of the HSB
     * channel value. And if it supports the brightness channel the switch can be controlled via the brightness. So we
     * can remove these lower level channels from the normal channel list.
     *
     * For more advanced applications, it is necessary to orthogonally command the color xy parameter, dimming
     * parameter, and/or on/off parameter independently. So we add corresponding advanced level 'CHANNEL_2_BLAH_ONLY'
     * channels for that purpose. Since they are advanced level, normal users should normally not be confused by them,
     * yet advanced users can use them nevertheless.
     */
    private void updateChannelList() {
        if (!disposing) {
            synchronized (supportedChannelIdSet) {
                logger.debug("{} -> updateChannelList()", resourceId);

                if (supportedChannelIdSet.contains(CHANNEL_2_COLOR)) {
                    supportedChannelIdSet.add(CHANNEL_2_COLOR_XY_ONLY);
                    //
                    supportedChannelIdSet.remove(CHANNEL_2_BRIGHTNESS);
                    supportedChannelIdSet.add(CHANNEL_2_DIMMING_ONLY);
                    //
                    supportedChannelIdSet.remove(CHANNEL_2_SWITCH);
                    supportedChannelIdSet.add(CHANNEL_2_ON_OFF_ONLY);
                }
                if (supportedChannelIdSet.contains(CHANNEL_2_BRIGHTNESS)) {
                    supportedChannelIdSet.add(CHANNEL_2_DIMMING_ONLY);
                    //
                    supportedChannelIdSet.remove(CHANNEL_2_SWITCH);
                    supportedChannelIdSet.add(CHANNEL_2_ON_OFF_ONLY);
                }
                if (supportedChannelIdSet.contains(CHANNEL_2_SWITCH)) {
                    supportedChannelIdSet.add(CHANNEL_2_ON_OFF_ONLY);
                }

                /*
                 * This binding creates its dynamic list of channels by a 'subtractive' method i.e. the full set of
                 * channels is initially created from the thing type xml, and then for any channels where UndfType.NULL
                 * data is returned, the respective channel is removed from the full list. However in seldom cases
                 * UndfType.NULL may wrongly be returned, so we should log a warning here just in case.
                 */
                if (logger.isDebugEnabled()) {
                    supportedChannelIdSet.stream().filter(channelId -> Objects.isNull(thing.getChannel(channelId)))
                            .forEach(channelId -> logger.debug(
                                    "{} -> updateChannelList() required channel '{}' missing", resourceId, channelId));
                } else {
                    supportedChannelIdSet.stream().filter(channelId -> Objects.isNull(thing.getChannel(channelId)))
                            .forEach(channelId -> logger.warn(
                                    "Thing '{}' is missing required channel '{}'. Please recreate the thing!",
                                    thing.getUID(), channelId));
                }

                // get list of unused channels
                List<Channel> unusedChannels = thing.getChannels().stream()
                        .filter(channel -> !supportedChannelIdSet.contains(channel.getUID().getId()))
                        .collect(Collectors.toList());

                // remove any unused channels
                if (!unusedChannels.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        unusedChannels.stream().map(channel -> channel.getUID().getId())
                                .forEach(channelId -> logger.debug(
                                        "{} -> updateChannelList() removing unused channel '{}'", resourceId,
                                        channelId));
                    }
                    updateThing(editThing().withoutChannels(unusedChannels).build());
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
        logger.debug("{} -> updateChannels() from resource {}", resourceId, resource);
        boolean fullUpdate = resource.hasFullState();
        switch (resource.getType()) {
            case BUTTON:
                if (fullUpdate) {
                    addSupportedChannel(CHANNEL_2_BUTTON_LAST_EVENT);
                    addSupportedChannel(CHANNEL_2_BUTTON_LAST_UPDATED);
                    controlIds.put(resource.getId(), resource.getControlId());
                } else {
                    State buttonState = resource.getButtonEventState(controlIds);
                    updateState(CHANNEL_2_BUTTON_LAST_EVENT, buttonState, fullUpdate);
                }
                // Update channel from timestamp if last button pressed.
                State buttonLastUpdatedState = resource.getButtonLastUpdatedState(timeZoneProvider.getTimeZone());
                if (buttonLastUpdatedState instanceof DateTimeType) {
                    Instant buttonLastUpdatedInstant = ((DateTimeType) buttonLastUpdatedState).getInstant();
                    if (buttonLastUpdatedInstant.isAfter(buttonGroupLastUpdated)) {
                        updateState(CHANNEL_2_BUTTON_LAST_UPDATED, buttonLastUpdatedState, fullUpdate);
                        buttonGroupLastUpdated = buttonLastUpdatedInstant;
                    }
                } else if (Instant.MIN.equals(buttonGroupLastUpdated)) {
                    updateState(CHANNEL_2_BUTTON_LAST_UPDATED, buttonLastUpdatedState, fullUpdate);
                }
                break;

            case DEVICE_POWER:
                updateState(CHANNEL_2_BATTERY_LEVEL, resource.getBatteryLevelState(), fullUpdate);
                updateState(CHANNEL_2_BATTERY_LOW, resource.getBatteryLowState(), fullUpdate);
                break;

            case LIGHT:
                if (fullUpdate) {
                    updateEffectChannel(resource);
                }
                updateState(CHANNEL_2_COLOR_TEMP_PERCENT, resource.getColorTemperaturePercentState(), fullUpdate);
                updateState(CHANNEL_2_COLOR_TEMP_ABSOLUTE, resource.getColorTemperatureAbsoluteState(), fullUpdate);
                updateState(CHANNEL_2_COLOR, resource.getColorState(), fullUpdate);
                updateState(CHANNEL_2_COLOR_XY_ONLY, resource.getColorXyState(), fullUpdate);
                updateState(CHANNEL_2_EFFECT, resource.getEffectState(), fullUpdate);
                // fall through for dimming and on/off related channels

            case GROUPED_LIGHT:
                if (fullUpdate) {
                    updateAlertChannel(resource);
                }
                updateState(CHANNEL_2_BRIGHTNESS, resource.getBrightnessState(), fullUpdate);
                updateState(CHANNEL_2_DIMMING_ONLY, resource.getDimmingState(), fullUpdate);
                updateState(CHANNEL_2_SWITCH, resource.getOnOffState(), fullUpdate);
                updateState(CHANNEL_2_ON_OFF_ONLY, resource.getOnOffState(), fullUpdate);
                updateState(CHANNEL_2_ALERT, resource.getAlertState(), fullUpdate);
                break;

            case LIGHT_LEVEL:
                updateState(CHANNEL_2_LIGHT_LEVEL, resource.getLightLevelState(), fullUpdate);
                updateState(CHANNEL_2_LIGHT_LEVEL_LAST_UPDATED,
                        resource.getLightLevelLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                updateState(CHANNEL_2_LIGHT_LEVEL_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case MOTION:
            case CAMERA_MOTION:
                updateState(CHANNEL_2_MOTION, resource.getMotionState(), fullUpdate);
                updateState(CHANNEL_2_MOTION_LAST_UPDATED,
                        resource.getMotionLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                updateState(CHANNEL_2_MOTION_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case RELATIVE_ROTARY:
                if (fullUpdate) {
                    addSupportedChannel(CHANNEL_2_ROTARY_STEPS);
                    addSupportedChannel(CHANNEL_2_ROTARY_STEPS_LAST_UPDATED);
                } else {
                    updateState(CHANNEL_2_ROTARY_STEPS, resource.getRotaryStepsState(), fullUpdate);
                }
                updateState(CHANNEL_2_ROTARY_STEPS_LAST_UPDATED,
                        resource.getRotaryStepsLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                break;

            case TEMPERATURE:
                updateState(CHANNEL_2_TEMPERATURE, resource.getTemperatureState(), fullUpdate);
                updateState(CHANNEL_2_TEMPERATURE_LAST_UPDATED,
                        resource.getTemperatureLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                updateState(CHANNEL_2_TEMPERATURE_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case ZIGBEE_CONNECTIVITY:
                updateConnectivityState(resource);
                break;

            case SCENE:
                updateState(CHANNEL_2_SCENE, resource.getSceneState(), fullUpdate);
                break;

            case CONTACT:
                updateState(CHANNEL_2_SECURITY_CONTACT, resource.getContactState(), fullUpdate);
                updateState(CHANNEL_2_SECURITY_CONTACT_LAST_UPDATED,
                        resource.getContactLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                updateState(CHANNEL_2_SECURITY_CONTACT_ENABLED, resource.getEnabledState(), fullUpdate);
                break;

            case TAMPER:
                updateState(CHANNEL_2_SECURITY_TAMPER, resource.getTamperState(), fullUpdate);
                updateState(CHANNEL_2_SECURITY_TAMPER_LAST_UPDATED,
                        resource.getTamperLastUpdatedState(timeZoneProvider.getTimeZone()), fullUpdate);
                break;

            case SMART_SCENE:
                updateState(CHANNEL_2_SCENE, resource.getSmartSceneState(), fullUpdate);
                break;

            default:
                return false;
        }
        if (thisResource.getType() == ResourceType.DEVICE) {
            updateState(CHANNEL_2_LAST_UPDATED, new DateTimeType(), fullUpdate);
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
                    supportedChannelIdSet.forEach(channelId -> updateState(channelId, UnDefType.UNDEF));
                }
            } else if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                refreshAllChannels();
            }
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
                if (!updatePropertiesDone) {
                    logger.debug("{} -> updateDependencies() properties not initialized", resourceId);
                    return;
                }
                if (!updateSceneContributorsDone && !updateSceneContributors()) {
                    logger.debug("{} -> updateDependencies() scenes not initialized", resourceId);
                    return;
                }
                updateLookups();
                updateServiceContributors();
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
                        "@text/offline.api2.conf-error.assets-not-loaded");
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Process the incoming Resource to initialize the fixed resp. timed effects channel.
     *
     * @param resource a Resource possibly containing a fixed and/or timed effects element.
     */
    public void updateEffectChannel(Resource resource) {
        Effects fixedEffects = resource.getFixedEffects();
        TimedEffects timedEffects = resource.getTimedEffects();
        List<StateOption> stateOptions = Stream
                .concat(Objects.nonNull(fixedEffects) ? fixedEffects.getStatusValues().stream() : Stream.empty(),
                        Objects.nonNull(timedEffects) ? timedEffects.getStatusValues().stream() : Stream.empty())
                .map(effect -> {
                    String effectName = EffectType.of(effect).name();
                    return new StateOption(effectName, effectName);
                }).distinct().collect(Collectors.toList());
        if (!stateOptions.isEmpty()) {
            stateDescriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_2_EFFECT), stateOptions);
            logger.debug("{} -> updateEffects() found {} effects", resourceId, stateOptions.size());
        }
    }

    /**
     * Update the light properties.
     *
     * @param resource a Resource object containing the property data.
     */
    private synchronized void updateLightProperties(Resource resource) {
        if (!disposing && !updateLightPropertiesDone) {
            logger.debug("{} -> updateLightProperties()", resourceId);

            Dimming dimming = resource.getDimming();
            thing.setProperty(PROPERTY_DIMMING_RANGE, Objects.nonNull(dimming) ? dimming.toPropertyValue() : null);

            MirekSchema mirekSchema = resource.getMirekSchema();
            thing.setProperty(PROPERTY_COLOR_TEMP_RANGE,
                    Objects.nonNull(mirekSchema) ? mirekSchema.toPropertyValue() : null);

            ColorXy colorXy = resource.getColorXy();
            Gamut2 gamut = Objects.nonNull(colorXy) ? colorXy.getGamut2() : null;
            thing.setProperty(PROPERTY_COLOR_GAMUT, Objects.nonNull(gamut) ? gamut.toPropertyValue() : null);

            updateLightPropertiesDone = true;
        }
    }

    /**
     * Initialize the lookup maps of resources that contribute to the thing state.
     */
    private void updateLookups() {
        if (!disposing) {
            logger.debug("{} -> updateLookups()", resourceId);
            // get supported services
            List<ResourceReference> services = thisResource.getServiceReferences();

            // add supported services to contributorsCache
            serviceContributorsCache.clear();
            serviceContributorsCache.putAll(services.stream()
                    .collect(Collectors.toMap(ResourceReference::getId, r -> new Resource(r.getType()))));

            // add supported services to commandResourceIds
            commandResourceIds.clear();
            commandResourceIds.putAll(services.stream() // use a 'mergeFunction' to prevent duplicates
                    .collect(Collectors.toMap(ResourceReference::getType, ResourceReference::getId, (r1, r2) -> r1)));
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
            properties.put(PROPERTY_RESOURCE_TYPE, thisResource.getType().toString());
            properties.put(PROPERTY_RESOURCE_NAME, thisResource.getName());

            // owner information
            ResourceReference owner = thisResource.getOwner();
            if (Objects.nonNull(owner)) {
                String ownerId = owner.getId();
                if (Objects.nonNull(ownerId)) {
                    properties.put(PROPERTY_OWNER, ownerId);
                }
                ResourceType ownerType = owner.getType();
                properties.put(PROPERTY_OWNER_TYPE, ownerType.toString());
            }

            // metadata
            MetaData metaData = thisResource.getMetaData();
            if (Objects.nonNull(metaData)) {
                properties.put(PROPERTY_RESOURCE_ARCHETYPE, metaData.getArchetype().toString());
            }

            // product data
            ProductData productData = thisResource.getProductData();
            if (Objects.nonNull(productData)) {
                String modelId = productData.getModelId();

                // standard properties
                properties.put(PROPERTY_RESOURCE_ID, resourceId);
                properties.put(Thing.PROPERTY_MODEL_ID, modelId);
                properties.put(Thing.PROPERTY_VENDOR, productData.getManufacturerName());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, productData.getSoftwareVersion());
                String hardwarePlatformType = productData.getHardwarePlatformType();
                if (Objects.nonNull(hardwarePlatformType)) {
                    properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwarePlatformType);
                }

                // hue specific properties
                properties.put(PROPERTY_PRODUCT_NAME, productData.getProductName());
                properties.put(PROPERTY_PRODUCT_ARCHETYPE, productData.getProductArchetype().toString());
                properties.put(PROPERTY_PRODUCT_CERTIFIED, productData.getCertified().toString());

                // Check device for needed work-arounds.
                if (LK_WISER_DIMMER_MODEL_ID.equals(modelId)) {
                    // Apply transition time as a workaround for LK Wiser Dimmer firmware bug.
                    // Additional details here: https://techblog.vindvejr.dk/?p=455
                    applyOffTransitionWorkaround = true;
                    logger.debug("{} -> enabling work-around for turning off LK Wiser Dimmer", resourceId);
                }
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
     * @throws InterruptedException
     */
    private void updateResource(ResourceReference reference)
            throws ApiException, AssetNotLoadedException, InterruptedException {
        if (!disposing) {
            logger.debug("{} -> updateResource() from resource {}", resourceId, reference);
            getBridgeHandler().getResources(reference).getResources().stream()
                    .forEach(resource -> onResource(resource));
        }
    }

    /**
     * Fetch the full list of normal resp. smart scenes from the bridge, and call
     * {@code updateSceneContributors(List<Resource> allScenes)}
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws InterruptedException
     */
    public boolean updateSceneContributors() throws ApiException, AssetNotLoadedException, InterruptedException {
        if (!disposing && !updateSceneContributorsDone) {
            List<Resource> allScenes = new ArrayList<>();
            for (ResourceType type : SUPPORTED_SCENE_TYPES) {
                allScenes.addAll(getBridgeHandler().getResources(new ResourceReference().setType(type)).getResources());
            }
            updateSceneContributors(allScenes);
        }
        return updateSceneContributorsDone;
    }

    /**
     * Process the incoming list of normal resp. smart scene resources to find those which contribute to this thing. And
     * if there are any, include a scene channel in the supported channel list, and populate its respective state
     * options.
     *
     * @param allScenes the full list of normal resp. smart scene resources.
     */
    public synchronized boolean updateSceneContributors(List<Resource> allScenes) {
        if (!disposing && !updateSceneContributorsDone) {
            sceneContributorsCache.clear();
            sceneResourceEntries.clear();

            ResourceReference thisReference = getResourceReference();
            Set<Resource> scenes = allScenes.stream().filter(s -> thisReference.equals(s.getGroup()))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Resource::getName))));

            if (!scenes.isEmpty()) {
                sceneContributorsCache.putAll(scenes.stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
                sceneResourceEntries.putAll(scenes.stream().collect(Collectors.toMap(s -> s.getName(), s -> s)));

                State state = Objects.requireNonNull(scenes.stream().filter(s -> s.getSceneActive().orElse(false))
                        .map(s -> s.getSceneState()).findAny().orElse(UnDefType.UNDEF));

                // create scene channel if it is missing
                if (getThing().getChannel(CHANNEL_2_SCENE) == null) {
                    updateThing(editThing()
                            .withChannel(ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_2_SCENE))
                                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_2_SCENE)).build())
                            .build());
                }

                updateState(CHANNEL_2_SCENE, state, true);

                stateDescriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_2_SCENE), scenes
                        .stream().map(s -> s.getName()).map(n -> new StateOption(n, n)).collect(Collectors.toList()));

                logger.debug("{} -> updateSceneContributors() found {} normal resp. smart scenes", resourceId,
                        scenes.size());
            }
            updateSceneContributorsDone = true;
        }
        return updateSceneContributorsDone;
    }

    /**
     * Execute a series of HTTP GET commands to fetch the resource data for all service resources that contribute to the
     * thing state.
     *
     * @throws ApiException if a communication error occurred.
     * @throws AssetNotLoadedException if one of the assets is not loaded.
     * @throws InterruptedException
     */
    private void updateServiceContributors() throws ApiException, AssetNotLoadedException, InterruptedException {
        if (!disposing) {
            logger.debug("{} -> updateServiceContributors() called for {} contributors", resourceId,
                    serviceContributorsCache.size());
            ResourceReference reference = new ResourceReference();
            for (var entry : serviceContributorsCache.entrySet()) {
                updateResource(reference.setId(entry.getKey()).setType(entry.getValue().getType()));
            }
        }
    }

    /**
     * Update the channel state, and if appropriate add the channel ID to the set of supportedChannelIds. Calls either
     * OH core updateState() or triggerChannel() methods depending on the channel kind.
     *
     * Note: the particular 'UnDefType.UNDEF' value of the state argument is used to specially indicate the undefined
     * state, but yet that its channel shall nevertheless continue to be present in the thing.
     *
     * @param channelID the id of the channel.
     * @param state the new state of the channel.
     * @param fullUpdate if true always update the channel, otherwise only update if state is not 'UNDEF'.
     */
    private void updateState(String channelID, State state, boolean fullUpdate) {
        boolean isDefined = state != UnDefType.NULL;
        Channel channel = thing.getChannel(channelID);

        if ((fullUpdate || isDefined) && Objects.nonNull(channel)) {
            logger.debug("{} -> updateState() '{}' update with '{}' (fullUpdate:{}, isDefined:{})", resourceId,
                    channelID, state, fullUpdate, isDefined);

            switch (channel.getKind()) {
                case STATE:
                    updateState(channelID, state);
                    break;

                case TRIGGER:
                    if (state instanceof DecimalType) {
                        triggerChannel(channelID, String.valueOf(((DecimalType) state).intValue()));
                    }
            }
        }
        if (fullUpdate && isDefined) {
            addSupportedChannel(channelID);
        }
    }

    /**
     * Check if a PROPERTY_LEGACY_THING_UID value was set by the discovery process, and if so, clone the legacy thing's
     * settings into this thing.
     */
    private void updateThingFromLegacy() {
        if (isInitialized()) {
            logger.warn("Cannot update thing '{}' from legacy thing since handler already initialized.",
                    thing.getUID());
            return;
        }
        Map<String, String> properties = thing.getProperties();
        String legacyThingUID = properties.get(PROPERTY_LEGACY_THING_UID);
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
                        .filter(uid -> REPLICATE_CHANNEL_ID_MAP.containsKey(uid.getId())
                                && itemChannelLinkRegistry.isLinked(uid))
                        .collect(Collectors.toList()));

                Map<String, String> newProperties = new HashMap<>(properties);
                newProperties.remove(PROPERTY_LEGACY_THING_UID);

                updateThing(editBuilder.withProperties(newProperties).build());
            }
        }
    }
}
