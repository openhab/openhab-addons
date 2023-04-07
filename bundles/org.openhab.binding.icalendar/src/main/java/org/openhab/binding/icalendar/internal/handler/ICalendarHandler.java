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
package org.openhab.binding.icalendar.internal.handler;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.icalendar.internal.config.ICalendarConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.CalendarException;
import org.openhab.core.OpenHAB;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ICalendarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Support for Command Tags embedded in the Event description
 * @author Michael Wodniok - Added last_update-channel and additional needed handling of it
 * @author Michael Wodniok - Changed calculation of Future for refresh of channels
 */
@NonNullByDefault
public class ICalendarHandler extends BaseBridgeHandler implements CalendarUpdateListener {

    private final File calendarFile;
    private @Nullable ICalendarConfiguration configuration;
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(ICalendarHandler.class);
    private final TimeZoneProvider tzProvider;
    private @Nullable ScheduledFuture<?> pullJobFuture;
    private @Nullable AbstractPresentableCalendar runtimeCalendar;
    private @Nullable Instant calendarDownloadedTime;

    public ICalendarHandler(Bridge bridge, HttpClient httpClient, TimeZoneProvider tzProvider) {
        super(bridge);
        this.httpClient = httpClient;
        final File cacheFolder = new File(new File(OpenHAB.getUserDataFolder(), "cache"),
                "org.openhab.binding.icalendar");
        if (!cacheFolder.exists()) {
            logger.debug("Creating cache folder '{}'", cacheFolder.getAbsolutePath());
            cacheFolder.mkdirs();
        }
        calendarFile = new File(cacheFolder,
                getThing().getUID().getAsString().replaceAll("[<>:\"/\\\\|?*]", "_") + ".ical");
        this.tzProvider = tzProvider;
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> currentPullJobFuture = pullJobFuture;
        if (currentPullJobFuture != null) {
            currentPullJobFuture.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_LAST_UPDATE:
                if (command instanceof RefreshType) {
                    updateStates();
                }
                break;
            default:
                logger.warn("Framework sent command to unknown channel with id '{}'", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        migrateLastUpdateChannel();

        final ICalendarConfiguration currentConfiguration = getConfigAs(ICalendarConfiguration.class);
        configuration = currentConfiguration;

        try {
            if ((currentConfiguration.username == null && currentConfiguration.password != null)
                    || (currentConfiguration.username != null && currentConfiguration.password == null)) {
                throw new ConfigBrokenException("Only one of username and password was set. This is invalid.");
            }

            PullJob regularPull;
            final BigDecimal maxSizeBD = currentConfiguration.maxSize;
            if (maxSizeBD == null || maxSizeBD.intValue() < 1) {
                throw new ConfigBrokenException(
                        "maxSize is either not set or less than 1 (mebibyte), which is not allowed.");
            }
            final int maxSize = maxSizeBD.intValue();
            try {
                regularPull = new PullJob(httpClient, new URI(currentConfiguration.url), currentConfiguration.username,
                        currentConfiguration.password, calendarFile, maxSize * 1048576, this);
            } catch (URISyntaxException e) {
                throw new ConfigBrokenException(String.format(
                        "The URI '%s' for downloading the calendar contains syntax errors.", currentConfiguration.url));

            }

            final BigDecimal refreshTimeBD = currentConfiguration.refreshTime;
            if (refreshTimeBD == null || refreshTimeBD.longValue() < 1) {
                throw new ConfigBrokenException(
                        "refreshTime is either not set or less than 1 (minute), which is not allowed.");
            }
            final long refreshTime = refreshTimeBD.longValue();
            if (calendarFile.isFile()) {
                updateStatus(ThingStatus.ONLINE);

                scheduler.submit(() -> {
                    // reload calendar file asynchronously
                    if (reloadCalendar()) {
                        updateStates();
                        updateChildren();
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "The calendar seems to be configured correctly, but the local copy of calendar could not be loaded.");
                    }
                });
                pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, refreshTime, refreshTime,
                        TimeUnit.MINUTES);
            } else {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug(
                        "The calendar is currently offline as no local copy exists. It will go online as soon as a valid valid calendar is retrieved.");
                pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, 0, refreshTime, TimeUnit.MINUTES);
            }
        } catch (ConfigBrokenException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        final AbstractPresentableCalendar calendar = runtimeCalendar;
        if (calendar != null) {
            updateChild(childHandler);
        }
    }

