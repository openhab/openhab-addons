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
package org.openhab.binding.hdpowerview.internal.builders;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.dto.Scene;
import org.openhab.binding.hdpowerview.internal.dto.SceneCollection;
import org.openhab.binding.hdpowerview.internal.dto.ScheduledEvent;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomationChannelBuilder} class creates automation channels
 * from structured scheduled event data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class AutomationChannelBuilder extends BaseChannelBuilder {

    private final Logger logger = LoggerFactory.getLogger(AutomationChannelBuilder.class);

    @Nullable
    private Map<Integer, Scene> scenes;
    @Nullable
    private Map<Integer, SceneCollection> sceneCollections;
    @Nullable
    private List<ScheduledEvent> scheduledEvents;

    private AutomationChannelBuilder(HDPowerViewTranslationProvider translationProvider,
            ChannelGroupUID channelGroupUid) {
        super(translationProvider, channelGroupUid, HDPowerViewBindingConstants.CHANNELTYPE_AUTOMATION_ENABLED);
    }

    /**
     * Creates an {@link AutomationChannelBuilder} for the given {@link HDPowerViewTranslationProvider} and
     * {@link ChannelGroupUID}.
     * 
     * @param translationProvider the {@link HDPowerViewTranslationProvider}
     * @param channelGroupUid parent {@link ChannelGroupUID} for created channels
     * @return channel builder
     */
    public static AutomationChannelBuilder create(HDPowerViewTranslationProvider translationProvider,
            ChannelGroupUID channelGroupUid) {
        return new AutomationChannelBuilder(translationProvider, channelGroupUid);
    }

    /**
     * Adds created channels to existing list.
     * 
     * @param channels list that channels will be added to
     * @return channel builder
     */
    public AutomationChannelBuilder withChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }

    /**
     * Sets the scenes.
     * 
     * @param scenes the scenes
     * @return channel builder
     */
    public AutomationChannelBuilder withScenes(List<Scene> scenes) {
        this.scenes = scenes.stream().collect(Collectors.toMap(scene -> scene.id, scene -> scene));
        return this;
    }

    /**
     * Sets the scene collections.
     * 
     * @param sceneCollections the scene collections
     * @return channel builder
     */
    public AutomationChannelBuilder withSceneCollections(List<SceneCollection> sceneCollections) {
        this.sceneCollections = sceneCollections.stream()
                .collect(Collectors.toMap(sceneCollection -> sceneCollection.id, sceneCollection -> sceneCollection));
        return this;
    }

    /**
     * Sets the scheduled events.
     * 
     * @param scheduledEvents the sceduled events
     * @return channel builder
     */
    public AutomationChannelBuilder withScheduledEvents(List<ScheduledEvent> scheduledEvents) {
        this.scheduledEvents = scheduledEvents;
        return this;
    }

    /**
     * Builds and returns the channels.
     *
     * @return the {@link Channel} list
     */
    public List<Channel> build() {
        List<ScheduledEvent> scheduledEvents = this.scheduledEvents;
        if (scheduledEvents == null || (scenes == null && sceneCollections == null)) {
            return getChannelList(0);
        }
        List<Channel> channels = getChannelList(scheduledEvents.size());
        scheduledEvents.stream().forEach(scheduledEvent -> {
            Channel channel = createChannel(scheduledEvent);
            if (channel != null) {
                channels.add(channel);
            }
        });

        return channels;
    }

    private @Nullable Channel createChannel(ScheduledEvent scheduledEvent) {
        String referencedName = getReferencedSceneOrSceneCollectionName(scheduledEvent);
        if (referencedName == null) {
            return null;
        }
        ChannelUID channelUid = new ChannelUID(channelGroupUid, Integer.toString(scheduledEvent.id));
        String label = getScheduledEventName(referencedName, scheduledEvent);
        String description = translationProvider.getText("dynamic-channel.automation-enabled.description",
                referencedName);
        return ChannelBuilder.create(channelUid, CoreItemFactory.SWITCH).withType(channelTypeUid).withLabel(label)
                .withDescription(description).build();
    }

    private @Nullable String getReferencedSceneOrSceneCollectionName(ScheduledEvent scheduledEvent) {
        if (scheduledEvent.sceneId > 0) {
            Map<Integer, Scene> scenes = this.scenes;
            if (scenes == null) {
                logger.warn("Scheduled event '{}' references scene '{}', but no scenes are loaded", scheduledEvent.id,
                        scheduledEvent.sceneId);
                return null;
            }
            Scene scene = scenes.get(scheduledEvent.sceneId);
            if (scene != null) {
                return scene.getName();
            }
            logger.warn("Scene '{}' was not found for scheduled event '{}'", scheduledEvent.sceneId, scheduledEvent.id);
            return null;
        } else if (scheduledEvent.sceneCollectionId > 0) {
            Map<Integer, SceneCollection> sceneCollections = this.sceneCollections;
            if (sceneCollections == null) {
                logger.warn(
                        "Scheduled event '{}' references scene collection '{}', but no scene collections are loaded",
                        scheduledEvent.id, scheduledEvent.sceneCollectionId);
                return null;
            }
            SceneCollection sceneCollection = sceneCollections.get(scheduledEvent.sceneCollectionId);
            if (sceneCollection != null) {
                return sceneCollection.getName();
            }
            logger.warn("Scene collection '{}' was not found for scheduled event '{}'",
                    scheduledEvent.sceneCollectionId, scheduledEvent.id);
            return null;
        } else {
            logger.warn("Scheduled event '{}'' not related to any scene or scene collection", scheduledEvent.id);
            return null;
        }
    }

    private String getScheduledEventName(String sceneName, ScheduledEvent scheduledEvent) {
        String timeString, daysString;

        switch (scheduledEvent.eventType) {
            case ScheduledEvent.SCHEDULED_EVENT_TYPE_TIME:
                timeString = LocalTime.of(scheduledEvent.hour, scheduledEvent.minute).toString();
                break;
            case ScheduledEvent.SCHEDULED_EVENT_TYPE_SUNRISE:
                if (scheduledEvent.minute == 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.at-sunrise");
                } else if (scheduledEvent.minute < 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.before-sunrise",
                            getFormattedTimeOffset(-scheduledEvent.minute));
                } else {
                    timeString = translationProvider.getText("dynamic-channel.automation.after-sunrise",
                            getFormattedTimeOffset(scheduledEvent.minute));
                }
                break;
            case ScheduledEvent.SCHEDULED_EVENT_TYPE_SUNSET:
                if (scheduledEvent.minute == 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.at-sunset");
                } else if (scheduledEvent.minute < 0) {
                    timeString = translationProvider.getText("dynamic-channel.automation.before-sunset",
                            getFormattedTimeOffset(-scheduledEvent.minute));
                } else {
                    timeString = translationProvider.getText("dynamic-channel.automation.after-sunset",
                            getFormattedTimeOffset(scheduledEvent.minute));
                }
                break;
            default:
                return sceneName;
        }

        EnumSet<DayOfWeek> days = scheduledEvent.getDays();
        if (EnumSet.allOf(DayOfWeek.class).equals(days)) {
            daysString = translationProvider.getText("dynamic-channel.automation.all-days");
        } else if (ScheduledEvent.WEEKDAYS.equals(days)) {
            daysString = translationProvider.getText("dynamic-channel.automation.weekdays");
        } else if (ScheduledEvent.WEEKENDS.equals(days)) {
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
}
