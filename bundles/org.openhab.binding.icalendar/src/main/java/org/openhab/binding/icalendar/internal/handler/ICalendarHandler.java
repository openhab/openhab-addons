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
package org.openhab.binding.icalendar.internal.handler;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.icalendar.internal.config.ICalendarConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.CalendarException;
import org.openhab.binding.icalendar.internal.logic.CommandTag;
import org.openhab.binding.icalendar.internal.logic.CommandTagType;
import org.openhab.binding.icalendar.internal.logic.Event;
import org.openhab.core.OpenHAB;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
    private final EventPublisher eventPublisherCallback;
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(ICalendarHandler.class);
    private final TimeZoneProvider tzProvider;
    private @Nullable ScheduledFuture<?> pullJobFuture;
    private @Nullable AbstractPresentableCalendar runtimeCalendar;
    private @Nullable ScheduledFuture<?> updateJobFuture;
    private Instant updateStatesLastCalledTime;
    private @Nullable Instant calendarDownloadedTime;

    public ICalendarHandler(Bridge bridge, HttpClient httpClient, EventPublisher eventPublisher,
            TimeZoneProvider tzProvider) {
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
        eventPublisherCallback = eventPublisher;
        updateStatesLastCalledTime = Instant.now();
        this.tzProvider = tzProvider;
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> currentUpdateJobFuture = updateJobFuture;
        if (currentUpdateJobFuture != null) {
            currentUpdateJobFuture.cancel(true);
        }
        final ScheduledFuture<?> currentPullJobFuture = pullJobFuture;
        if (currentPullJobFuture != null) {
            currentPullJobFuture.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_CURRENT_EVENT_PRESENT:
            case CHANNEL_CURRENT_EVENT_TITLE:
            case CHANNEL_CURRENT_EVENT_START:
            case CHANNEL_CURRENT_EVENT_END:
            case CHANNEL_NEXT_EVENT_TITLE:
            case CHANNEL_NEXT_EVENT_START:
            case CHANNEL_NEXT_EVENT_END:
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

    private void executeEventCommands(List<Event> events, CommandTagType execTime) {
        // no begun or ended events => exit quietly as there is nothing to do
        if (events.isEmpty()) {
            return;
        }

        final ICalendarConfiguration syncConfiguration = configuration;
        if (syncConfiguration == null) {
            logger.debug("Configuration not instantiated!");
            return;
        }
        // loop through all events in the list
        for (Event event : events) {

            // loop through all command tags in the event
            for (CommandTag cmdTag : event.commandTags) {

                // only process the BEGIN resp. END tags
                if (cmdTag.getTagType() != execTime) {
                    continue;
                }
                if (!cmdTag.isAuthorized(syncConfiguration.authorizationCode)) {
                    logger.warn("Event: {}, Command Tag: {} => Command not authorized!", event.title,
                            cmdTag.getFullTag());
                    continue;
                }

                final Command cmdState = cmdTag.getCommand();
                if (cmdState == null) {
                    logger.warn("Event: {}, Command Tag: {} => Error creating Command State!", event.title,
                            cmdTag.getFullTag());
                    continue;
                }

                // (try to) execute the command
                try {
                    eventPublisherCallback.post(ItemEventFactory.createCommandEvent(cmdTag.getItemName(), cmdState));
                    if (logger.isDebugEnabled()) {
                        String cmdType = cmdState.getClass().toString();
                        int index = cmdType.lastIndexOf(".") + 1;
                        if ((index > 0) && (index < cmdType.length())) {
                            cmdType = cmdType.substring(index);
                        }
                        logger.debug("Event: {}, Command Tag: {} => {}.postUpdate({}: {})", event.title,
                                cmdTag.getFullTag(), cmdTag.getItemName(), cmdType, cmdState);
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    logger.warn("Event: {}, Command Tag: {} => Unable to push command to target item!", event.title,
                            cmdTag.getFullTag());
                    logger.debug("Exception occured while pushing to item!", e);
                }
            }
        }
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
            rescheduleCalendarStateUpdate();
            calendarDownloadedTime = Instant.ofEpochMilli(calendarFile.lastModified());
        } catch (IOException | CalendarException e) {
            logger.warn("Loading calendar failed: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Reschedules the next update of the states.
     */
    private void rescheduleCalendarStateUpdate() {
        final ScheduledFuture<?> currentUpdateJobFuture = updateJobFuture;
        if (currentUpdateJobFuture != null) {
            if (!(currentUpdateJobFuture.isCancelled() || currentUpdateJobFuture.isDone())) {
                currentUpdateJobFuture.cancel(true);
            }
            updateJobFuture = null;
        }
        final AbstractPresentableCalendar currentCalendar = runtimeCalendar;
        if (currentCalendar == null) {
            return;
        }
        final Instant now = Instant.now();
        Instant nextRegularUpdate = null;
        if (currentCalendar.isEventPresent(now)) {
            final Event currentEvent = currentCalendar.getCurrentEvent(now);
            if (currentEvent == null) {
                logger.debug(
                        "Could not schedule next update of states, due to unexpected behaviour of calendar implementation.");
                return;
            }
            nextRegularUpdate = currentEvent.end;
        }

        final Event nextEvent = currentCalendar.getNextEvent(now);
        final ICalendarConfiguration currentConfig = this.configuration;
        if (currentConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Something is broken, the configuration is not available.");
            return;
        }
        if (nextEvent != null) {
            if (nextRegularUpdate == null || nextEvent.start.isBefore(nextRegularUpdate)) {
                nextRegularUpdate = nextEvent.start;
            }
        }

        if (nextRegularUpdate != null) {
            updateJobFuture = scheduler.schedule(() -> {
                ICalendarHandler.this.updateStates();
                ICalendarHandler.this.rescheduleCalendarStateUpdate();
            }, nextRegularUpdate.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
            logger.debug("Scheduled update in {} seconds", nextRegularUpdate.getEpochSecond() - now.getEpochSecond());
        } else {
            updateJobFuture = scheduler.schedule(() -> {
                ICalendarHandler.this.rescheduleCalendarStateUpdate();
            }, 1L, TimeUnit.DAYS);
            logger.debug("Scheduled reschedule in 1 day");
        }
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

            final Instant now = Instant.now();
            if (calendar.isEventPresent(now)) {
                updateState(CHANNEL_CURRENT_EVENT_PRESENT, OnOffType.ON);
                final Event currentEvent = calendar.getCurrentEvent(now);
                if (currentEvent == null) {
                    logger.warn("Unexpected inconsistency of internal API. Not Updating event details.");
                } else {
                    updateState(CHANNEL_CURRENT_EVENT_TITLE, new StringType(currentEvent.title));
                    updateState(CHANNEL_CURRENT_EVENT_START,
                            new DateTimeType(currentEvent.start.atZone(tzProvider.getTimeZone())));
                    updateState(CHANNEL_CURRENT_EVENT_END,
                            new DateTimeType(currentEvent.end.atZone(tzProvider.getTimeZone())));
                }
            } else {
                updateState(CHANNEL_CURRENT_EVENT_PRESENT, OnOffType.OFF);
                updateState(CHANNEL_CURRENT_EVENT_TITLE, UnDefType.UNDEF);
                updateState(CHANNEL_CURRENT_EVENT_START, UnDefType.UNDEF);
                updateState(CHANNEL_CURRENT_EVENT_END, UnDefType.UNDEF);
            }

            final Event nextEvent = calendar.getNextEvent(now);
            if (nextEvent != null) {
                updateState(CHANNEL_NEXT_EVENT_TITLE, new StringType(nextEvent.title));
                updateState(CHANNEL_NEXT_EVENT_START,
                        new DateTimeType(nextEvent.start.atZone(tzProvider.getTimeZone())));
                updateState(CHANNEL_NEXT_EVENT_END, new DateTimeType(nextEvent.end.atZone(tzProvider.getTimeZone())));
            } else {
                updateState(CHANNEL_NEXT_EVENT_TITLE, UnDefType.UNDEF);
                updateState(CHANNEL_NEXT_EVENT_START, UnDefType.UNDEF);
                updateState(CHANNEL_NEXT_EVENT_END, UnDefType.UNDEF);
            }

            final Instant lastUpdate = calendarDownloadedTime;
            updateState(CHANNEL_LAST_UPDATE,
                    (lastUpdate != null ? new DateTimeType(lastUpdate.atZone(tzProvider.getTimeZone()))
                            : UnDefType.UNDEF));

            // process all Command Tags in all Calendar Events which ENDED since updateStates was last called
            // the END Event tags must be processed before the BEGIN ones
            executeEventCommands(calendar.getJustEndedEvents(updateStatesLastCalledTime, now), CommandTagType.END);

            // process all Command Tags in all Calendar Events which BEGAN since updateStates was last called
            // the END Event tags must be processed before the BEGIN ones
            executeEventCommands(calendar.getJustBegunEvents(updateStatesLastCalledTime, now), CommandTagType.BEGIN);

            // save time when updateStates was previously called
            // the purpose is to prevent repeat command execution of events that have already been executed
            updateStatesLastCalledTime = now;
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
        if (childHandler instanceof CalendarUpdateListener) {
            logger.trace("Notifying {} about fresh calendar.", childHandler.getThing().getUID());
            try {
                ((CalendarUpdateListener) childHandler).onCalendarUpdated();
            } catch (Exception e) {
                logger.trace("The update of a child handler failed. Ignoring.", e);
            }
        }
    }
}
