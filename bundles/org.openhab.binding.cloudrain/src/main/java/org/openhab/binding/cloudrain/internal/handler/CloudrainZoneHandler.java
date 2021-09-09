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
package org.openhab.binding.cloudrain.internal.handler;

import static org.openhab.binding.cloudrain.internal.CloudrainBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.CloudrainConfig;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPI;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIException;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Zone;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CloudrainZoneHandler} is responsible for handling Zone Things representing irrigation zones in the
 * Cloudrain ecosysten. Zones comprise of one or more irrigation valves that are controlled according to a schedule or
 * individual commands. The Cloudrain Developer API V1 only offers access to user-defined zones and not to individual
 * valves. This handler is responsible for updating the zone's state (active irrigations) and executing individual
 * commands(starting, adjusting or stopping an irrigation). The API offers no access to modify schedules defined in the
 * Cloudrain App.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainZoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CloudrainZoneHandler.class);

    /**
     * A reference to the {@link ItemChannelLinkRegistry} for checking the link status
     */
    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    /**
     * A reference to a {@link TimeZoneProvider} for localized times
     */
    private TimeZoneProvider timeZoneProvider;

    /**
     * The update job which provides real time status updates for active irrigations
     */
    private @Nullable ScheduledFuture<?> remainingSecsUpdateJob;

    /**
     * A reference to the {@link CloudrainAPI}
     */
    private CloudrainAPI cloudrainAPI;

    /**
     * The id if the zone associated with this handler
     */
    private String zoneId = "";

    /**
     * a local attribute for convenient access to the defined command duration. Default value 10 seconds.
     */
    private transient int localCommandDuration = 10;

    /**
     * This class handles real time status update of active irrigations.
     * If scheduled each second this job will decrease the remaining seconds accordingly.
     * If configured the job will also update the zone's status at the projected end of the irrigation.
     */
    private class RealtimeUpdateJob implements Runnable {
        private int remaining;
        private Channel channel;
        private CloudrainConfig config;

        public RealtimeUpdateJob(Channel channel, int remaining, CloudrainConfig config) {
            this.channel = channel;
            this.remaining = remaining;
            this.config = config;
        }

        @Override
        public void run() {
            if (--remaining >= 0) {
                if (config.getRealtimeUpdates()) {
                    updateState(channel.getUID(), new DecimalType(remaining));
                }
            } else {
                // trigger thing update to reflect the end of the irrigation if desired
                if (config.getUpdateAfterIrrigation()) {
                    updateStatusFromAPI();
                }
            }
        }
    }

    /**
     * Creates this CloudrainZoneHandler
     *
     * @param thing the Cloudrain Zone thing
     * @param cloudrainAPI the initialized Cloudrain API. Handlers of this class expect that the
     *            {@link CloudrainAccountHanlder} has already authenticated.
     * @param timeZoneProvider the TimeZoneProvider for handling dates
     * @param itemChannelLinkRegistry required for checking this Zone's channel link status
     * @param timeZoneProvider a {@link TimeZoneProvider} for date time localization
     */
    public CloudrainZoneHandler(Thing thing, CloudrainAPI cloudrainAPI, TimeZoneProvider timeZoneProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);
        this.cloudrainAPI = cloudrainAPI;
        this.timeZoneProvider = timeZoneProvider;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void initialize() {
        // Retrieve this zone's ID from the properties as set by discovery
        String zoneIdFromProperty = getThing().getProperties().get(PROPERTY_ZONE_ID);
        if (zoneIdFromProperty != null) {
            zoneId = zoneIdFromProperty;
            // schedule initialization tasks
            scheduler.execute(() -> {
                CloudrainAccountHanlder handler = getAccountHanlder();
                if (handler != null) {
                    // Register the zone to receive property and status updates
                    handler.registerZone(zoneId, getThing());
                    // Call the Cloudrain API and update the thing's status now
                    boolean successful = updateStatusFromAPI();
                    if (successful) {
                        // Register the zone to receive irrigation updates only if channels are linked
                        // This is done to avoid unnecessary frequent API calls
                        if (isAnyIrrigationChannelLinked()) {
                            handler.registerForIrrigationUpdates(zoneId, getThing());
                        }
                        // Update the THING status
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    logger.warn(ERROR_MSG_ZONE_REGISTRATION, zoneId);
                    updateStatus(ThingStatus.OFFLINE);
                }
            });
        } else {
            logger.warn(ERROR_MSG_ZONE_ID_PROPERTY);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // unregister this zone from the account handler
        unregisterAll();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        unregisterAll();
    }

    private void unregisterAll() {
        // unregister this zone from the account handler
        CloudrainAccountHanlder handler = getAccountHanlder();
        if (handler != null) {
            handler.unregisterZone(zoneId);
            handler.unregisterFromIrrigationUpdates(zoneId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // handle irrigation commands
        if (CHANNEL_GROUP_ID_COMMAND.equals(channelUID.getGroupId())) {
            if (command instanceof OnOffType && ((OnOffType) command) == OnOffType.ON) {
                try {
                    if (CHANNEL_ID_START_CMD.equals(channelUID.getIdWithoutGroup())) {
                        // Start irrigation
                        this.cloudrainAPI.startIrrigation(zoneId, localCommandDuration);
                    } else if (CHANNEL_ID_CHANGE_CMD.equals(channelUID.getIdWithoutGroup())) {
                        // Adjust irrigation
                        this.cloudrainAPI.adjustIrrigation(zoneId, localCommandDuration);
                    } else if (CHANNEL_ID_STOP_CMD.equals(channelUID.getIdWithoutGroup())) {
                        // Stop irrigation
                        this.cloudrainAPI.stopIrrigation(zoneId);
                    }
                    // reset the command switch
                    updateState(channelUID, OnOffType.OFF);
                    // Allow some seconds for the command to be processed before updating the status
                    scheduler.schedule(() -> {
                        // Fetch the irrigation status from the API to reflect the change
                        updateStatusFromAPI();
                    }, 5, TimeUnit.SECONDS);
                } catch (CloudrainAPIException e) {
                    logger.warn(ERROR_MSG_IRRIGATION_COMMAND, zoneId, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE);
                }
            } else if (CHANNEL_ID_DURATION_CMD.equals(channelUID.getIdWithoutGroup())) {
                if (command instanceof DecimalType) {
                    localCommandDuration = ((DecimalType) command).intValue();
                    updateState(channelUID, (DecimalType) command);
                }
            }
        }
    }

    /**
     * Updates this zone's status by calling the Cloudrain API
     * This is an ad hoc update which will be done in parallel to the central polling for all zones. It may be useful to
     * refresh a zone's status individually at certain events, e.g. initialization or at the end of
     * an irrigation.
     */
    private boolean updateStatusFromAPI() {
        try {
            // retrieve the zone's details in case they may have changed
            Zone zone = cloudrainAPI.getZone(zoneId);
            if (zone != null && zoneId.equals(zone.getId())) {
                updateZoneProperties(zone);
                // retrieve the zone's irrigation status
                Irrigation irrigation = cloudrainAPI.getIrrigation(zoneId);
                // update the things state with the retrieved data
                updateIrrigationState(irrigation);
                return true;
            } else {
                logger.debug(ERROR_MSG_ZONE_NOT_FOUND, zoneId, getAccountConfig().getTestMode());
                updateStatus(ThingStatus.OFFLINE);
                return false;
            }
        } catch (CloudrainAPIException e) {
            logger.warn(ERROR_MSG_STATUS_UPDATE, zoneId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        // if a relevant channel is linked this zone has to be updated from the central status polling
        if (CHANNEL_GROUP_ID_IRRIGATION.equals(channelUID.getGroupId())) {
            // the account handler performs polling for all zones if at least one is registered for updates
            CloudrainAccountHanlder handler = getAccountHanlder();
            if (handler != null) {
                handler.registerForIrrigationUpdates(zoneId, getThing());
            } else {
                logger.warn(ERROR_MSG_ZONE_REGISTRATION, zoneId);
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        // if a channel is unlinked we need to check whether this zone still requires updates from polling
        if (!isAnyIrrigationChannelLinked()) {
            // if no channel is linked this zone may unregister from updates
            CloudrainAccountHanlder handler = getAccountHanlder();
            if (handler != null) {
                handler.unregisterFromIrrigationUpdates(zoneId);
            }
        }
    }

    private boolean isAnyIrrigationChannelLinked() {
        List<Channel> channels = getThing().getChannelsOfGroup(CHANNEL_GROUP_ID_IRRIGATION);
        // check whether there is still at least one linked channel
        for (Channel channel : channels) {
            Set<Item> linkedItems = itemChannelLinkRegistry.getLinkedItems(channel.getUID());
            if (!linkedItems.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the Zone Thing's properties
     *
     * @param zone the zone details
     */
    public void updateZoneProperties(Zone zone) {
        if (zoneId.equals(zone.getId())) {
            updateProperty(PROPERTY_ZONE_NAME, zone.getNameWithDefault());
            updateProperty(PROPERTY_CTRL_NAME, zone.getControllerNameWithDefault());
            updateProperty(PROPERTY_CTRL_ID, zone.getControlleIdWithDefault());
        }
    }

    /**
     * Updates the Zone Thing's Channels with data from irrigation data
     *
     * @param irrigation the running irrigation or null if no irrigation is running in the zone
     */
    public void updateIrrigationState(@Nullable Irrigation irrigation) {
        // Default Channel state
        State statusState = new StringType(CHANNEL_STATE_VALUE_OFF);
        State startTimeState = UnDefType.NULL;
        State endTimeState = UnDefType.NULL;
        State durationState = UnDefType.NULL;
        State remainingSecsState = UnDefType.NULL;

        if (irrigation != null) {
            // Retrieve actual state from irrigation if it is active
            if (irrigation.isActive()) {
                statusState = new StringType(CHANNEL_STATE_VALUE_ON);
                startTimeState = convertToState(irrigation.getStartTime(), timeZoneProvider);
                endTimeState = convertToState(irrigation.getPlannedEndTime(), timeZoneProvider);
                durationState = convertToState(irrigation.getDuration());
                remainingSecsState = convertToState(irrigation.getRemainingSeconds());
            }
        }
        // Update the state of the channels
        List<Channel> channels = getThing().getChannelsOfGroup(CHANNEL_GROUP_ID_IRRIGATION);
        for (Channel channel : channels) {
            if (CHANNEL_ID_STATE.equals(channel.getUID().getIdWithoutGroup())) {
                updateState(channel.getUID(), statusState);
            } else if (CHANNEL_ID_START_TIME.equals(channel.getUID().getIdWithoutGroup())) {
                updateState(channel.getUID(), startTimeState);
            } else if (CHANNEL_ID_END_TIME.equals(channel.getUID().getIdWithoutGroup())) {
                updateState(channel.getUID(), endTimeState);
            } else if (CHANNEL_ID_DURATION.equals(channel.getUID().getIdWithoutGroup())) {
                updateState(channel.getUID(), durationState);
            } else if (CHANNEL_ID_REMAINING_SECS.equals(channel.getUID().getIdWithoutGroup())) {
                updateState(channel.getUID(), remainingSecsState);
                // check for running real time update jobs and cancel to prevent parallel jobs
                ScheduledFuture<?> remSecsUpdateJob = this.remainingSecsUpdateJob;
                if (remSecsUpdateJob != null) {
                    remSecsUpdateJob.cancel(true);
                }
                // check if we need to schedule a real time update job
                if (irrigation != null && irrigation.isActive()) {
                    CloudrainConfig config = getAccountConfig();
                    if (config.getRealtimeUpdates() || config.getUpdateAfterIrrigation()) {
                        Integer remaining = irrigation.getRemainingSeconds();
                        if (remaining != null && remaining.intValue() > 0) {
                            this.remainingSecsUpdateJob = scheduler.scheduleWithFixedDelay(
                                    new RealtimeUpdateJob(channel, remaining, config), 1, 1, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to convert a LocalTime into a State object.
     *
     * @param lt a LocalTime object
     * @param provider the {@link TimeZoneProvider}
     * @return the corresponding {@link DateTimeType} or {@link UnDefType}.NULL
     */
    private State convertToState(@Nullable LocalTime lt, TimeZoneProvider provider) {
        if (lt == null) {
            return UnDefType.NULL;
        }
        LocalDateTime ldt = LocalDateTime.of(LocalDate.now(), lt);
        ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, timeZoneProvider.getTimeZone(), null);
        return new DateTimeType(zdt);
    }

    /**
     * Helper method to convert a Integer into a State object.
     *
     * @param value an Integer value to be converted
     * @return the corresponding {@link DecimalType} or {@link UnDefType}.NULL
     */
    private State convertToState(@Nullable Integer value) {
        if (value == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(value);
    }

    /**
     * Helper method to obtain the {@link CloudrainAccountHanlder}.
     *
     * @return the {@link CloudrainAccountHanlder}.
     */
    private @Nullable CloudrainAccountHanlder getAccountHanlder() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof CloudrainAccountHanlder) {
            return (CloudrainAccountHanlder) bridge.getHandler();
        }
        return null;
    }

    /**
     * Helper method to obtain the {@link CloudrainConfig} of the bridge / account.
     *
     * @return the {@link CloudrainConfig}.
     */
    private CloudrainConfig getAccountConfig() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return bridge.getConfiguration().as(CloudrainConfig.class);
        }
        // return a default configuration in case something went wrong
        return new CloudrainConfig();
    }
}
