/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.icalendar.internal.config.ICalendarConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.CalendarException;
import org.openhab.binding.icalendar.internal.logic.CommandTag;
import org.openhab.binding.icalendar.internal.logic.CommandTagType;
import org.openhab.binding.icalendar.internal.logic.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ICalendarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Wodniok - Initial contribution
 *
 * @author Andrew Fiddian-Green - Support for Command Tags embedded in the Event description
 *
 */
@NonNullByDefault
public class ICalendarHandler extends BaseThingHandler implements CalendarUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(ICalendarHandler.class);
    private HttpClient httpClient;

    private @Nullable ICalendarConfiguration configuration;
    private File calendarFile;
    private @Nullable AbstractPresentableCalendar runtimeCalendar;

    private @Nullable ScheduledFuture<?> updateJobFuture;
    private @Nullable ScheduledFuture<?> pullJobFuture;

    @Nullable
    private EventPublisher eventPublisherCallback = null;

    private Instant updateStatesLastCalledTime = Instant.now();

    public ICalendarHandler(Thing thing, HttpClient httpClient, @Nullable EventPublisher eventPublisher) {
        super(thing);
        this.httpClient = httpClient;
        calendarFile = new File(ConfigConstants.getUserDataFolder() + File.separator
                + getThing().getUID().getAsString().replaceAll("[<>:\"/\\\\|?*]", "_") + ".ical");
        eventPublisherCallback = eventPublisher;
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
        updateStatus(ThingStatus.UNKNOWN);

        ICalendarConfiguration currentConfiguration = getConfigAs(ICalendarConfiguration.class);
        configuration = currentConfiguration;

        if ((currentConfiguration.username == null && currentConfiguration.password != null)
                || (currentConfiguration.username != null && currentConfiguration.password == null)) {
            logger.warn("Only one of username and password was set. This is invalid.");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        PullJob regularPull;
        try {
            regularPull = new PullJob(httpClient, new URI(currentConfiguration.url), currentConfiguration.username,
                    currentConfiguration.password, calendarFile, this);
        } catch (URISyntaxException e) {
            logger.warn(
                    "The URI '{}' for downloading the calendar contains syntax errors. This will result in no downloads/updates.",
                    currentConfiguration.url, e);
            return;
        }

        if (calendarFile.isFile()) {
            if (reloadCalendar()) {
                updateStatus(ThingStatus.ONLINE);
                updateStates();
                rescheduleCalendarStateUpdate();
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, currentConfiguration.refreshTime.longValue(),
                    currentConfiguration.refreshTime.longValue(), TimeUnit.MINUTES);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, 0,
                    currentConfiguration.refreshTime.longValue(), TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> currentUpdateJobFuture = updateJobFuture;
        if (currentUpdateJobFuture != null) {
            currentUpdateJobFuture.cancel(true);
        }
        ScheduledFuture<?> currentPullJobFuture = pullJobFuture;
        if (currentPullJobFuture != null) {
            currentPullJobFuture.cancel(true);
        }
    }

    /**
     * Reloads the calendar from local ical-file. Replaces the class internal calendar - if loading succeeds. Else
     * logging details at warn-level logger.
     *
     * @return Whether the calendar was loaded successfully.
     */
    private boolean reloadCalendar() {
        if (!calendarFile.isFile()) {
            logger.warn("Local file for loading calendar is missing.");
            return false;
        }
        ICalendarConfiguration config = configuration;
        if (config == null) {
            logger.warn("Can't reload calendar when configuration is missing.");
            return false;
        }
        try {
            AbstractPresentableCalendar calendar = AbstractPresentableCalendar
                    .create(new FileInputStream(calendarFile));
            runtimeCalendar = calendar;
            rescheduleCalendarStateUpdate();
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
        AbstractPresentableCalendar calendar = runtimeCalendar;
        if (calendar == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);

            Channel eventPresenceChannel = thing.getChannel(CHANNEL_CURRENT_EVENT_PRESENT);
            Channel currentEventTitleChannel = thing.getChannel(CHANNEL_CURRENT_EVENT_TITLE);
            Channel currentEventStartChannel = thing.getChannel(CHANNEL_CURRENT_EVENT_START);
            Channel currentEventEndChannel = thing.getChannel(CHANNEL_CURRENT_EVENT_END);
            Channel nextEventTitleChannel = thing.getChannel(CHANNEL_NEXT_EVENT_TITLE);
            Channel nextEventStartChannel = thing.getChannel(CHANNEL_NEXT_EVENT_START);
            Channel nextEventEndChannel = thing.getChannel(CHANNEL_NEXT_EVENT_END);
            if (eventPresenceChannel == null || currentEventTitleChannel == null || currentEventStartChannel == null
                    || currentEventEndChannel == null || nextEventTitleChannel == null || nextEventStartChannel == null
                    || nextEventEndChannel == null) {
                logger.warn("Could not retrieve one or more calendar channels. Not updating.");
                return;
            }

            Instant now = Instant.now();
            if (calendar.isEventPresent(now)) {
                updateState(eventPresenceChannel.getUID(), OnOffType.ON);
                Event currentEvent = calendar.getCurrentEvent(now);
                if (currentEvent == null) {
                    logger.warn("Unexpected inconsistency of internal API. Not Updating event details.");
                } else {
                    updateState(currentEventTitleChannel.getUID(), new StringType(currentEvent.title));
                    updateState(currentEventStartChannel.getUID(),
                            new DateTimeType(currentEvent.start.atZone(ZoneId.systemDefault())));
                    updateState(currentEventEndChannel.getUID(),
                            new DateTimeType(currentEvent.end.atZone(ZoneId.systemDefault())));
                }
            } else {
                updateState(eventPresenceChannel.getUID(), OnOffType.OFF);
                updateState(currentEventTitleChannel.getUID(), UnDefType.NULL);
                updateState(currentEventStartChannel.getUID(), UnDefType.NULL);
                updateState(currentEventEndChannel.getUID(), UnDefType.NULL);
            }

            Event nextEvent = calendar.getNextEvent(now);
            if (nextEvent != null) {
                updateState(nextEventTitleChannel.getUID(), new StringType(nextEvent.title));
                updateState(nextEventStartChannel.getUID(),
                        new DateTimeType(nextEvent.start.atZone(ZoneId.systemDefault())));
                updateState(nextEventEndChannel.getUID(),
                        new DateTimeType(nextEvent.end.atZone(ZoneId.systemDefault())));
            } else {
                updateState(nextEventTitleChannel.getUID(), UnDefType.NULL);
                updateState(nextEventStartChannel.getUID(), UnDefType.NULL);
                updateState(nextEventEndChannel.getUID(), UnDefType.NULL);
            }

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

    private void executeEventCommands(@Nullable List<Event> events, CommandTagType execTime) {
        // no begun or ended events => exit quietly as there is nothing to do
        if ((events == null) || (events.isEmpty())) {
            return;
        }
        // prevent synchronization issues (MVN null pointer warnings) in "eventPublisherCallback"
        @Nullable
        EventPublisher syncEventPublisherCallback = eventPublisherCallback;
        if (syncEventPublisherCallback == null) {
            logger.warn("EventPublisher object not instantiated!");
            return;
        }
        // prevent potential synchronization issues (MVN null pointer warnings) in "configuration"
        @Nullable
        ICalendarConfiguration syncConfiguration = configuration;
        if (syncConfiguration == null) {
            logger.warn("Configuration not instantiated!");
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
                    logger.warn("Event: {}, Command Tag: {} => Not authorized!", event.title, cmdTag.getFullTag());
                    continue;
                }
                String itemName = cmdTag.getItemName();
                if (!itemName.matches("^\\w+$")) {
                    logger.warn("Event: {}, Command Tag: {} => Bad syntax for Item name!", event.title,
                            cmdTag.getFullTag());
                    continue;
                }
                Command cmdState = cmdTag.getCommand();
                if (cmdState == null) {
                    logger.warn("Event: {}, Command Tag: {} => Invalid Target State!", event.title,
                            cmdTag.getFullTag());
                    continue;
                }

                // (try to) execute the command
                try {
                    syncEventPublisherCallback.post(ItemEventFactory.createCommandEvent(itemName, cmdState));
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
                    logger.warn("Event: {}, Command Tag: {} => Could not be pushed to target item.", event.title,
                            cmdTag.getFullTag());
                    logger.debug("Exception occured while pushing to item.", e);
                }
            }
        }
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisherCallback = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        eventPublisherCallback = null;
    }

    /**
     * Reschedules the next update of the states.
     */
    private void rescheduleCalendarStateUpdate() {
        ScheduledFuture<?> currentUpdateJobFuture = updateJobFuture;
        if (currentUpdateJobFuture != null) {
            if (!(currentUpdateJobFuture.isCancelled() || currentUpdateJobFuture.isDone())) {
                currentUpdateJobFuture.cancel(true);
            }
            updateJobFuture = null;
        }
        AbstractPresentableCalendar currentCalendar = runtimeCalendar;
        if (currentCalendar == null) {
            return;
        }
        Instant now = Instant.now();
        if (currentCalendar.isEventPresent(now)) {
            Event currentEvent = currentCalendar.getCurrentEvent(now);
            if (currentEvent == null) {
                logger.debug(
                        "Could not schedule next update of states, due to unexpected behaviour of calendar implementation.");
                return;
            }
            updateJobFuture = scheduler.schedule(() -> {
                ICalendarHandler.this.updateStates();
                ICalendarHandler.this.rescheduleCalendarStateUpdate();
            }, currentEvent.end.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
        } else {
            Event nextEvent = currentCalendar.getNextEvent(now);
            ICalendarConfiguration currentConfig = this.configuration;
            if (currentConfig == null) {
                logger.debug("Something is broken, the configuration is not available.");
                return;
            }
            if (nextEvent == null) {
                updateJobFuture = scheduler.schedule(() -> {
                    ICalendarHandler.this.rescheduleCalendarStateUpdate();
                }, 1L, TimeUnit.DAYS);
            } else {
                updateJobFuture = scheduler.schedule(() -> {
                    ICalendarHandler.this.updateStates();
                    ICalendarHandler.this.rescheduleCalendarStateUpdate();
                }, nextEvent.start.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onCalendarUpdated() {
        if (reloadCalendar()) {
            updateStates();
        } else {
            logger.trace("Calendar was updated, but loading failed.");
        }
    }

}
