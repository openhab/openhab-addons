/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.handler;

import static org.openhab.binding.darksky.internal.DarkSkyBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelGroupUID;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.darksky.internal.config.DarkSkyChannelConfiguration;
import org.openhab.binding.darksky.internal.config.DarkSkyLocationConfiguration;
import org.openhab.binding.darksky.internal.connection.DarkSkyCommunicationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConfigurationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractDarkSkyHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDarkSkyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractDarkSkyHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(THING_TYPE_WEATHER_AND_FORECAST);

    // keeps track of all jobs
    private static final Map<String, Job> JOBS = new ConcurrentHashMap<>();

    // keeps track of the parsed location
    protected @Nullable PointType location;

    public AbstractDarkSkyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        DarkSkyLocationConfiguration config = getConfigAs(DarkSkyLocationConfiguration.class);

        boolean configValid = true;
        if (StringUtils.trimToNull(config.getLocation()) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-location");
            configValid = false;
        }

        try {
            location = new PointType(config.getLocation());
        } catch (IllegalArgumentException e) {
            location = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-parsing-location");
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        cancelAllJobs();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else {
            logger.debug("The Dark Sky binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())
                && !ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Updates Dark Sky data for this location.
     *
     * @param connection {@link DarkSkyConnection} instance
     */
    public void updateData(DarkSkyConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (DarkSkyCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (DarkSkyConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Requests the data from Dark Sky API.
     *
     * @param connection {@link DarkSkyConnection} instance
     * @return true, if the request for the Dark Sky data was successful
     * @throws DarkSkyCommunicationException
     * @throws DarkSkyConfigurationException
     */
    protected abstract boolean requestData(DarkSkyConnection connection)
            throws DarkSkyCommunicationException, DarkSkyConfigurationException;

    /**
     * Updates all channels of this handler from the latest Dark Sky data retrieved.
     */
    private void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup() && channelUID.getGroupId() != null
                    && isLinked(channelUID)) {
                updateChannel(channelUID);
            }
        }
    }

    /**
     * Updates the channel with the given UID from the latest Dark Sky data retrieved.
     *
     * @param channelUID UID of the channel
     */
    protected abstract void updateChannel(ChannelUID channelUID);

    /**
     * Applies the given configuration to the given timestamp.
     *
     * @param dateTime timestamp represented as {@link ZonedDateTime}
     * @param config {@link DarkSkyChannelConfiguration} instance
     * @return the modified timestamp
     */
    protected ZonedDateTime applyChannelConfig(ZonedDateTime dateTime, @Nullable DarkSkyChannelConfiguration config) {
        ZonedDateTime modifiedDateTime = dateTime;
        if (config != null) {
            if (config.getOffset() != 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Apply offset of {} min to timestamp '{}'.", config.getOffset(),
                            modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                modifiedDateTime = modifiedDateTime.plusMinutes(config.getOffset());
            }
            long earliestInMinutes = config.getEarliestInMinutes();
            if (earliestInMinutes > 0) {
                ZonedDateTime earliestDateTime = modifiedDateTime.truncatedTo(ChronoUnit.DAYS)
                        .plusMinutes(earliestInMinutes);
                if (modifiedDateTime.isBefore(earliestDateTime)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Use earliest timestamp '{}' instead of '{}'.",
                                earliestDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    return earliestDateTime;
                }
            }
            long latestInMinutes = config.getLatestInMinutes();
            if (latestInMinutes > 0) {
                ZonedDateTime latestDateTime = modifiedDateTime.truncatedTo(ChronoUnit.DAYS)
                        .plusMinutes(latestInMinutes);
                if (modifiedDateTime.isAfter(latestDateTime)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Use latest timestamp '{}' instead of '{}'.",
                                latestDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    return latestDateTime;
                }
            }
        }
        return modifiedDateTime;
    }

    protected State getDateTimeTypeState(int value) {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneId.systemDefault()));
    }

    protected State getDecimalTypeState(double value) {
        return new DecimalType(value);
    }

    protected State getRawTypeState(@Nullable RawType image) {
        return (image == null) ? UnDefType.UNDEF : image;
    }

    protected State getStringTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new StringType(value);
    }

    protected State getQuantityTypeState(double value, Unit<?> unit) {
        return new QuantityType<>(value, unit);
    }

    /**
     * Creates all {@link Channel}s for the given {@link ChannelGroupTypeUID}.
     *
     * @param channelGroupId the channel group id
     * @param channelGroupTypeUID the {@link ChannelGroupTypeUID}
     * @return a list of all {@link Channel}s for the channel group
     */
    protected List<Channel> createChannelsForGroup(String channelGroupId, ChannelGroupTypeUID channelGroupTypeUID) {
        logger.debug("Building channel group '{}' for thing '{}'.", channelGroupId, getThing().getUID());
        List<Channel> channels = new ArrayList<>();
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            for (ChannelBuilder channelBuilder : callback.createChannelBuilders(
                    new ChannelGroupUID(getThing().getUID(), channelGroupId), channelGroupTypeUID)) {
                channels.add(channelBuilder.build());
            }
        }
        return channels;
    }

    /**
     * Removes all {@link Channel}s of the given channel group.
     *
     * @param channelGroupId the channel group id
     * @return a list of all {@link Channel}s in the given channel group
     */
    protected List<Channel> removeChannelsOfGroup(String channelGroupId) {
        logger.debug("Removing channel group '{}' from thing '{}'.", channelGroupId, getThing().getUID());
        return getThing().getChannelsOfGroup(channelGroupId);
    }

    /**
     * Schedules or reschedules a job for the channel with the given id if the given timestamp is in the future.
     *
     * @param channelId id of the channel
     * @param dateTime timestamp of the job represented as {@link ZonedDateTime}
     */
    @SuppressWarnings("null")
    protected synchronized void scheduleJob(String channelId, ZonedDateTime dateTime) {
        long delay = dateTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
        if (delay > 0) {
            Job job = JOBS.get(channelId);
            if (job == null || job.getFuture().isCancelled()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Schedule job for '{}' in {} s (at '{}').", channelId, delay,
                            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                JOBS.put(channelId, new Job(channelId, delay));
            } else {
                if (delay != job.getDelay()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reschedule job for '{}' in {} s (at '{}').", channelId, delay,
                                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    job.getFuture().cancel(true);
                    JOBS.put(channelId, new Job(channelId, delay));
                }
            }
        }
    }

    /**
     * Cancels all jobs.
     */
    protected void cancelAllJobs() {
        logger.debug("Cancel all jobs.");
        JOBS.keySet().forEach(this::cancelJob);
    }

    /**
     * Cancels the job for the channel with the given id.
     *
     * @param channelId id of the channel
     */
    @SuppressWarnings("null")
    protected synchronized void cancelJob(String channelId) {
        Job job = JOBS.remove(channelId);
        if (job != null && !job.getFuture().isCancelled()) {
            logger.debug("Cancel job for '{}'.", channelId);
            job.getFuture().cancel(true);
        }
    }

    /**
     * Executes the job for the channel with the given id.
     *
     * @param channelId id of the channel
     */
    private void executeJob(String channelId) {
        logger.debug("Trigger channel '{}' with event '{}'.", channelId, EVENT_START);
        triggerChannel(channelId, EVENT_START);
    }

    private final class Job {
        private final long delay;
        private final ScheduledFuture<?> future;

        public Job(String event, long delay) {
            this.delay = delay;
            this.future = scheduler.schedule(() -> {
                executeJob(event);
            }, delay, TimeUnit.SECONDS);
        }

        public long getDelay() {
            return delay;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }
    }
}
