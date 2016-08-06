/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.handler;

import static org.openhab.binding.harmonyhub.HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
import org.openhab.binding.harmonyhub.config.HarmonyHubConfig;
import org.openhab.binding.harmonyhub.discovery.HarmonyHubDiscovery;
import org.openhab.binding.harmonyhub.discovery.HarmonyHubDiscoveryListener;
import org.openhab.binding.harmonyhub.discovery.HarmonyHubDiscoveryResult;
import org.openhab.binding.harmonyhub.internal.HarmonyHubHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyHubListener;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.HarmonyConfig;
import net.whistlingfish.harmony.protocol.LoginToken;

/**
 * The {@link HarmonyHubHandler} is responsible for handling commands for Harmony Hubs, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class HarmonyHubHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(HarmonyHubHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HARMONY_HUB_THING_TYPE);

    // one minute should be plenty short, but not overwhelm the hub with requests
    private static final long CONFIG_CACHE_TIME = 60 * 1000;

    // this can be overridden by a configuration option
    private static final int DISCO_TIME = 30;

    private static final int RETRY_TIME = 60;

    private List<HubStatusListener> listeners = new CopyOnWriteArrayList<HubStatusListener>();

    private HarmonyClient client;

    private HarmonyConfig cachedConfig;

    private Date cacheConfigExpireDate;

    private HarmonyHubHandlerFactory factory;

    private ScheduledFuture<?> retryJob;

    public HarmonyHubHandler(Bridge bridge, HarmonyHubHandlerFactory factory) {
        super(bridge);
        this.factory = factory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                client.startActivityByName(command.toString());
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
            logger.warn("Command {]: Not a acceptable type (String or Decimal), ignorning", command);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);

        if (retryJob != null && !retryJob.isDone()) {
            retryJob.cancel(false);
        }

        final HarmonyHubConfig config = getConfig().as(HarmonyHubConfig.class);
        int discoTime = config.discoveryTimeout > 0 ? config.discoveryTimeout : DISCO_TIME;

        final HarmonyHubDiscovery disco = new HarmonyHubDiscovery(discoTime);
        disco.addListener(new HarmonyHubDiscoveryListener() {
            @Override
            public void hubDiscoveryFinished() {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No Harmony Hub found with name" + config.name);
                retryJob = scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        initialize();
                    }
                }, RETRY_TIME, TimeUnit.SECONDS);
            }

            @Override
            public void hubDiscovered(HarmonyHubDiscoveryResult result) {
                logger.debug("Found hub with name {}", result.getFriendlyName());
                if (result.getFriendlyName().equalsIgnoreCase(config.name)) {
                    // remove our listener so HubDiscoveryFinished can not be called while we connect
                    disco.removeListener(this);
                    disco.stopDiscovery();
                    getThing().setProperty(HarmonyHubBindingConstants.HUB_PROPERTY_ACCOUNTID, result.getAccountId());
                    getThing().setProperty(HarmonyHubBindingConstants.HUB_PROPERTY_SESSIONID, result.getSessionID());
                    getThing().setProperty(HarmonyHubBindingConstants.HUB_PROPERTY_HOST, result.getHost());
                    getThing().setProperty(HarmonyHubBindingConstants.HUB_PROPERTY_ID, result.getId());
                    connectToHub();
                }

            }
        });
        disco.startDiscovery();
    }

    @Override
    public void dispose() {
        listeners.clear();

        if (retryJob != null && !retryJob.isDone()) {
            retryJob.cancel(true);
        }

        if (getClient() != null) {
            getClient().disconnect();
        }
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
     * Connects to a Harmony Hub using credentials obtained through network discovery
     * x
     */
    private void connectToHub() {

        String host = getThing().getProperties().get(HarmonyHubBindingConstants.HUB_PROPERTY_HOST);
        String accountId = getThing().getProperties().get(HarmonyHubBindingConstants.HUB_PROPERTY_ACCOUNTID);
        String sessionId = getThing().getProperties().get(HarmonyHubBindingConstants.HUB_PROPERTY_SESSIONID);

        if (host == null || accountId == null || sessionId == null) {
            logger.error("Can not connect to hub with host {}, accountId {} and sessionId {}", host, accountId,
                    sessionId);
            return;
        }

        client = HarmonyClient.getInstance();
        client.addListener(new HarmonyHubListener() {
            @Override
            public void removeFrom(HarmonyClient hc) {
                // we have been removed from listening
            }

            @Override
            public void addTo(HarmonyClient hc) {
                hc.addListener(new ActivityChangeListener() {
                    @Override
                    public void activityStarted(Activity activity) {
                        updateState(activity);
                    }
                });
            }
        });

        try {
            logger.debug("Connecting: host {} sessionId {} accountId {}", host, sessionId, accountId);
            client.connect(host, new LoginToken(accountId, sessionId));
            updateStatus(ThingStatus.ONLINE);
            buildChannel();
        } catch (Exception e) {
            logger.error("Could not connect to HarmonyHub at " + host, e);
            client = null;
            retryJob = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    initialize();
                }
            }, RETRY_TIME, TimeUnit.SECONDS);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private void updateState(Activity activity) {
        logger.debug("Updating current activity to {}", activity.getLabel());
        updateState(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                new StringType(activity.getLabel()));
    }

    private void buildChannel() {
        try {
            HarmonyConfig config = getCachedConfig();
            List<Activity> activities = config.getActivities();
            // sort our activities in order
            Collections.sort(activities, new Comparator<Activity>() {
                @Override
                public int compare(Activity a1, Activity a2) {
                    // if the order value is null we want it to be at the start of the list
                    int o1 = a1.getActivityOrder() == null ? -1 : a1.getActivityOrder().intValue();
                    int o2 = a2.getActivityOrder() == null ? -1 : a2.getActivityOrder().intValue();
                    return (o1 < o2) ? -1 : (o1 == o2) ? 0 : 1;
                }
            });

            // add our activities as channel state options
            List<StateOption> states = new LinkedList<StateOption>();
            for (Activity activity : activities) {
                states.add(new StateOption(String.valueOf(activity.getLabel()), activity.getLabel()));
            }

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                    HarmonyHubBindingConstants.BINDING_ID + ":" + HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY);

            ChannelType channelType = new ChannelType(channelTypeUID, true, "String", "Current Activity",
                    "Current Activity", null, null, new StateDescription(null, null, null, "%s", false, states),
                    new URI(HarmonyHubBindingConstants.BINDING_ID, HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY,
                            null));

            factory.addChannelType(channelType);

            BridgeBuilder thingBuilder = (BridgeBuilder) BridgeBuilder
                    .create(getThing().getThingTypeUID(), getThing().getUID())
                    .withConfiguration(getThing().getConfiguration()).withProperties(getThing().getProperties());

            Channel channel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                            "String")
                    .withType(channelTypeUID).build();

            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        } catch (Exception e) {
            logger.debug("Could not add current activity channel to hub", e);
        }
    }

    /**
     * Returns an instance of our connected HarmonyClient
     *
     * @return
     */
    public HarmonyClient getClient() {
        return client;
    }

    /**
     * Cache a call to get the harmony config so we don't overwhelm it with requests.
     *
     * @return
     */
    public synchronized HarmonyConfig getCachedConfig() {
        if (client == null) {
            return null;
        }

        Date now = new Date();
        if (cachedConfig == null || cacheConfigExpireDate == null || now.after(cacheConfigExpireDate)) {
            logger.debug("Refreshing conf cache");
            cachedConfig = client.getConfig();
            cacheConfigExpireDate = new Date(System.currentTimeMillis() + CONFIG_CACHE_TIME);
        }
        return cachedConfig;
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
