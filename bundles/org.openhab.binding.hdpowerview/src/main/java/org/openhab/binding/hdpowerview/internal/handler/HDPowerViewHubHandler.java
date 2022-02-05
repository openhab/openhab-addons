/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.responses.FirmwareVersions;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections.SceneCollection;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.builders.AutomationChannelBuilder;
import org.openhab.binding.hdpowerview.internal.builders.SceneChannelBuilder;
import org.openhab.binding.hdpowerview.internal.builders.SceneGroupChannelBuilder;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
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
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HDPowerViewHubHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 * @author Jacob Laursen - Added support for scene groups and automations
 */
@NonNullByDefault
public class HDPowerViewHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubHandler.class);
    private final HttpClient httpClient;
    private final HDPowerViewTranslationProvider translationProvider;
    private final ConcurrentHashMap<ThingUID, ShadeData> pendingShadeInitializations = new ConcurrentHashMap<>();

    private long refreshInterval;
    private long hardRefreshPositionInterval;
    private long hardRefreshBatteryLevelInterval;

    private @Nullable HDPowerViewWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> hardRefreshPositionFuture;
    private @Nullable ScheduledFuture<?> hardRefreshBatteryLevelFuture;

    private List<Scene> sceneCache = new CopyOnWriteArrayList<>();
    private List<SceneCollection> sceneCollectionCache = new CopyOnWriteArrayList<>();
    private List<ScheduledEvent> scheduledEventCache = new CopyOnWriteArrayList<>();
    private @Nullable FirmwareVersions firmwareVersions;
    private Boolean deprecatedChannelsCreated = false;

    private final ChannelTypeUID sceneChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    private final ChannelTypeUID sceneGroupChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_GROUP_ACTIVATE);

    private final ChannelTypeUID automationChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_AUTOMATION_ENABLED);

    public HDPowerViewHubHandler(Bridge bridge, HttpClient httpClient,
            HDPowerViewTranslationProvider translationProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.translationProvider = translationProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            requestRefreshShadePositions();
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            return;
        }

        try {
            HDPowerViewWebTargets webTargets = this.webTargets;
            if (webTargets == null) {
                throw new ProcessingException("Web targets not initialized");
            }
            int id = Integer.parseInt(channelUID.getIdWithoutGroup());
            if (sceneChannelTypeUID.equals(channel.getChannelTypeUID()) && OnOffType.ON == command) {
                webTargets.activateScene(id);
                // Reschedule soft poll for immediate shade position update.
                scheduleSoftPoll();
            } else if (sceneGroupChannelTypeUID.equals(channel.getChannelTypeUID()) && OnOffType.ON == command) {
                webTargets.activateSceneCollection(id);
                // Reschedule soft poll for immediate shade position update.
                scheduleSoftPoll();
            } else if (automationChannelTypeUID.equals(channel.getChannelTypeUID())) {
                webTargets.enableScheduledEvent(id, OnOffType.ON == command);
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (NumberFormatException | HubException e) {
            logger.debug("Unexpected error {}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hub");
        HDPowerViewHubConfiguration config = getConfigAs(HDPowerViewHubConfiguration.class);
        String host = config.host;

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-host-address");
            return;
        }

        pendingShadeInitializations.clear();
        webTargets = new HDPowerViewWebTargets(httpClient, host);
        refreshInterval = config.refresh;
        hardRefreshPositionInterval = config.hardRefresh;
        hardRefreshBatteryLevelInterval = config.hardRefreshBatteryLevel;
        initializeChannels();
        firmwareVersions = null;
        schedulePoll();
    }

    private void initializeChannels() {
        // Rebuild dynamic channels and synchronize with cache.
        updateThing(editThing().withChannels(new ArrayList<Channel>()).build());
        sceneCache.clear();
        sceneCollectionCache.clear();
        scheduledEventCache.clear();
        deprecatedChannelsCreated = false;
    }

    public @Nullable HDPowerViewWebTargets getWebTargets() {
        return webTargets;
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
        pendingShadeInitializations.clear();
    }

    @Override
    public void childHandlerInitialized(final ThingHandler childHandler, final Thing childThing) {
        logger.debug("Child handler initialized: {}", childThing.getUID());
        if (childHandler instanceof HDPowerViewShadeHandler) {
            ShadeData shadeData = pendingShadeInitializations.remove(childThing.getUID());
            if (shadeData != null) {
                if (shadeData.id > 0) {
                    updateShadeThing(shadeData.id, childThing, shadeData);
                } else {
                    updateUnknownShadeThing(childThing);
                }
            }
        }
        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child handler disposed: {}", childThing.getUID());
        if (childHandler instanceof HDPowerViewShadeHandler) {
            pendingShadeInitializations.remove(childThing.getUID());
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    private void schedulePoll() {
        scheduleSoftPoll();
        scheduleHardPoll();
    }

    private void scheduleSoftPoll() {
        ScheduledFuture<?> future = this.pollFuture;
        if (future != null) {
            future.cancel(false);
        }
        logger.debug("Scheduling poll every {} ms", refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.MILLISECONDS);
    }

    private void scheduleHardPoll() {
        ScheduledFuture<?> future = this.hardRefreshPositionFuture;
        if (future != null) {
            future.cancel(false);
        }
        if (hardRefreshPositionInterval > 0) {
            logger.debug("Scheduling hard position refresh every {} minutes", hardRefreshPositionInterval);
            this.hardRefreshPositionFuture = scheduler.scheduleWithFixedDelay(this::requestRefreshShadePositions, 1,
                    hardRefreshPositionInterval, TimeUnit.MINUTES);
        }

        future = this.hardRefreshBatteryLevelFuture;
        if (future != null) {
            future.cancel(false);
        }
        if (hardRefreshBatteryLevelInterval > 0) {
            logger.debug("Scheduling hard battery level refresh every {} hours", hardRefreshBatteryLevelInterval);
            this.hardRefreshBatteryLevelFuture = scheduler.scheduleWithFixedDelay(
                    this::requestRefreshShadeBatteryLevels, 1, hardRefreshBatteryLevelInterval, TimeUnit.HOURS);
        }
    }

    private synchronized void stopPoll() {
        ScheduledFuture<?> future = this.pollFuture;
        if (future != null) {
            future.cancel(true);
        }
        this.pollFuture = null;

        future = this.hardRefreshPositionFuture;
        if (future != null) {
            future.cancel(true);
        }
        this.hardRefreshPositionFuture = null;

        future = this.hardRefreshBatteryLevelFuture;
        if (future != null) {
            future.cancel(true);
        }
        this.hardRefreshBatteryLevelFuture = null;
    }

    private synchronized void poll() {
        try {
            updateFirmwareProperties();
        } catch (HubException e) {
            logger.warn("Failed to update firmware properties: {}", e.getMessage());
        }

        try {
            logger.debug("Polling for state");
            pollShades();

            List<Scene> scenes = updateSceneChannels();
            List<SceneCollection> sceneCollections = updateSceneGroupChannels();
            List<ScheduledEvent> scheduledEvents = updateAutomationChannels(scenes, sceneCollections);

            // Scheduled events should also have their current state updated if event has been
            // enabled or disabled through app or other integration.
            updateAutomationStates(scheduledEvents);
        } catch (HubInvalidResponseException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
            } else {
                logger.warn("Bridge returned a bad JSON response: {} -> {}", e.getMessage(), cause.getMessage());
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (HubException e) {
            logger.warn("Error connecting to bridge: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        }
    }

    private void updateFirmwareProperties()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        if (firmwareVersions != null) {
            return;
        }
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }
        FirmwareVersions firmwareVersions = webTargets.getFirmwareVersions();
        Firmware mainProcessor = firmwareVersions.mainProcessor;
        if (mainProcessor == null) {
            logger.warn("Main processor firmware version missing in response.");
            return;
        }
        logger.debug("Main processor firmware version received: {}, {}", mainProcessor.name, mainProcessor.toString());
        Map<String, String> properties = editProperties();
        String mainProcessorName = mainProcessor.name;
        if (mainProcessorName != null) {
            properties.put(HDPowerViewBindingConstants.PROPERTY_FIRMWARE_NAME, mainProcessorName);
        }
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, mainProcessor.toString());
        Firmware radio = firmwareVersions.radio;
        if (radio != null) {
            logger.debug("Radio firmware version received: {}", radio.toString());
            properties.put(HDPowerViewBindingConstants.PROPERTY_RADIO_FIRMWARE_VERSION, radio.toString());
        }
        updateProperties(properties);
    }

    private void pollShades() throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Shades shades = webTargets.getShades();
        List<ShadeData> shadesData = shades.shadeData;
        if (shadesData == null) {
            throw new HubInvalidResponseException("Missing 'shades.shadeData' element");
        }

        updateStatus(ThingStatus.ONLINE);
        logger.debug("Received data for {} shades", shadesData.size());

        Map<Integer, ShadeData> idShadeDataMap = getIdShadeDataMap(shadesData);
        Map<Thing, Integer> thingIdMap = getShadeThingIdMap();
        for (Entry<Thing, Integer> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            int shadeId = item.getValue();
            ShadeData shadeData = idShadeDataMap.get(shadeId);
            if (shadeData != null) {
                updateShadeThing(shadeId, thing, shadeData);
            } else {
                updateUnknownShadeThing(thing);
            }
        }
    }

    private void updateShadeThing(int shadeId, Thing thing, ShadeData shadeData) {
        HDPowerViewShadeHandler thingHandler = ((HDPowerViewShadeHandler) thing.getHandler());
        if (thingHandler == null) {
            logger.debug("Shade '{}' handler not initialized", shadeId);
            pendingShadeInitializations.put(thing.getUID(), shadeData);
            return;
        }
        ThingStatus thingStatus = thingHandler.getThing().getStatus();
        switch (thingStatus) {
            case UNKNOWN:
            case ONLINE:
            case OFFLINE:
                logger.debug("Updating shade '{}'", shadeId);
                thingHandler.onReceiveUpdate(shadeData);
                break;
            case UNINITIALIZED:
            case INITIALIZING:
                logger.debug("Shade '{}' handler not yet ready; status: {}", shadeId, thingStatus);
                pendingShadeInitializations.put(thing.getUID(), shadeData);
                break;
            case REMOVING:
            case REMOVED:
            default:
                logger.debug("Ignoring shade update for shade '{}' in status {}", shadeId, thingStatus);
                break;
        }
    }

    private void updateUnknownShadeThing(Thing thing) {
        String shadeId = thing.getUID().getId();
        logger.debug("Shade '{}' has no data in hub", shadeId);
        HDPowerViewShadeHandler thingHandler = ((HDPowerViewShadeHandler) thing.getHandler());
        if (thingHandler == null) {
            logger.debug("Shade '{}' handler not initialized", shadeId);
            pendingShadeInitializations.put(thing.getUID(), new ShadeData());
            return;
        }
        ThingStatus thingStatus = thingHandler.getThing().getStatus();
        switch (thingStatus) {
            case UNKNOWN:
            case ONLINE:
            case OFFLINE:
                thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "@text/offline.gone.shade-unknown-to-hub"));
                break;
            case UNINITIALIZED:
            case INITIALIZING:
                logger.debug("Shade '{}' handler not yet ready; status: {}", shadeId, thingStatus);
                pendingShadeInitializations.put(thing.getUID(), new ShadeData());
                break;
            case REMOVING:
            case REMOVED:
            default:
                logger.debug("Ignoring shade status update for shade '{}' in status {}", shadeId, thingStatus);
                break;
        }
    }

    private List<Scene> fetchScenes()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Scenes scenes = webTargets.getScenes();
        List<Scene> sceneData = scenes.sceneData;
        if (sceneData == null) {
            throw new HubInvalidResponseException("Missing 'scenes.sceneData' element");
        }
        logger.debug("Received data for {} scenes", sceneData.size());

        return sceneData;
    }

    private List<Scene> updateSceneChannels()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        List<Scene> scenes = fetchScenes();

        if (scenes.size() == sceneCache.size() && sceneCache.containsAll(scenes)) {
            // Duplicates are not allowed. Reordering is not supported.
            logger.debug("Preserving scene channels, no changes detected");
            return scenes;
        }

        logger.debug("Updating all scene channels, changes detected");
        sceneCache = new CopyOnWriteArrayList<Scene>(scenes);

        List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
        allChannels.removeIf(c -> HDPowerViewBindingConstants.CHANNEL_GROUP_SCENES.equals(c.getUID().getGroupId()));

        SceneChannelBuilder channelBuilder = SceneChannelBuilder
                .create(this.translationProvider,
                        new ChannelGroupUID(thing.getUID(), HDPowerViewBindingConstants.CHANNEL_GROUP_SCENES))
                .withScenes(scenes).withChannels(allChannels);

        updateThing(editThing().withChannels(channelBuilder.build()).build());

        createDeprecatedSceneChannels(scenes);

        return scenes;
    }

    /**
     * Create backwards compatible scene channels if any items configured before release 3.2
     * are still linked. Users should have a reasonable amount of time to migrate to the new
     * scene channels that are connected to a channel group.
     */
    private void createDeprecatedSceneChannels(List<Scene> scenes) {
        if (deprecatedChannelsCreated) {
            // Only do this once.
            return;
        }
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_SCENES);
        for (Scene scene : scenes) {
            String channelId = Integer.toString(scene.id);
            ChannelUID newChannelUid = new ChannelUID(channelGroupUid, channelId);
            ChannelUID deprecatedChannelUid = new ChannelUID(getThing().getUID(), channelId);
            String description = translationProvider.getText("dynamic-channel.scene-activate.deprecated.description",
                    scene.getName());
            Channel channel = ChannelBuilder.create(deprecatedChannelUid, CoreItemFactory.SWITCH)
                    .withType(sceneChannelTypeUID).withLabel(scene.getName()).withDescription(description).build();
            logger.debug("Creating deprecated channel '{}' ('{}') to probe for linked items", deprecatedChannelUid,
                    scene.getName());
            updateThing(editThing().withChannel(channel).build());
            if (this.isLinked(deprecatedChannelUid) && !this.isLinked(newChannelUid)) {
                logger.warn("Created deprecated channel '{}' ('{}'), please link items to '{}' instead",
                        deprecatedChannelUid, scene.getName(), newChannelUid);
            } else {
                if (this.isLinked(newChannelUid)) {
                    logger.debug("Removing deprecated channel '{}' ('{}') since new channel '{}' is linked",
                            deprecatedChannelUid, scene.getName(), newChannelUid);

                } else {
                    logger.debug("Removing deprecated channel '{}' ('{}') since it has no linked items",
                            deprecatedChannelUid, scene.getName());
                }
                updateThing(editThing().withoutChannel(deprecatedChannelUid).build());
            }
        }
        deprecatedChannelsCreated = true;
    }

    private List<SceneCollection> fetchSceneCollections()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        SceneCollections sceneCollections = webTargets.getSceneCollections();
        List<SceneCollection> sceneCollectionData = sceneCollections.sceneCollectionData;
        if (sceneCollectionData == null) {
            throw new HubInvalidResponseException("Missing 'sceneCollections.sceneCollectionData' element");
        }
        logger.debug("Received data for {} sceneCollections", sceneCollectionData.size());

        return sceneCollectionData;
    }

    private List<SceneCollection> updateSceneGroupChannels()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        List<SceneCollection> sceneCollections = fetchSceneCollections();

        if (sceneCollections.size() == sceneCollectionCache.size()
                && sceneCollectionCache.containsAll(sceneCollections)) {
            // Duplicates are not allowed. Reordering is not supported.
            logger.debug("Preserving scene group channels, no changes detected");
            return sceneCollections;
        }

        logger.debug("Updating all scene group channels, changes detected");
        sceneCollectionCache = new CopyOnWriteArrayList<SceneCollection>(sceneCollections);

        List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
        allChannels
                .removeIf(c -> HDPowerViewBindingConstants.CHANNEL_GROUP_SCENE_GROUPS.equals(c.getUID().getGroupId()));

        SceneGroupChannelBuilder channelBuilder = SceneGroupChannelBuilder
                .create(this.translationProvider,
                        new ChannelGroupUID(thing.getUID(), HDPowerViewBindingConstants.CHANNEL_GROUP_SCENE_GROUPS))
                .withSceneCollections(sceneCollections).withChannels(allChannels);

        updateThing(editThing().withChannels(channelBuilder.build()).build());

        return sceneCollections;
    }

    private List<ScheduledEvent> fetchScheduledEvents()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        ScheduledEvents scheduledEvents = webTargets.getScheduledEvents();
        List<ScheduledEvent> scheduledEventData = scheduledEvents.scheduledEventData;
        if (scheduledEventData == null) {
            throw new HubInvalidResponseException("Missing 'scheduledEvents.scheduledEventData' element");
        }
        logger.debug("Received data for {} scheduledEvents", scheduledEventData.size());

        return scheduledEventData;
    }

    private List<ScheduledEvent> updateAutomationChannels(List<Scene> scenes, List<SceneCollection> sceneCollections)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException {
        List<ScheduledEvent> scheduledEvents = fetchScheduledEvents();

        if (scheduledEvents.size() == scheduledEventCache.size() && scheduledEventCache.containsAll(scheduledEvents)) {
            // Duplicates are not allowed. Reordering is not supported.
            logger.debug("Preserving automation channels, no changes detected");
            return scheduledEvents;
        }

        logger.debug("Updating all automation channels, changes detected");
        scheduledEventCache = new CopyOnWriteArrayList<ScheduledEvent>(scheduledEvents);

        List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
        allChannels
                .removeIf(c -> HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS.equals(c.getUID().getGroupId()));
        AutomationChannelBuilder channelBuilder = AutomationChannelBuilder
                .create(this.translationProvider,
                        new ChannelGroupUID(thing.getUID(), HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS))
                .withScenes(scenes).withSceneCollections(sceneCollections).withScheduledEvents(scheduledEvents)
                .withChannels(allChannels);
        updateThing(editThing().withChannels(channelBuilder.build()).build());

        return scheduledEvents;
    }

    private void updateAutomationStates(List<ScheduledEvent> scheduledEvents) {
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS);
        for (ScheduledEvent scheduledEvent : scheduledEvents) {
            String scheduledEventId = Integer.toString(scheduledEvent.id);
            ChannelUID channelUid = new ChannelUID(channelGroupUid, scheduledEventId);
            updateState(channelUid, scheduledEvent.enabled ? OnOffType.ON : OnOffType.OFF);
        }
    }

    private Map<Thing, Integer> getShadeThingIdMap() {
        Map<Thing, Integer> ret = new HashMap<>();
        getThing().getThings().stream()
                .filter(thing -> HDPowerViewBindingConstants.THING_TYPE_SHADE.equals(thing.getThingTypeUID()))
                .forEach(thing -> {
                    int id = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
                    if (id > 0) {
                        ret.put(thing, id);
                    }
                });
        return ret;
    }

    private Map<Integer, ShadeData> getIdShadeDataMap(List<ShadeData> shadeData) {
        Map<Integer, ShadeData> ret = new HashMap<>();
        for (ShadeData shade : shadeData) {
            if (shade.id > 0) {
                ret.put(shade.id, shade);
            }
        }
        return ret;
    }

    private void requestRefreshShadePositions() {
        Map<Thing, Integer> thingIdMap = getShadeThingIdMap();
        for (Entry<Thing, Integer> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.GONE) {
                // Skip shades unknown to the Hub.
                logger.debug("Shade '{}' is unknown, skipping position refresh", item.getValue());
                continue;
            }
            ThingHandler handler = thing.getHandler();
            if (handler instanceof HDPowerViewShadeHandler) {
                ((HDPowerViewShadeHandler) handler).requestRefreshShadePosition();
            } else {
                int shadeId = item.getValue();
                logger.debug("Shade '{}' handler not initialized", shadeId);
            }
        }
    }

    private void requestRefreshShadeBatteryLevels() {
        Map<Thing, Integer> thingIdMap = getShadeThingIdMap();
        for (Entry<Thing, Integer> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.GONE) {
                // Skip shades unknown to the Hub.
                logger.debug("Shade '{}' is unknown, skipping battery level refresh", item.getValue());
                continue;
            }
            ThingHandler handler = thing.getHandler();
            if (handler instanceof HDPowerViewShadeHandler) {
                ((HDPowerViewShadeHandler) handler).requestRefreshShadeBatteryLevel();
            } else {
                int shadeId = item.getValue();
                logger.debug("Shade '{}' handler not initialized", shadeId);
            }
        }
    }
}
