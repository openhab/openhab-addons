/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.handler;

import static org.openhab.binding.harmonyhub.HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE;

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
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
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
 */
public class HarmonyHubHandler extends BaseBridgeHandler implements HarmonyHubListener {

    private Logger logger = LoggerFactory.getLogger(HarmonyHubHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HARMONY_HUB_THING_TYPE);

    private static final Comparator<Activity> ACTIVITY_COMPERATOR = Comparator.comparing(Activity::getActivityOrder,
            Comparator.nullsFirst(Integer::compareTo));

    // one minute should be plenty short, but not overwhelm the hub with requests
    private static final long CONFIG_CACHE_TIME = TimeUnit.MINUTES.toMillis(1);

    private static final int RETRY_TIME = 60;

    private static final int HEARTBEAT_INTERVAL = 30;

    private ScheduledExecutorService buttonExecutor;

    private List<HubStatusListener> listeners = new CopyOnWriteArrayList<HubStatusListener>();

    private HarmonyClient client;

    private ExpiringCacheAsync<HarmonyConfig> configCache = new ExpiringCacheAsync<>(CONFIG_CACHE_TIME);

    private HarmonyHubHandlerFactory factory;

    private ScheduledFuture<?> retryJob;

    private ScheduledFuture<?> heartBeatJob;

    private int heartBeatInterval;

