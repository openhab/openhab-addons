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
package org.openhab.binding.sleepiq.internal.handler;

import static org.openhab.binding.sleepiq.internal.SleepIQBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.sleepiq.internal.config.SleepIQCloudConfiguration.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.sleepiq.api.Configuration;
import org.openhab.binding.sleepiq.api.LoginException;
import org.openhab.binding.sleepiq.api.SleepIQ;
import org.openhab.binding.sleepiq.api.UnauthorizedException;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.BedStatus;
import org.openhab.binding.sleepiq.api.model.FamilyStatus;
import org.openhab.binding.sleepiq.internal.SleepIQBindingConstants;
import org.openhab.binding.sleepiq.internal.SleepIQConfigStatusMessage;
import org.openhab.binding.sleepiq.internal.config.SleepIQCloudConfiguration;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SleepIQCloudHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepIQCloudHandler extends ConfigStatusBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_CLOUD);

    private final Logger logger = LoggerFactory.getLogger(SleepIQCloudHandler.class);

    private final List<BedStatusListener> bedStatusListeners = new CopyOnWriteArrayList<>();

    private ExpiringCache<FamilyStatus> statusCache;

    private ScheduledFuture<?> pollingJob;

    private SleepIQ cloud;

    private ClientBuilder clientBuilder;

    public SleepIQCloudHandler(final Bridge bridge, ClientBuilder clientBuilder) {
        super(bridge);
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void initialize() {
        try {
            logger.debug("Configuring bed status cache");
            statusCache = new ExpiringCache<>(TimeUnit.SECONDS.toMillis(getPollingInterval() / 2),
                    () -> cloud.getFamilyStatus());

            createCloudConnection();

            logger.debug("Setting SleepIQ cloud online");
            updateListenerManagement();
            updateStatus(ThingStatus.ONLINE);
        } catch (UnauthorizedException e) {
            logger.debug("SleepIQ cloud authentication failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid SleepIQ credentials");
        } catch (LoginException e) {
            logger.debug("SleepIQ cloud login failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "SleepIQ cloud login failed: " + e.getMessage());
        } catch (Exception e) {
            logger.debug("Unexpected error while communicating with SleepIQ cloud", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to connect to SleepIQ cloud: " + e.getMessage());
        }
    }

    /**
     * Create a new SleepIQ cloud service connection. If a connection already exists, it will be lost.
     *
     * @param clientBuilder2
     *
     * @throws LoginException if there is an error while authenticating to the service
     */
    private void createCloudConnection() throws LoginException {
        logger.debug("Reading SleepIQ cloud binding configuration");
        SleepIQCloudConfiguration bindingConfig = getConfigAs(SleepIQCloudConfiguration.class);

        logger.debug("Creating SleepIQ client");
        Configuration cloudConfig = new Configuration().withUsername(bindingConfig.username)
                .withPassword(bindingConfig.password).withLogging(logger.isDebugEnabled());
        cloud = SleepIQ.create(cloudConfig, clientBuilder);

        logger.debug("Authenticating at the SleepIQ cloud service");
        cloud.login();

        logger.info("Successfully authenticated at the SleepIQ cloud service");
    }

    @Override
    public synchronized void dispose() {
        logger.debug("Disposing SleepIQ cloud handler");

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Start or stop a background polling job to look for bed status updates based on whether or not there are any
     * listeners to notify.
     */
    private synchronized void updateListenerManagement() {
        if (!bedStatusListeners.isEmpty() && (pollingJob == null || pollingJob.isCancelled())) {
            int pollingInterval = getPollingInterval();
            pollingJob = scheduler.scheduleWithFixedDelay(this::refreshBedStatus, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
        } else if (bedStatusListeners.isEmpty() && pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Retrieve the polling interval for updating bed status.
     *
     * @return the polling interval in seconds
     */
    private int getPollingInterval() {
        return getConfigAs(SleepIQCloudConfiguration.class).pollingInterval;
    }

    /**
     * Retrieve the latest status on all beds and update all registered listeners.
     */
    public void refreshBedStatus() {
        try {
            FamilyStatus status = statusCache.getValue();
            updateStatus(ThingStatus.ONLINE);

            for (BedStatus bedStatus : status.getBeds()) {
                bedStatusListeners.stream().forEach(l -> l.onBedStateChanged(cloud, bedStatus));
            }
        } catch (Exception e) {
            logger.debug("Unexpected error while communicating with SleepIQ cloud", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to connect to SleepIQ cloud: " + e.getMessage());
        }
    }

    /**
     * Register the given listener to receive bed status updates.
     *
     * @param listener the listener to register
     */
    public void registerBedStatusListener(final BedStatusListener listener) {
        if (listener == null) {
            return;
        }

        bedStatusListeners.add(listener);
        refreshBedStatus();
        updateListenerManagement();
    }

    /**
     * Unregister the given listener from further bed status updates.
     *
     * @param listener the listener to unregister
     * @return <code>true</code> if listener was previously registered and is now unregistered; <code>false</code>
     *         otherwise
     */
    public boolean unregisterBedStatusListener(final BedStatusListener listener) {
        boolean result = bedStatusListeners.remove(listener);
        if (result) {
            updateListenerManagement();
        }

        return result;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // cloud handler has no channels
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();

        SleepIQCloudConfiguration config = getConfigAs(SleepIQCloudConfiguration.class);
        String username = config.username;
        String password = config.password;

        if (username.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(USERNAME)
                    .withMessageKeySuffix(SleepIQConfigStatusMessage.USERNAME_MISSING).withArguments(USERNAME).build());
        }

        if (password.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(PASSWORD)
                    .withMessageKeySuffix(SleepIQConfigStatusMessage.PASSWORD_MISSING).withArguments(PASSWORD).build());
        }

        return configStatusMessages;
    }

    /**
     * Get a list of all beds registered to the cloud service account.
     *
     * @return the list of beds (never <code>null</code>)
     */
    public List<Bed> getBeds() {
        return cloud.getBeds();
    }

    /**
     * Get the {@link Bed} corresponding to the given identifier.
     *
     * @param bedId the bed identifier
     * @return the identified {@link Bed} or <code>null</code> if no such bed exists
     */
    public Bed getBed(final String bedId) {
        for (Bed bed : getBeds()) {
            if (bedId.equals(bed.getBedId())) {
                return bed;
            }
        }

        return null;
    }

    /**
     * Update the given properties with attributes of the given bed. If no properties are given, a new map will be
     * created.
     *
     * @param bed the source of data
     * @param properties the properties to update (this may be <code>null</code>)
     * @return the given map (or a new map if no map was given) with updated/set properties from the supplied bed
     */
    public Map<String, String> updateProperties(final Bed bed, Map<String, String> properties) {
        if (bed != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, bed.getModel());
            properties.put(SleepIQBindingConstants.PROPERTY_BASE, bed.getBase());
            if (bed.isKidsBed() != null) {
                properties.put(SleepIQBindingConstants.PROPERTY_KIDS_BED, bed.isKidsBed().toString());
            }
            properties.put(SleepIQBindingConstants.PROPERTY_MAC_ADDRESS, bed.getMacAddress());
            properties.put(SleepIQBindingConstants.PROPERTY_NAME, bed.getName());
            if (bed.getPurchaseDate() != null) {
                properties.put(SleepIQBindingConstants.PROPERTY_PURCHASE_DATE, bed.getPurchaseDate().toString());
            }
            properties.put(SleepIQBindingConstants.PROPERTY_SIZE, bed.getSize());
            properties.put(SleepIQBindingConstants.PROPERTY_SKU, bed.getSku());
        }

        return properties;
    }
}
