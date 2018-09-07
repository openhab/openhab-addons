/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.handler;

import static org.openhab.binding.harmonyhub.HarmonyHubBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCacheAsync;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.harmonyhub.internal.HarmonyHubHandlerFactory;
import org.openhab.binding.harmonyhub.internal.config.HarmonyHubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.ActivityStatusListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyHubListener;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.HarmonyConfig;

/**
 * The {@link HarmonyHubHandler} is responsible for handling commands for Harmony Hubs, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Pawel Pieczul - added support for hub status changes
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class HarmonyHubHandler extends BaseBridgeHandler implements HarmonyHubListener {

    private final Logger logger = LoggerFactory.getLogger(HarmonyHubHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HARMONY_HUB_THING_TYPE);

    private static final Comparator<Activity> ACTIVITY_COMPERATOR = Comparator.comparing(Activity::getActivityOrder,
            Comparator.nullsFirst(Integer::compareTo));

    // one minute should be plenty short, but not overwhelm the hub with requests
    private static final long CONFIG_CACHE_TIME = TimeUnit.MINUTES.toMillis(1);
    private static final int RETRY_TIME = 60;
    private static final int HEARTBEAT_INTERVAL = 30;

    private List<HubStatusListener> listeners = new CopyOnWriteArrayList<>();

    private final ExpiringCacheAsync<@Nullable HarmonyConfig> configCache = new ExpiringCacheAsync<>(CONFIG_CACHE_TIME);
    private final HarmonyHubHandlerFactory factory;

    private @NonNullByDefault({}) ScheduledExecutorService buttonExecutor;
    private @NonNullByDefault({}) HarmonyHubConfig config;

    private @Nullable HarmonyClient client;
    private @Nullable ScheduledFuture<?> retryJob;
    private @Nullable ScheduledFuture<?> heartBeatJob;

    private int heartBeatInterval;

    public HarmonyHubHandler(Bridge bridge, HarmonyHubHandlerFactory factory) {
        super(bridge);
        this.factory = factory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command '{}' for {}", command, channelUID);

        HarmonyClient localClient = client;
        if (localClient == null) {
            logger.warn("Cannot send command '{}' on {} because HarmonyClient is null", command, channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            updateState(localClient.getCurrentActivity());
        } else if (command instanceof StringType) {
            try {
                try {
                    int actId = Integer.parseInt(command.toString());
                    localClient.startActivity(actId);
                } catch (NumberFormatException ignored) {
                    localClient.startActivityByName(command.toString());
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Activity '{}' is not known by the hub, ignoring it.", command);
            } catch (Exception e) {
                logger.error("Could not start activity", e);
            }
        } else if (command instanceof DecimalType) {
            try {
                localClient.startActivity(((DecimalType) command).intValue());
            } catch (Exception e) {
                logger.error("Could not start activity", e);
            }
        } else {
            logger.warn("Command {}: Not an acceptable type (String or Decimal), ignoring", command);
        }
    }

    @Override
    public void initialize() {
        buttonExecutor = Executors.newSingleThreadScheduledExecutor();
        config = getConfigAs(HarmonyHubConfig.class);
        cancelRetry();
        updateStatus(ThingStatus.UNKNOWN);
        retryJob = scheduler.schedule(this::connect, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        buttonExecutor.shutdownNow();
        listeners.clear();
        cancelRetry();
        disconnectFromHub();
        factory.removeChannelTypesForThing(getThing().getUID());
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
        HarmonyClient localClient = client;
        if (localClient != null) {
            updateState(channelUID, new StringType(localClient.getCurrentActivity().getLabel()));
        }
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void removeFrom(@Nullable HarmonyClient hc) {
        // we have been removed from listening
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void addTo(@Nullable HarmonyClient hc) {
        if (hc == null) {
            logger.warn("Cannot add listeners to HarmonyClient that is null");
            return;
        }
        hc.addListener(new ActivityChangeListener() {
            @Override
            public void activityStarted(@Nullable Activity activity) {
                updateState(activity);
            }
        });
        hc.addListener(new ActivityStatusListener() {
            @Override
            public void activityStatusChanged(@Nullable Activity activity, Activity.@Nullable Status status) {
                updateActivityStatus(activity, status);
            }
        });
    }

    /**
     * Starts the connection process
     */
    private synchronized void connect() {
        disconnectFromHub();

        heartBeatInterval = config.heartBeatInterval > 0 ? config.heartBeatInterval : HEARTBEAT_INTERVAL;

        String host = config.host;

        // earlier versions required a name and used network discovery to find the hub and retrieve the host,
        // this section is to not break that and also update older configurations to use the host configuration
        // option instead of name
        if (StringUtils.isBlank(host)) {
            host = getThing().getProperties().get(HUB_PROPERTY_HOST);
            if (StringUtils.isNotBlank(host)) {
                Configuration genericConfig = getConfig();
                genericConfig.put(HUB_PROPERTY_HOST, host);
                updateConfiguration(genericConfig);
            } else {
                logger.debug("host not configured");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host not configured");
                return;
            }
        }

        HarmonyClient localClient = HarmonyClient.getInstance();
        localClient.addListener(this);
        client = localClient;

        try {
            logger.debug("Connecting: host {}", host);
            localClient.connect(host);
            heartBeatJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    localClient.sendPing();
                } catch (Exception e) {
                    logger.warn("heartbeat failed", e);
                    setOfflineAndReconnect("Hearbeat failed");
                }
            }, heartBeatInterval, heartBeatInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
            getConfigFuture().thenAcceptAsync(harmonyConfig -> updateCurrentActivityChannel(harmonyConfig), scheduler)
                    .exceptionally(e -> {
                        setOfflineAndReconnect("Getting config failed: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            logger.debug("Could not connect to HarmonyHub at {}", host, e);
            setOfflineAndReconnect("Could not connect: " + e.getMessage());
        }
    }

    private void disconnectFromHub() {
        ScheduledFuture<?> localHeartBeatJob = heartBeatJob;
        if (localHeartBeatJob != null && !localHeartBeatJob.isDone()) {
            localHeartBeatJob.cancel(false);
        }

        HarmonyClient localClient = client;
        if (localClient != null) {
            localClient.removeListener(this);
            localClient.disconnect();
        }
    }

    private void setOfflineAndReconnect(String error) {
        disconnectFromHub();
        retryJob = scheduler.schedule(this::connect, RETRY_TIME, TimeUnit.SECONDS);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }

    private void cancelRetry() {
        ScheduledFuture<?> localRetryJob = retryJob;
        if (localRetryJob != null && !localRetryJob.isDone()) {
            localRetryJob.cancel(false);
        }
    }

    private void updateState(@Nullable Activity activity) {
        if (activity != null) {
            logger.debug("Updating current activity to {}", activity.getLabel());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT_ACTIVITY),
                    new StringType(activity.getLabel()));
        }
    }

    private void updateActivityStatus(@Nullable Activity activity, Activity.@Nullable Status status) {
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
                .withStateDescription(new StateDescription(null, null, null, "%s", false, states)).build();

        factory.addChannelType(channelType);

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
        if (!buttonExecutor.isShutdown()) {
            buttonExecutor.execute(() -> {
                if (client != null) {
                    client.pressButton(device, button);
                }
            });
        }
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(String device, String button) {
        if (!buttonExecutor.isShutdown()) {
            buttonExecutor.execute(() -> {
                if (client != null) {
                    client.pressButton(device, button);
                }
            });
        }
    }

    public CompletableFuture<@Nullable HarmonyConfig> getConfigFuture() {
        Supplier<@Nullable HarmonyConfig> configSupplier = () -> {
            HarmonyClient localClient = client;
            if (localClient == null) {
                throw new IllegalStateException("Client is null");
            }
            try {
                logger.debug("Getting config from client");
                return localClient.getConfig();
            } catch (Exception e) {
                logger.debug("Could not get config from client", e);
                throw e;
            }
        };

        return configCache.getValue(() -> {
            return CompletableFuture.supplyAsync(configSupplier, scheduler);
        });
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