    public HarmonyHubHandler(Bridge bridge, HarmonyHubHandlerFactory factory) {
        super(bridge);
        this.factory = factory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(client.getCurrentActivity());
        } else if (command instanceof StringType) {
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
                logger.error("Could not start activity", e);
            }
        } else if (command instanceof DecimalType) {
            try {
                client.startActivity(((DecimalType) command).intValue());
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
        cancelRetry();
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
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, String comment) {
        super.updateStatus(status, detail, comment);
        logger.debug("Updating listeners with status {}", status);
        for (HubStatusListener listener : listeners) {
            listener.hubStatusChanged(status);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (client != null) {
            updateState(channelUID, new StringType(client.getCurrentActivity().getLabel()));
        }
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void removeFrom(HarmonyClient hc) {
        // we have been removed from listening
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void addTo(HarmonyClient hc) {
        hc.addListener(new ActivityChangeListener() {
            @Override
            public void activityStarted(Activity activity) {
                updateState(activity);
            }
        });
        hc.addListener(new ActivityStatusListener() {
            @Override
            public void activityStatusChanged(Activity activity, Activity.Status status) {
                updateActivityStatus(activity, status);
            }
        });
    }

    /**
     * Starts the connection process
     */
    private synchronized void connect() {
        disconnectFromHub();

        HarmonyHubConfig config = getConfig().as(HarmonyHubConfig.class);
        heartBeatInterval = config.heartBeatInterval > 0 ? config.heartBeatInterval : HEARTBEAT_INTERVAL;

        String host = config.host;

        // earlier versions required a name and used network discovery to find the hub and retrieve the host,
        // this section is to not break that and also update older configurations to use the host configuration
        // option instead of name
        if (StringUtils.isBlank(host)) {
            host = getThing().getProperties().get(HarmonyHubBindingConstants.HUB_PROPERTY_HOST);
            if (StringUtils.isNotBlank(host)) {
                Configuration genericConfig = getConfig();
                genericConfig.put("host", host);
                updateConfiguration(genericConfig);
            } else {
                logger.debug("host not configured");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host not configured");
                return;
            }
        }

        client = HarmonyClient.getInstance();
        client.addListener(this);

        try {
            logger.debug("Connecting: host {}", host);
            client.connect(host);
            heartBeatJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    client.sendPing();
                } catch (Exception e) {
                    logger.warn("heartbeat failed", e);
                    setOfflineAndReconnect("Hearbeat failed");
                }
            }, heartBeatInterval, heartBeatInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
            getConfigFuture().thenAcceptAsync(harmonyConfig -> buildChannel(harmonyConfig), scheduler)
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
        if (heartBeatJob != null && !heartBeatJob.isDone()) {
            heartBeatJob.cancel(false);
        }

        if (client != null) {
            client.removeListener(this);
            client.disconnect();
        }
    }

    private void setOfflineAndReconnect(String error) {
        disconnectFromHub();
        retryJob = scheduler.schedule(this::connect, RETRY_TIME, TimeUnit.SECONDS);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }

    private void cancelRetry() {
        if (retryJob != null && !retryJob.isDone()) {
            retryJob.cancel(false);
        }
    }

    private void updateState(Activity activity) {
        logger.debug("Updating current activity to {}", activity.getLabel());
        updateState(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                new StringType(activity.getLabel()));
    }

    private void updateActivityStatus(Activity activity, Activity.Status status) {
        logger.debug("Received {} activity status for {}", status, activity.getLabel());
        switch (status) {
            case ACTIVITY_IS_STARTING:
                triggerChannel(HarmonyHubBindingConstants.CHANNEL_ACTIVITY_STARTING_TRIGGER, getEventName(activity));
                break;
            case ACTIVITY_IS_STARTED:
            case HUB_IS_OFF:
                // hub is off is received with power-off activity
                triggerChannel(HarmonyHubBindingConstants.CHANNEL_ACTIVITY_STARTED_TRIGGER, getEventName(activity));
                break;
            case HUB_IS_TURNING_OFF:
                // hub is turning off is received for current activity, we will translate it into activity starting
                // trigger of power-off activity (with ID=-1)
                getConfigFuture().thenAccept(config -> {
                    if (config != null) {
                        Activity powerOff = config.getActivityById(-1);
                        if (powerOff != null) {
                            triggerChannel(HarmonyHubBindingConstants.CHANNEL_ACTIVITY_STARTING_TRIGGER,
                                    getEventName(powerOff));
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

    private void buildChannel(HarmonyConfig config) {
        try {
            List<Activity> activities = config.getActivities();
            // sort our activities in order
            Collections.sort(activities, ACTIVITY_COMPERATOR);

            // add our activities as channel state options
            List<StateOption> states = new LinkedList<StateOption>();
            for (Activity activity : activities) {
                states.add(new StateOption(activity.getLabel(), activity.getLabel()));
            }

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                    getThing().getUID() + ":" + HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY);

            ChannelType channelType = new ChannelType(channelTypeUID, false, "String", "Current Activity",
                    "Current activity for " + getThing().getLabel(), null, null,
                    new StateDescription(null, null, null, "%s", false, states), null);

            factory.addChannelType(channelType);

            BridgeBuilder thingBuilder = editThing();

            Channel channel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                            "String")
                    .withType(channelTypeUID).build();

            // replace existing currentActivity with updated one
            List<Channel> currentChannels = getThing().getChannels();
            List<Channel> newChannels = new ArrayList<Channel>();
            for (Channel c : currentChannels) {
                if (!c.getUID().equals(channel.getUID())) {
                    newChannels.add(c);
                }
            }
            newChannels.add(channel);
            thingBuilder.withChannels(newChannels);

            updateThing(thingBuilder.build());
        } catch (Exception e) {
            logger.debug("Could not add current activity channel to hub", e);
        }
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(int device, String button) {
        if (buttonExecutor != null && !buttonExecutor.isShutdown()) {
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
        if (buttonExecutor != null && !buttonExecutor.isShutdown()) {
            buttonExecutor.execute(() -> {
                if (client != null) {
                    client.pressButton(device, button);
                }
            });
        }
    }

    public CompletableFuture<HarmonyConfig> getConfigFuture() {
        Supplier<HarmonyConfig> configSupplier = () -> {
            if (client == null) {
                throw new IllegalStateException("Client is null");
            }
            try {
                logger.debug("Getting config from client");
                return client.getConfig();
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
