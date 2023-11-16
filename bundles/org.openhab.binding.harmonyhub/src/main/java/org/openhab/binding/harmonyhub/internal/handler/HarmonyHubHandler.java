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
package org.openhab.binding.harmonyhub.internal.handler;

import static org.openhab.binding.harmonyhub.internal.HarmonyHubBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.harmonyhub.internal.HarmonyHubDynamicTypeProvider;
import org.openhab.binding.harmonyhub.internal.config.HarmonyHubConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.harmony.HarmonyClient;
import com.digitaldan.harmony.HarmonyClientListener;
import com.digitaldan.harmony.config.Activity;
import com.digitaldan.harmony.config.Activity.Status;
import com.digitaldan.harmony.config.HarmonyConfig;
import com.digitaldan.harmony.config.Ping;

/**
 * The {@link HarmonyHubHandler} is responsible for handling commands for Harmony Hubs, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Pawel Pieczul - added support for hub status changes
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class HarmonyHubHandler extends BaseBridgeHandler implements HarmonyClientListener {

    private final Logger logger = LoggerFactory.getLogger(HarmonyHubHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(HARMONY_HUB_THING_TYPE);

    private static final Comparator<Activity> ACTIVITY_COMPERATOR = Comparator.comparing(Activity::getActivityOrder,
            Comparator.nullsFirst(Integer::compareTo));

    private static final int RETRY_TIME = 60;
    private static final int HEARTBEAT_INTERVAL = 30;
    // Websocket will timeout after 60 seconds, pick a sensible max under this,
    private static final int HEARTBEAT_INTERVAL_MAX = 50;
    private Set<HubStatusListener> listeners = ConcurrentHashMap.newKeySet();
    private final HarmonyHubDynamicTypeProvider typeProvider;
    private @NonNullByDefault({}) HarmonyHubConfig config;
    private final HarmonyClient client;
    private @Nullable ScheduledFuture<?> retryJob;
    private @Nullable ScheduledFuture<?> heartBeatJob;
    private boolean propertiesUpdated;

    private int heartBeatInterval;

    public HarmonyHubHandler(Bridge bridge, HarmonyHubDynamicTypeProvider typeProvider, HttpClient httpClient) {
        super(bridge);
        this.typeProvider = typeProvider;
        client = new HarmonyClient(httpClient);
        client.addListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command '{}' for {}", command, channelUID);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Hub is offline, ignoring command {} for channel {}", command, channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            client.getCurrentActivity().thenAccept(activity -> {
                updateState(activity);
            });
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("No such channel for UID {}", channelUID);
            return;
        }

        switch (channel.getUID().getId()) {
            case CHANNEL_CURRENT_ACTIVITY:
                if (command instanceof DecimalType decimalCommand) {
                    try {
                        client.startActivity(decimalCommand.intValue());
                    } catch (Exception e) {
                        logger.warn("Could not start activity", e);
                    }
                } else {
                    try {
                        try {
                            int actId = Integer.parseInt(command.toString());
                            client.startActivity(actId);
                        } catch (NumberFormatException ignored) {
                            client.startActivityByName(command.toString());
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("Activity '{}' is not known by the hub, ignoring it.", command);
                    } catch (Exception e) {
                        logger.warn("Could not start activity", e);
                    }
                }
                break;
            case CHANNEL_BUTTON_PRESS:
                client.pressButtonCurrentActivity(command.toString());
                break;
            case CHANNEL_PLAYER:
                String cmd = null;
                if (command instanceof PlayPauseType) {
                    if (command == PlayPauseType.PLAY) {
                        cmd = "Play";
                    } else if (command == PlayPauseType.PAUSE) {
                        cmd = "Pause";
                    }
                } else if (command instanceof NextPreviousType) {
                    if (command == NextPreviousType.NEXT) {
                        cmd = "SkipForward";
                    } else if (command == NextPreviousType.PREVIOUS) {
                        cmd = "SkipBackward";
                    }
                } else if (command instanceof RewindFastforwardType) {
                    if (command == RewindFastforwardType.FASTFORWARD) {
                        cmd = "FastForward";
                    } else if (command == RewindFastforwardType.REWIND) {
                        cmd = "Rewind";
                    }
                }
                if (cmd != null) {
                    client.pressButtonCurrentActivity(cmd);
                } else {
                    logger.warn("Unknown player type {}", command);
                }
                break;
            default:
                logger.warn("Unknown channel id {}", channel.getUID().getId());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(HarmonyHubConfig.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduleRetry(0);
    }

    @Override
    public void dispose() {
        listeners.clear();
        cancelRetry();
        disconnectFromHub();
    }

    @Override
    public void handleRemoval() {
        typeProvider.removeChannelTypesForThing(getThing().getUID());
        super.handleRemoval();
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String comment) {
        super.updateStatus(status, detail, comment);
        logger.debug("Updating listeners with status {}", status);
        for (HubStatusListener listener : listeners) {
            listener.hubStatusChanged(status);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        client.getCurrentActivity().thenAccept((activity) -> {
            updateState(channelUID, new StringType(activity.getLabel()));
        });
    }

    @Override
    public void hubDisconnected(@Nullable String reason) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            setOfflineAndReconnect(String.format("Could not connect: %s", reason));
        }
    }

    @Override
    public void hubConnected() {
        heartBeatJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                Ping ping = client.sendPing().get();
                if (!propertiesUpdated) {
                    Map<String, String> properties = editProperties();
                    properties.put(HUB_PROPERTY_ID, ping.getUuid());
                    updateProperties(properties);
                    propertiesUpdated = true;
                }
            } catch (Exception e) {
                logger.debug("heartbeat failed", e);
                setOfflineAndReconnect("Hearbeat failed");
            }
        }, 5, heartBeatInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        getConfigFuture().thenAcceptAsync(harmonyConfig -> updateCurrentActivityChannel(harmonyConfig), scheduler)
                .exceptionally(e -> {
                    setOfflineAndReconnect("Getting config failed: " + e.getMessage());
                    return null;
                });
        client.getCurrentActivity().thenAccept(activity -> {
            updateState(activity);
        });
    }

    @Override
    public void activityStatusChanged(@Nullable Activity activity, @Nullable Status status) {
        updateActivityStatus(activity, status);
    }

    @Override
    public void activityStarted(@Nullable Activity activity) {
        updateState(activity);
    }

    /**
     * Starts the connection process
     */
    private synchronized void connect() {
        disconnectFromHub();

        heartBeatInterval = Math.min(config.heartBeatInterval > 0 ? config.heartBeatInterval : HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL_MAX);

        String host = config.host;

        // earlier versions required a name and used network discovery to find the hub and retrieve the host,
        // this section is to not break that and also update older configurations to use the host configuration
        // option instead of name
        if (host == null || host.isBlank()) {
            host = getThing().getProperties().get(HUB_PROPERTY_HOST);
            if (host != null && !host.isBlank()) {
                Configuration genericConfig = getConfig();
                genericConfig.put(HUB_PROPERTY_HOST, host);
                updateConfiguration(genericConfig);
            } else {
                logger.debug("host not configured");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host not configured");
                return;
            }
        }

        try {
            logger.debug("Connecting: host {}", host);
            client.connect(host);
        } catch (Exception e) {
            logger.debug("Could not connect to HarmonyHub at {}", host, e);
            setOfflineAndReconnect("Could not connect: " + e.getMessage());
        }
    }

    private void disconnectFromHub() {
        ScheduledFuture<?> heartBeatJob = this.heartBeatJob;
        if (heartBeatJob != null) {
            heartBeatJob.cancel(true);
            this.heartBeatJob = null;
        }
        client.disconnect();
    }

    private void setOfflineAndReconnect(String error) {
        disconnectFromHub();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
        scheduleRetry(RETRY_TIME);
    }

    private void cancelRetry() {
        ScheduledFuture<?> retryJob = this.retryJob;
        if (retryJob != null) {
            retryJob.cancel(true);
            this.retryJob = null;
        }
    }

    private synchronized void scheduleRetry(int delaySeconds) {
        cancelRetry();
        retryJob = scheduler.schedule(this::connect, delaySeconds, TimeUnit.SECONDS);
    }

    private void updateState(@Nullable Activity activity) {
        if (activity != null) {
            logger.debug("Updating current activity to {}", activity.getLabel());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT_ACTIVITY),
                    new StringType(activity.getLabel()));
        }
    }

    private void updateActivityStatus(@Nullable Activity activity, @Nullable Status status) {
        if (activity == null) {
            logger.debug("Cannot update activity status of {} with activity that is null", getThing().getUID());
            return;
        } else if (status == null) {
            logger.debug("Cannot update activity status of {} with status that is null", getThing().getUID());
            return;
        }

        logger.debug("Received {} activity status for {}", status, activity.getLabel());
        switch (status) {
            case ACTIVITY_IS_STARTING:
                triggerChannel(CHANNEL_ACTIVITY_STARTING_TRIGGER, getEventName(activity));
                break;
            case ACTIVITY_IS_STARTED:
            case HUB_IS_OFF:
                // hub is off is received with power-off activity
                triggerChannel(CHANNEL_ACTIVITY_STARTED_TRIGGER, getEventName(activity));
                break;
            case HUB_IS_TURNING_OFF:
                // hub is turning off is received for current activity, we will translate it into activity starting
                // trigger of power-off activity (with ID=-1)
                getConfigFuture().thenAccept(config -> {
                    if (config != null) {
                        Activity powerOff = config.getActivityById(-1);
                        if (powerOff != null) {
                            triggerChannel(CHANNEL_ACTIVITY_STARTING_TRIGGER, getEventName(powerOff));
                        }
                    }
                }).exceptionally(e -> {
                    setOfflineAndReconnect("Getting config failed: " + e.getMessage());
                    return null;
                });
                break;
            default:
                break;
        }
    }

    private String getEventName(Activity activity) {
        return activity.getLabel().replaceAll("[^A-Za-z0-9]", "_");
    }

    /**
     * Updates the current activity channel with the available activities as option states.
     */
    private void updateCurrentActivityChannel(@Nullable HarmonyConfig config) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(getThing().getUID() + ":" + CHANNEL_CURRENT_ACTIVITY);

        if (config == null) {
            logger.debug("Cannot update {} when HarmonyConfig is null", channelTypeUID);
            return;
        }

        logger.debug("Updating {}", channelTypeUID);

        List<Activity> activities = config.getActivities();
        // sort our activities in order
        Collections.sort(activities, ACTIVITY_COMPERATOR);

        // add our activities as channel state options
        List<StateOption> states = new LinkedList<>();
        for (Activity activity : activities) {
            states.add(new StateOption(activity.getLabel(), activity.getLabel()));
        }

        ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID, "Current Activity", "String")
                .withDescription("Current activity for " + getThing().getLabel())
                .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withPattern("%s")
                        .withReadOnly(false).withOptions(states).build())
                .build();

        typeProvider.putChannelType(channelType);

        Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT_ACTIVITY), "String")
                .withType(channelTypeUID).build();

        // replace existing currentActivity with updated one
        List<Channel> newChannels = new ArrayList<>();
        for (Channel c : getThing().getChannels()) {
            if (!c.getUID().equals(channel.getUID())) {
                newChannels.add(c);
            }
        }
        newChannels.add(channel);

        BridgeBuilder thingBuilder = editThing();
        thingBuilder.withChannels(newChannels);
        updateThing(thingBuilder.build());
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(int device, String button) {
        client.pressButton(device, button);
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(String device, String button) {
        client.pressButton(device, button);
    }

    public CompletableFuture<@Nullable HarmonyConfig> getConfigFuture() {
        return client.getConfig();
    }

    /**
     * Adds a HubConnectedListener
     *
     * @param listener
     */
    public void addHubStatusListener(HubStatusListener listener) {
        listeners.add(listener);
        listener.hubStatusChanged(getThing().getStatus());
    }

    /**
     * Removes a HubConnectedListener
     *
     * @param listener
     */
    public void removeHubStatusListener(HubStatusListener listener) {
        listeners.remove(listener);
    }
}
