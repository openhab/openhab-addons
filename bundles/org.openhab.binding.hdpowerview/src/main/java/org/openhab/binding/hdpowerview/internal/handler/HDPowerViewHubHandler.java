/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
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
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections.SceneCollection;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

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

    private static final long INITIAL_SOFT_POLL_DELAY_MS = 5_000;

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubHandler.class);
    private final HttpClient httpClient;
    private final HDPowerViewTranslationProvider translationProvider;

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
                scheduleSoftPoll(0);
            } else if (sceneGroupChannelTypeUID.equals(channel.getChannelTypeUID()) && OnOffType.ON == command) {
                webTargets.activateSceneCollection(id);
                // Reschedule soft poll for immediate shade position update.
                scheduleSoftPoll(0);
            } else if (automationChannelTypeUID.equals(channel.getChannelTypeUID())) {
                webTargets.enableScheduledEvent(id, OnOffType.ON == command);
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (NumberFormatException | HubProcessingException e) {
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

        webTargets = new HDPowerViewWebTargets(httpClient, host);
        refreshInterval = config.refresh;
        hardRefreshPositionInterval = config.hardRefresh;
        hardRefreshBatteryLevelInterval = config.hardRefreshBatteryLevel;
        initializeChannels();
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
    }

    private void schedulePoll() {
        scheduleSoftPoll(INITIAL_SOFT_POLL_DELAY_MS);
        scheduleHardPoll();
    }

    private void scheduleSoftPoll(long initialDelay) {
        ScheduledFuture<?> future = this.pollFuture;
        if (future != null) {
            future.cancel(false);
        }
        logger.debug("Scheduling poll for {} ms out, then every {} ms", initialDelay, refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, initialDelay, refreshInterval,
                TimeUnit.MILLISECONDS);
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
            logger.debug("Polling for state");
            pollShades();

            List<Scene> scenes = updateSceneChannels();
            List<SceneCollection> sceneCollections = updateSceneCollectionChannels();
            List<ScheduledEvent> scheduledEvents = updateScheduledEventChannels(scenes, sceneCollections);

            // Scheduled events should also have their current state updated if event has been
            // enabled or disabled through app or other integration.
            updateScheduledEventStates(scheduledEvents);
        } catch (JsonParseException e) {
            logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
        } catch (HubProcessingException e) {
            logger.warn("Error connecting to bridge: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        }
    }

    private void pollShades() throws JsonParseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Shades shades = webTargets.getShades();
        if (shades == null) {
            throw new JsonParseException("Missing 'shades' element");
        }

        List<ShadeData> shadesData = shades.shadeData;
        if (shadesData == null) {
            throw new JsonParseException("Missing 'shades.shadeData' element");
        }

        updateStatus(ThingStatus.ONLINE);
        logger.debug("Received data for {} shades", shadesData.size());

        Map<String, ShadeData> idShadeDataMap = getIdShadeDataMap(shadesData);
        Map<Thing, String> thingIdMap = getThingIdMap();
        for (Entry<Thing, String> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            String shadeId = item.getValue();
            ShadeData shadeData = idShadeDataMap.get(shadeId);
            updateShadeThing(shadeId, thing, shadeData);
        }
    }

    private void updateShadeThing(String shadeId, Thing thing, @Nullable ShadeData shadeData) {
        HDPowerViewShadeHandler thingHandler = ((HDPowerViewShadeHandler) thing.getHandler());
        if (thingHandler == null) {
            logger.debug("Shade '{}' handler not initialized", shadeId);
            return;
        }
        if (shadeData == null) {
            logger.debug("Shade '{}' has no data in hub", shadeId);
        } else {
            logger.debug("Updating shade '{}'", shadeId);
        }
        thingHandler.onReceiveUpdate(shadeData);
    }

    private List<Scene> fetchScenes() throws JsonParseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Scenes scenes = webTargets.getScenes();
        if (scenes == null) {
            throw new JsonParseException("Missing 'scenes' element");
        }

        List<Scene> sceneData = scenes.sceneData;
        if (sceneData == null) {
            throw new JsonParseException("Missing 'scenes.sceneData' element");
        }
        logger.debug("Received data for {} scenes", sceneData.size());

        return sceneData;
    }

    private List<Scene> updateSceneChannels()
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
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
        scenes.stream().sorted().forEach(scene -> allChannels.add(createSceneChannel(scene)));
        updateThing(editThing().withChannels(allChannels).build());

        createDeprecatedSceneChannels(scenes);

        return scenes;
    }

    private Channel createSceneChannel(Scene scene) {
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_SCENES);
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(scene.id));
        String description = translationProvider.getText("dynamic-channel.scene-activate.description", scene.getName());
        Channel channel = ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(sceneChannelTypeUID)
                .withLabel(scene.getName()).withDescription(description).build();

        return channel;
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
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        SceneCollections sceneCollections = webTargets.getSceneCollections();
        if (sceneCollections == null) {
            throw new JsonParseException("Missing 'sceneCollections' element");
        }

        List<SceneCollection> sceneCollectionData = sceneCollections.sceneCollectionData;
        if (sceneCollectionData == null) {
            throw new JsonParseException("Missing 'sceneCollections.sceneCollectionData' element");
        }
        logger.debug("Received data for {} sceneCollections", sceneCollectionData.size());

        return sceneCollectionData;
    }

    private List<SceneCollection> updateSceneCollectionChannels()
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        List<SceneCollection> sceneCollections = fetchSceneCollections();

        if (sceneCollections.size() == sceneCollectionCache.size()
                && sceneCollectionCache.containsAll(sceneCollections)) {
            // Duplicates are not allowed. Reordering is not supported.
            logger.debug("Preserving scene collection channels, no changes detected");
            return sceneCollections;
        }

        logger.debug("Updating all scene collection channels, changes detected");
        sceneCollectionCache = new CopyOnWriteArrayList<SceneCollection>(sceneCollections);

        List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
        allChannels
                .removeIf(c -> HDPowerViewBindingConstants.CHANNEL_GROUP_SCENE_GROUPS.equals(c.getUID().getGroupId()));
        sceneCollections.stream().sorted()
                .forEach(sceneCollection -> allChannels.add(createSceneCollectionChannel(sceneCollection)));
        updateThing(editThing().withChannels(allChannels).build());

        return sceneCollections;
    }

    private Channel createSceneCollectionChannel(SceneCollection sceneCollection) {
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_SCENE_GROUPS);
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(sceneCollection.id));
        String description = translationProvider.getText("dynamic-channel.scene-group-activate.description",
                sceneCollection.getName());
        Channel channel = ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(sceneGroupChannelTypeUID)
                .withLabel(sceneCollection.getName()).withDescription(description).build();

        return channel;
    }

    private List<ScheduledEvent> fetchScheduledEvents()
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        ScheduledEvents scheduledEvents = webTargets.getScheduledEvents();
        if (scheduledEvents == null) {
            throw new JsonParseException("Missing 'scheduledEvents' element");
        }

        List<ScheduledEvent> scheduledEventData = scheduledEvents.scheduledEventData;
        if (scheduledEventData == null) {
            throw new JsonParseException("Missing 'scheduledEvents.scheduledEventData' element");
        }
        logger.debug("Received data for {} scheduledEvents", scheduledEventData.size());

        return scheduledEventData;
    }

    private List<ScheduledEvent> updateScheduledEventChannels(List<Scene> scenes,
            List<SceneCollection> sceneCollections)
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        List<ScheduledEvent> scheduledEvents = fetchScheduledEvents();

        if (scheduledEvents.size() == scheduledEventCache.size() && scheduledEventCache.containsAll(scheduledEvents)) {
            // Duplicates are not allowed. Reordering is not supported.
            logger.debug("Preserving scheduled event channels, no changes detected");
            return scheduledEvents;
        }

        logger.debug("Updating all scheduled event channels, changes detected");
        scheduledEventCache = new CopyOnWriteArrayList<ScheduledEvent>(scheduledEvents);

        List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
        allChannels
                .removeIf(c -> HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS.equals(c.getUID().getGroupId()));
        scheduledEvents.stream().forEach(scheduledEvent -> {
            Channel channel = createScheduledEventChannel(scheduledEvent, scenes, sceneCollections);
            if (channel != null) {
                allChannels.add(channel);
            }
        });
        updateThing(editThing().withChannels(allChannels).build());

        return scheduledEvents;
    }

    private @Nullable Channel createScheduledEventChannel(ScheduledEvent scheduledEvent, List<Scene> scenes,
            List<SceneCollection> sceneCollections) {
        String referencedName = getReferencedSceneOrSceneCollectionName(scheduledEvent, scenes, sceneCollections);
        if (referencedName == null) {
            return null;
        }
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS);
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(scheduledEvent.id));
        String label = getScheduledEventName(referencedName, scheduledEvent);
        String description = translationProvider.getText("dynamic-channel.automation-enabled.description",
                referencedName);
        Channel channel = ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(automationChannelTypeUID)
                .withLabel(label).withDescription(description).build();

        return channel;
    }

    private @Nullable String getReferencedSceneOrSceneCollectionName(ScheduledEvent scheduledEvent, List<Scene> scenes,
            List<SceneCollection> sceneCollections) {
        if (scheduledEvent.sceneId > 0) {
            for (Scene scene : scenes) {
                if (scene.id == scheduledEvent.sceneId) {
                    return scene.getName();
                }
            }
            logger.error("Scene '{}' was not found for scheduled event '{}'", scheduledEvent.sceneId,
                    scheduledEvent.id);
            return null;
        } else if (scheduledEvent.sceneCollectionId > 0) {
            for (SceneCollection sceneCollection : sceneCollections) {
                if (sceneCollection.id == scheduledEvent.sceneCollectionId) {
                    return sceneCollection.getName();
                }
            }
            logger.error("Scene collection '{}' was not found for scheduled event '{}'",
                    scheduledEvent.sceneCollectionId, scheduledEvent.id);
            return null;
        } else {
            logger.error("Scheduled event '{}'' not related to any scene or scene collection", scheduledEvent.id);
            return null;
        }
    }

    private String getScheduledEventName(String sceneName, ScheduledEvent scheduledEvent) {
        String timeString, daysString;

        switch (scheduledEvent.eventType) {
            case ScheduledEvents.SCHEDULED_EVENT_TYPE_TIME:
                timeString = LocalTime.of(scheduledEvent.hour, scheduledEvent.minute).toString();
                break;
            case ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE:
                if (scheduledEvent.minute == 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.at_sunrise");
                } else if (scheduledEvent.minute < 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.before_sunrise",
                            getFormattedTimeOffset(-scheduledEvent.minute));
                } else {
                    timeString = translationProvider.getText("dynamic-channel.automation.after_sunrise",
                            getFormattedTimeOffset(scheduledEvent.minute));
                }
                break;
            case ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNSET:
                if (scheduledEvent.minute == 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.at_sunset");
                } else if (scheduledEvent.minute < 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.before_sunset",
                            getFormattedTimeOffset(-scheduledEvent.minute));
                } else {
                    timeString = translationProvider.getText("dynamic-channel.automation.after_sunset",
                            getFormattedTimeOffset(scheduledEvent.minute));
                }
                break;
            default:
                return sceneName;
        }

        EnumSet<DayOfWeek> days = scheduledEvent.getDays();
        if (EnumSet.allOf(DayOfWeek.class).equals(days)) {
            daysString = translationProvider.getText("dynamic-channel.automation.all-days");
        } else if (ScheduledEvents.WEEKDAYS.equals(days)) {
            daysString = translationProvider.getText("dynamic-channel.automation.weekdays");
        } else if (ScheduledEvents.WEEKENDS.equals(days)) {
            daysString = translationProvider.getText("dynamic-channel.automation.weekends");
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            days.forEach(day -> joiner.add(day.getDisplayName(TextStyle.SHORT, translationProvider.getLocale())));
            daysString = joiner.toString();
        }

        return translationProvider.getText("dynamic-channel.automation-enabled.label", sceneName, timeString,
                daysString);
    }

    private String getFormattedTimeOffset(int minutes) {
        if (minutes >= 60) {
            int remainder = minutes % 60;
            if (remainder == 0) {
                return translationProvider.getText("dynamic-channel.automation.hour", minutes / 60);
            }
            return translationProvider.getText("dynamic-channel.automation.hour-minute", minutes / 60, remainder);
        }
        return translationProvider.getText("dynamic-channel.automation.minute", minutes);
    }

    private void updateScheduledEventStates(List<ScheduledEvent> scheduledEvents) {
        ChannelGroupUID channelGroupUid = new ChannelGroupUID(thing.getUID(),
                HDPowerViewBindingConstants.CHANNEL_GROUP_AUTOMATIONS);
        for (ScheduledEvent scheduledEvent : scheduledEvents) {
            String scheduledEventId = Integer.toString(scheduledEvent.id);
            ChannelUID channelUid = new ChannelUID(channelGroupUid, scheduledEventId);
            updateState(channelUid, scheduledEvent.enabled ? OnOffType.ON : OnOffType.OFF);
        }
    }

    private Map<Thing, String> getThingIdMap() {
        Map<Thing, String> ret = new HashMap<>();
        for (Thing thing : getThing().getThings()) {
            String id = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
            if (id != null && !id.isEmpty()) {
                ret.put(thing, id);
            }
        }
        return ret;
    }

    private Map<String, ShadeData> getIdShadeDataMap(List<ShadeData> shadeData) {
        Map<String, ShadeData> ret = new HashMap<>();
        for (ShadeData shade : shadeData) {
            if (shade.id != 0) {
                ret.put(Integer.toString(shade.id), shade);
            }
        }
        return ret;
    }

    private void requestRefreshShadePositions() {
        Map<Thing, String> thingIdMap = getThingIdMap();
        for (Entry<Thing, String> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            ThingHandler handler = thing.getHandler();
            if (handler instanceof HDPowerViewShadeHandler) {
                ((HDPowerViewShadeHandler) handler).requestRefreshShadePosition();
            } else {
                String shadeId = item.getValue();
                logger.debug("Shade '{}' handler not initialized", shadeId);
            }
        }
    }

    private void requestRefreshShadeBatteryLevels() {
        Map<Thing, String> thingIdMap = getThingIdMap();
        for (Entry<Thing, String> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            ThingHandler handler = thing.getHandler();
            if (handler instanceof HDPowerViewShadeHandler) {
                ((HDPowerViewShadeHandler) handler).requestRefreshShadeBatteryLevel();
            } else {
                String shadeId = item.getValue();
                logger.debug("Shade '{}' handler not initialized", shadeId);
            }
        }
    }
}
