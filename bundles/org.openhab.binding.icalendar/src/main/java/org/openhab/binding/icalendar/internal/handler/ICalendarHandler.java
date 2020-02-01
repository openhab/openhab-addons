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
import java.time.Duration;
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
        this.calendarFile = new File(ConfigConstants.getUserDataFolder() + File.separator
                + getThing().getUID().getAsString().replaceAll("[^a-zA-Z0-9\\._-]", "_") + ".ical");
        this.runtimeCalendar = null;
        this.eventPublisherCallback = eventPublisher;
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
                    this.updateStates();
                }
                break;
            default:
                this.logger.warn("Framework sent command to unknown channel with id '{}'", channelUID.getId());
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        this.configuration = getConfigAs(ICalendarConfiguration.class);
        ICalendarConfiguration currentConfiguration = this.configuration;
        if (currentConfiguration == null) {
            logger.warn("Something in API is behaving wrong. Stopping initialization.");
            return;
        }

        if ((currentConfiguration.username == null && currentConfiguration.password != null)
                || (currentConfiguration.username != null && currentConfiguration.password == null)) {
            logger.warn("Only one of username and password was set. This is invalid.");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        PullJob regularPull;
        try {
            regularPull = new PullJob(this.httpClient, new URI(currentConfiguration.url), currentConfiguration.username,
                    currentConfiguration.password, this.calendarFile, this);
        } catch (URISyntaxException e) {
            logger.warn(
                    "The URI for downloading the calendar contains syntax errors. This will result in no downloads/updates.",
                    e);
            return;
        }

        if (this.calendarFile.isFile()) {
            if (this.reloadCalendar()) {
                this.updateStatus(ThingStatus.ONLINE);
                this.updateStates();
                this.rescheduleCalendarStateUpdate();
            } else {
                this.updateStatus(ThingStatus.OFFLINE);
            }
            pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, currentConfiguration.refreshTime.longValue(),
                    currentConfiguration.refreshTime.longValue(), TimeUnit.MINUTES);
        } else {
            this.updateStatus(ThingStatus.OFFLINE);
            pullJobFuture = scheduler.scheduleWithFixedDelay(regularPull, 0,
                    currentConfiguration.refreshTime.longValue(), TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> currentUpdateJobFuture = this.updateJobFuture;
        if (currentUpdateJobFuture != null) {
            currentUpdateJobFuture.cancel(true);
        }
        ScheduledFuture<?> currentPullJobFuture = this.pullJobFuture;
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
        if (!this.calendarFile.isFile()) {
            this.logger.warn("Local file for loading calendar is missing.");
            return false;
        }
        ICalendarConfiguration config = this.configuration;
        if (config == null) {
            this.logger.warn("Can't reload calendar when configuration is missing.");
            return false;
        }
        try {
            AbstractPresentableCalendar calendar = AbstractPresentableCalendar.create(
                    new FileInputStream(this.calendarFile), Duration.ofMinutes(config.readAroundTime.longValue()));
            this.runtimeCalendar = calendar;
            this.rescheduleCalendarStateUpdate();
        } catch (IOException | CalendarException e) {
            this.logger.warn("Loading calendar failed.", e);
            return false;
        }
        return true;
    }

    /**
     * Updates the states of the Thing and it's channels.
     */
    private void updateStates() {
        AbstractPresentableCalendar calendar = this.runtimeCalendar;
        if (calendar == null) {
            this.updateStatus(ThingStatus.OFFLINE);
        } else {
            this.updateStatus(ThingStatus.ONLINE);

            Channel eventPresenceChannel = this.thing.getChannel(CHANNEL_CURRENT_EVENT_PRESENT);
            Channel currentEventTitleChannel = this.thing.getChannel(CHANNEL_CURRENT_EVENT_TITLE);
            Channel currentEventStartChannel = this.thing.getChannel(CHANNEL_CURRENT_EVENT_START);
            Channel currentEventEndChannel = this.thing.getChannel(CHANNEL_CURRENT_EVENT_END);
            Channel nextEventTitleChannel = this.thing.getChannel(CHANNEL_NEXT_EVENT_TITLE);
            Channel nextEventStartChannel = this.thing.getChannel(CHANNEL_NEXT_EVENT_START);
            Channel nextEventEndChannel = this.thing.getChannel(CHANNEL_NEXT_EVENT_END);
            if (eventPresenceChannel == null || currentEventTitleChannel == null || currentEventStartChannel == null
                    || currentEventEndChannel == null || nextEventTitleChannel == null || nextEventStartChannel == null
                    || nextEventEndChannel == null) {
                logger.warn("Could not retrieve one or more calendar channels. Not updating.");
                return;
            }

            Instant now = Instant.now();
            if (calendar.isEventPresent(now)) {
                this.updateState(eventPresenceChannel.getUID(), OnOffType.ON);
                Event currentEvent = calendar.getCurrentEvent(now);
                if (currentEvent == null) {
                    logger.warn("Unexpected inconsistency of internal API. Not Updating event details.");
                } else {
                    this.updateState(currentEventTitleChannel.getUID(), new StringType(currentEvent.title));
                    this.updateState(currentEventStartChannel.getUID(),
                            new DateTimeType(currentEvent.start.atZone(ZoneId.systemDefault())));
                    this.updateState(currentEventEndChannel.getUID(),
                            new DateTimeType(currentEvent.end.atZone(ZoneId.systemDefault())));
                }
            } else {
                this.updateState(eventPresenceChannel.getUID(), OnOffType.OFF);
                this.updateState(currentEventTitleChannel.getUID(), UnDefType.NULL);
                this.updateState(currentEventStartChannel.getUID(), UnDefType.NULL);
                this.updateState(currentEventEndChannel.getUID(), UnDefType.NULL);
            }

            Event nextEvent = calendar.getNextEvent(now);
            if (nextEvent != null) {
                this.updateState(nextEventTitleChannel.getUID(), new StringType(nextEvent.title));
                this.updateState(nextEventStartChannel.getUID(),
                        new DateTimeType(nextEvent.start.atZone(ZoneId.systemDefault())));
                this.updateState(nextEventEndChannel.getUID(),
                        new DateTimeType(nextEvent.end.atZone(ZoneId.systemDefault())));
            } else {
                this.updateState(nextEventTitleChannel.getUID(), UnDefType.NULL);
                this.updateState(nextEventStartChannel.getUID(), UnDefType.NULL);
                this.updateState(nextEventEndChannel.getUID(), UnDefType.NULL);
            }

            // FIRSTLY: process all Command Tags in all Calendar Events which ENDED since updateStates was last called
            executeEventCommands(calendar.getJustEndedEvents(updateStatesLastCalledTime, now), CommandTagType.END);

            // SECONDLY: process all Command Tags in all Calendar Events which BEGAN since updateStates was last called
            executeEventCommands(calendar.getJustBegunEvents(updateStatesLastCalledTime, now), CommandTagType.BEGIN);

            // save time when updateStates was previously called:
            // note: the purpose is to prevent repeat command execution of events that have already been executed
            updateStatesLastCalledTime = now;
        }
    }

    private void executeEventCommands(@Nullable List<Event> events, CommandTagType execTime) {
        ICalendarConfiguration currentConfiguration = this.configuration;
        if ((events != null) && (!events.isEmpty()) && currentConfiguration != null) {
            EventPublisher currentEventPublisherCallback = this.eventPublisherCallback;
            if (currentEventPublisherCallback == null) {
                logger.error("EventPublisher object not instantiated!");
                return;
            }

            // loop through all events in the list
            for (Event event : events) {

                // loop through all command tags in the event
                for (CommandTag cmdTag : event.commandTags) {

                    // only process the BEGIN resp. END tags
                    if (cmdTag.tagType == execTime) {

                        if (!cmdTag.isAuthorized(currentConfiguration.authorizationCode)) {
                            logger.warn("Event: {}, Command Tag: {} => Not authorized!", event.title, cmdTag.fullTag);
                            continue;
                        }
                        String targetItem = cmdTag.itemName;
                        if (targetItem == null || (!targetItem.matches("^\\w+$"))) {
                            logger.warn("Event: {}, Command Tag: {} => Bad syntax for item name!", event.title,
                                    cmdTag.fullTag);
                            continue;
                        }
                        Command cmdState = cmdTag.getCommand();
                        if (cmdState == null) {
                            logger.warn("Event: {}, Command Tag: {} => Invalid target state!", event.title,
                                    cmdTag.fullTag);
                            continue;
                        }

                        // (try to) execute the command
                        try {
                            currentEventPublisherCallback
                                    .post(ItemEventFactory.createCommandEvent(cmdTag.itemName, cmdState));
                            if (logger.isDebugEnabled()) {
                                String cmdType = cmdState.getClass().toString();
                                int index = cmdType.lastIndexOf(".") + 1;
                                if ((index > 0) && (index < cmdType.length())) {
                                    cmdType = cmdType.substring(index);
                                }
                                logger.debug("Event: {}, Command Tag: {} => {}.postUpdate({}: {})", event.title,
                                        cmdTag.fullTag, cmdTag.itemName, cmdType, cmdState);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Event: {}, Command Tag: {} => Illegal Argument exception!", event.title,
                                    cmdTag.fullTag);
                        } catch (IllegalStateException e) {
                            logger.warn("Event: {}, Command Tag: {} => Illegal State exception!", event.title,
                                    cmdTag.fullTag);
                        }

                    }
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
        ScheduledFuture<?> currentUpdateJobFuture = this.updateJobFuture;
        if (currentUpdateJobFuture != null) {
            if (!(currentUpdateJobFuture.isCancelled() || currentUpdateJobFuture.isDone())) {
                currentUpdateJobFuture.cancel(true);
            }
            this.updateJobFuture = null;
        }
        AbstractPresentableCalendar currentCalendar = this.runtimeCalendar;
        if (currentCalendar != null) {
            Instant now = Instant.now();
            if (currentCalendar.isEventPresent(now)) {
                Event currentEvent = currentCalendar.getCurrentEvent(now);
                if (currentEvent == null) {
                    this.logger.warn(
                            "Could not schedule next update of states, due to unexpected behaviour of calendar implementation.");
                    return;
                }
                this.updateJobFuture = this.scheduler.schedule(() -> {
                    ICalendarHandler.this.updateStates();
                    ICalendarHandler.this.rescheduleCalendarStateUpdate();
                }, currentEvent.end.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
            } else {
                Event nextEvent = currentCalendar.getNextEvent(now);
                ICalendarConfiguration currentConfig = this.configuration;
                if (currentConfig == null) {
                    logger.warn("Something is broken, the configuration is not available.");
                    return;
                }
                if (nextEvent == null) {
                    this.updateJobFuture = this.scheduler.schedule(() -> {
                        ICalendarHandler.this.rescheduleCalendarStateUpdate();
                    }, currentConfig.readAroundTime.longValue(), TimeUnit.MINUTES);
                } else {
                    this.updateJobFuture = this.scheduler.schedule(() -> {
                        ICalendarHandler.this.updateStates();
                        ICalendarHandler.this.rescheduleCalendarStateUpdate();
                    }, nextEvent.start.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void onCalendarUpdated() {
        if (this.reloadCalendar()) {
            this.updateStates();
        } else {
            logger.warn("Calendar was updated, but loading failed.");
        }
    }

}
