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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.sleepiq.internal.SleepIQBindingConstants;
import org.openhab.binding.sleepiq.internal.SleepIQConfigStatusMessage;
import org.openhab.binding.sleepiq.internal.api.Configuration;
import org.openhab.binding.sleepiq.internal.api.LoginException;
import org.openhab.binding.sleepiq.internal.api.SleepIQ;
import org.openhab.binding.sleepiq.internal.api.SleepIQException;
import org.openhab.binding.sleepiq.internal.api.UnauthorizedException;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
import org.openhab.binding.sleepiq.internal.api.dto.FamilyStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.SleepDataResponse;
import org.openhab.binding.sleepiq.internal.api.dto.Sleeper;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.api.enums.SleepDataInterval;
import org.openhab.binding.sleepiq.internal.config.SleepIQCloudConfiguration;
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
@NonNullByDefault
public class SleepIQCloudHandler extends ConfigStatusBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_CLOUD);

    private static final int SLEEPER_POLLING_INTERVAL_HOURS = 12;

    private final Logger logger = LoggerFactory.getLogger(SleepIQCloudHandler.class);

    private final HttpClient httpClient;

    private final List<BedStatusListener> bedStatusListeners = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> statusPollingJob;
    private @Nullable ScheduledFuture<?> sleeperPollingJob;

    private @Nullable SleepIQ cloud;

    private @Nullable List<Sleeper> sleepers;

    public SleepIQCloudHandler(final Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        scheduler.execute(() -> {
            try {
                createCloudConnection();
                updateListenerManagement();
                updateStatus(ThingStatus.ONLINE);
            } catch (UnauthorizedException e) {
                logger.debug("CloudHandler: SleepIQ cloud authentication failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid SleepIQ credentials");
            } catch (LoginException e) {
                logger.debug("CloudHandler: SleepIQ cloud login failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "SleepIQ cloud login failed: " + e.getMessage());
            }
        });
    }

    @Override
    public synchronized void dispose() {
        stopSleeperPollingJob();
        stopStatusPollingJob();
        if (cloud != null) {
            cloud.shutdown();
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // cloud handler has no channels
    }

    /**
     * Validate the config from openHAB
     *
     * @return validity status of config parameters
     */
    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();
        SleepIQCloudConfiguration config = getConfigAs(SleepIQCloudConfiguration.class);
        String username = config.username;
        String password = config.password;
        if (username.isBlank()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(USERNAME)
                    .withMessageKeySuffix(SleepIQConfigStatusMessage.USERNAME_MISSING).withArguments(USERNAME).build());
        }
        if (password.isBlank()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(PASSWORD)
                    .withMessageKeySuffix(SleepIQConfigStatusMessage.PASSWORD_MISSING).withArguments(PASSWORD).build());
        }
        return configStatusMessages;
    }

    /**
     * Register the given listener to receive bed status updates.
     *
     * @param listener the listener to register
     */
    public void registerBedStatusListener(final BedStatusListener listener) {
        bedStatusListeners.add(listener);
        scheduler.execute(() -> {
            refreshSleepers();
            refreshBedStatus();
            updateListenerManagement();
        });
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

    /**
     * Get a list of all beds registered to the cloud service account.
     *
     * @return the list of beds or null if unable to get list
     */
    public @Nullable List<Bed> getBeds() {
        try {
            return cloud.getBeds();
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception getting list of beds", e);
            return null;
        }
    }

    /**
     * Get the bed corresponding to the given bed id
     *
     * @param bedId the bed identifier
     * @return the identified {@link Bed} or <code>null</code> if no such bed exists
     */
    public @Nullable Bed getBed(final @Nullable String bedId) {
        logger.debug("CloudHandler: Get bed object for bedId={}", bedId);
        if (bedId == null) {
            return null;
        }
        List<Bed> beds = getBeds();
        if (beds != null) {
            for (Bed bed : beds) {
                if (bedId.equals(bed.getBedId())) {
                    return bed;
                }
            }
        }
        return null;
    }

    /**
     * Get the sleeper associated with the bedId and side
     *
     * @param bedId the bed identifier
     * @param side the side of the bed
     * @return the sleeper or null if sleeper not found
     */
    public @Nullable Sleeper getSleeper(@Nullable String bedId, Side side) {
        logger.debug("CloudHandler: Get sleeper object for bedId={}, side={}", bedId, side);
        if (bedId == null) {
            return null;
        }
        List<Sleeper> localSleepers = sleepers;
        if (localSleepers != null) {
            for (Sleeper sleeper : localSleepers) {
                if (bedId.equals(sleeper.getBedId()) && side.equals(sleeper.getSide())) {
                    return sleeper;
                }
            }
        }
        return null;
    }

    /**
     * Set the sleep number of the specified chamber
     *
     * @param bedId the bed identifier
     * @param sleepNumber the sleep number multiple of 5 between 5 and 100
     * @param side the chamber to set
     */
    public void setSleepNumber(@Nullable String bedId, Side side, int sleepNumber) {
        if (bedId == null) {
            return;
        }
        try {
            cloud.setSleepNumber(bedId, side, sleepNumber);
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception setting sleep number of bed={}", bedId, e);
        }
    }

    /**
     * Set the pause mode of the specified bed
     *
     * @param bedId the bed identifier
     * @param mode turn pause mode on or off
     */
    public void setPauseMode(@Nullable String bedId, boolean mode) {
        if (bedId == null) {
            return;
        }
        try {
            cloud.setPauseMode(bedId, mode);
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception setting pause mode of bed={}", bedId, e);
        }
    }

    /**
     * Update the given properties with attributes of the given bed. If no properties are given, a new map will be
     * created.
     *
     * @param bed the source of data
     * @param properties the properties to update (this may be <code>null</code>)
     * @return the given map (or a new map if no map was given) with updated/set properties from the supplied bed
     */
    public Map<String, String> updateProperties(final @Nullable Bed bed, Map<String, String> properties) {
        if (bed != null) {
            logger.debug("CloudHandler: Updating bed properties for bed={}", bed.getBedId());
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

    /**
     * Retrieve the latest status on all beds and update all registered listeners
     * with bed status, sleepers and sleep data.
     */
    private void refreshBedStatus() {
        logger.debug("CloudHandler: Refreshing BED STATUS, updating chanels with status, sleepers, and sleep data");
        try {
            FamilyStatusResponse familyStatus = cloud.getFamilyStatus();
            if (familyStatus != null && familyStatus.getBeds() != null) {
                updateStatus(ThingStatus.ONLINE);
                for (BedStatus bedStatus : familyStatus.getBeds()) {
                    logger.debug("CloudHandler: Informing listeners with bed status for bedId={}",
                            bedStatus.getBedId());
                    bedStatusListeners.stream().forEach(l -> l.onBedStateChanged(bedStatus));
                }

                List<Sleeper> localSleepers = sleepers;
                if (localSleepers != null) {
                    for (Sleeper sleeper : localSleepers) {
                        logger.debug("CloudHandler: Informing listeners with sleepers for sleeperId={}",
                                sleeper.getSleeperId());
                        bedStatusListeners.stream().forEach(l -> l.onSleeperChanged(sleeper));
                    }
                }
                return;
            }
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception refreshing bed status", e);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to connect to SleepIQ cloud");
    }

    /**
     * Refresh the list of sleepers
     */
    private void refreshSleepers() {
        logger.debug("CloudHandler: Refreshing SLEEPERS");
        try {
            sleepers = cloud.getSleepers();
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception refreshing list of sleepers", e);
        }
    }

    public @Nullable SleepDataResponse getDailySleepData(String sleeperId) {
        return getSleepData(sleeperId, SleepDataInterval.DAY);
    }

    public @Nullable SleepDataResponse getMonthlySleepData(String sleeperId) {
        return getSleepData(sleeperId, SleepDataInterval.MONTH);
    }

    private @Nullable SleepDataResponse getSleepData(String sleeperId, SleepDataInterval interval) {
        try {
            return cloud.getSleepData(sleeperId, interval);
        } catch (SleepIQException e) {
            logger.debug("CloudHandler: Exception getting sleep data for sleeperId={}", sleeperId, e);
        }
        return null;
    }

    /**
     * Create a new SleepIQ cloud service connection. If a connection already exists, it will be lost.
     *
     * @throws LoginException if there is an error while authenticating to the service
     */
    private void createCloudConnection() throws LoginException {
        SleepIQCloudConfiguration bindingConfig = getConfigAs(SleepIQCloudConfiguration.class);
        Configuration cloudConfig = new Configuration().withUsername(bindingConfig.username)
                .withPassword(bindingConfig.password).withLogging(logger.isTraceEnabled());
        logger.debug("CloudHandler: Authenticating at the SleepIQ cloud service");
        cloud = SleepIQ.create(cloudConfig, httpClient);
        cloud.login();
    }

    /**
     * Start or stop the background polling jobs
     */
    private synchronized void updateListenerManagement() {
        startSleeperPollingJob();
        startStatusPollingJob();
    }

    /**
     * Start or stop the bed status polling job
     */
    private void startStatusPollingJob() {
        ScheduledFuture<?> localPollingJob = statusPollingJob;
        if (!bedStatusListeners.isEmpty() && (localPollingJob == null || localPollingJob.isCancelled())) {
            int pollingInterval = getStatusPollingIntervalSeconds();
            logger.debug("CloudHandler: Scheduling bed status polling job every {} seconds", pollingInterval);
            statusPollingJob = scheduler.scheduleWithFixedDelay(this::refreshBedStatus, pollingInterval,
                    pollingInterval, TimeUnit.SECONDS);
        } else if (bedStatusListeners.isEmpty()) {
            stopStatusPollingJob();
        }
    }

    /**
     * Stop the bed status polling job
     */
    private void stopStatusPollingJob() {
        ScheduledFuture<?> localPollingJob = statusPollingJob;
        if (localPollingJob != null) {
            logger.debug("CloudHandler: Canceling bed status polling job");
            localPollingJob.cancel(true);
            statusPollingJob = null;
        }
    }

    private int getStatusPollingIntervalSeconds() {
        return getConfigAs(SleepIQCloudConfiguration.class).pollingInterval;
    }

    /**
     * Start or stop the sleeper polling job
     */
    private void startSleeperPollingJob() {
        ScheduledFuture<?> localJob = sleeperPollingJob;
        if (!bedStatusListeners.isEmpty() && (localJob == null || localJob.isCancelled())) {
            logger.debug("CloudHandler: Scheduling sleeper polling job every {} hours", SLEEPER_POLLING_INTERVAL_HOURS);
            sleeperPollingJob = scheduler.scheduleWithFixedDelay(this::refreshSleepers, SLEEPER_POLLING_INTERVAL_HOURS,
                    SLEEPER_POLLING_INTERVAL_HOURS, TimeUnit.HOURS);
        } else if (bedStatusListeners.isEmpty()) {
            stopSleeperPollingJob();
        }
    }

    /**
     * Stop the sleeper polling job
     */
    private void stopSleeperPollingJob() {
        ScheduledFuture<?> localJob = sleeperPollingJob;
        if (localJob != null && !localJob.isCancelled()) {
            logger.debug("CloudHandler: Canceling sleeper polling job");
            localJob.cancel(true);
            sleeperPollingJob = null;
        }
    }
}