    @Override
    public void onCalendarUpdated() {
        if (reloadCalendar()) {
            updateStates();
            updateChildren();
        } else {
            logger.trace("Calendar was updated, but loading failed.");
        }
    }

    /**
     * @return the calendar that is used for all operations
     */
    @Nullable
    public AbstractPresentableCalendar getRuntimeCalendar() {
        return runtimeCalendar;
    }

    /**
     * Migration for last_update-channel as this change is compatible to previous instances.
     */
    private void migrateLastUpdateChannel() {
        final Thing thing = getThing();
        if (thing.getChannel(CHANNEL_LAST_UPDATE) == null) {
            logger.trace("last_update channel is missing in this Thing. Adding it.");
            final ThingHandlerCallback callback = getCallback();
            if (callback == null) {
                logger.debug("ThingHandlerCallback is null. Skipping migration of last_update channel.");
                return;
            }
            final ChannelBuilder channelBuilder = callback
                    .createChannelBuilder(new ChannelUID(thing.getUID(), CHANNEL_LAST_UPDATE), LAST_UPDATE_TYPE_UID);
            final ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannel(channelBuilder.build());
            updateThing(thingBuilder.build());
        }
    }

    /**
     * Reloads the calendar from local ical-file. Replaces the class internal calendar - if loading succeeds. Else
     * logging details at warn-level logger.
     *
     * @return Whether the calendar was loaded successfully.
     */
    private boolean reloadCalendar() {
        logger.trace("reloading calendar of {}", getThing().getUID());
        if (!calendarFile.isFile()) {
            logger.info("Local file for reloading calendar is missing.");
            return false;
        }
        final ICalendarConfiguration config = configuration;
        if (config == null) {
            logger.warn("Can't reload calendar when configuration is missing.");
            return false;
        }
        try (final FileInputStream fileStream = new FileInputStream(calendarFile)) {
            final AbstractPresentableCalendar calendar = AbstractPresentableCalendar.create(fileStream);
            runtimeCalendar = calendar;
            calendarDownloadedTime = Instant.ofEpochMilli(calendarFile.lastModified());
        } catch (IOException | CalendarException e) {
            logger.warn("Loading calendar failed: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Updates the states of the Thing and its channels.
     */
    private void updateStates() {
        logger.trace("updating states of {}", getThing().getUID());
        final AbstractPresentableCalendar calendar = runtimeCalendar;
        if (calendar == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
            final Instant lastUpdate = calendarDownloadedTime;
            updateState(CHANNEL_LAST_UPDATE,
                    (lastUpdate != null ? new DateTimeType(lastUpdate.atZone(tzProvider.getTimeZone()))
                            : UnDefType.UNDEF));
        }
    }

    /**
     * Updates all children of this handler.
     */
    private void updateChildren() {
        getThing().getThings().forEach(childThing -> updateChild(childThing.getHandler()));
    }

    /**
     * Updates a specific child handler.
     *
     * @param childHandler the handler to be updated
     */
    private void updateChild(@Nullable ThingHandler childHandler) {
        if (childHandler instanceof CalendarUpdateListener updateListener) {
            logger.trace("Notifying {} about fresh calendar.", childHandler.getThing().getUID());
            try {
                updateListener.onCalendarUpdated();
            } catch (Exception e) {
                logger.trace("The update of a child handler failed. Ignoring.", e);
            }
        }
    }
}
