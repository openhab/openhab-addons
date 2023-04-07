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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icalendar.internal.config.TagExecutorConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.CommandTag;
import org.openhab.binding.icalendar.internal.logic.CommandTagType;
import org.openhab.binding.icalendar.internal.logic.Event;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TagExecutorHandler} shows the current and next event from a calendar.
 *
 * @author Michael Wodniok - Initial Contribution, mostly a copy of the bridge {@link ICalendarHandler}.
 */
@NonNullByDefault
public class TagExecutorHandler extends BaseThingHandler implements CalendarUpdateListener {

    private final EventPublisher eventPublisherCallback;
    private @Nullable TagExecutorConfiguration configuration;
    private final Logger logger = LoggerFactory.getLogger(TagExecutorHandler.class);
    private @Nullable ScheduledFuture<?> updateFuture;
    private @Nullable AbstractPresentableCalendar calendar;
    private @Nullable Instant lastUpdate;

    public TagExecutorHandler(Thing thing, EventPublisher eventPublisher) {
        super(thing);
        this.eventPublisherCallback = eventPublisher;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            executeTags();
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> currentUpdateFuture = updateFuture;
        if (currentUpdateFuture != null) {
            currentUpdateFuture.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            executeTags();
        }
    }

    @Override
    public void initialize() {
        Bridge iCalendarBridge = getBridge();
        if (iCalendarBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This thing requires a bridge configured to work.");
            return;
        }

        final TagExecutorConfiguration config = getConfigAs(TagExecutorConfiguration.class);
        configuration = config;

        if (iCalendarBridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        lastUpdate = null;
        updateStatus(ThingStatus.UNKNOWN);
        retrieveCalendar();
        executeTags();
        rescheduleTagExecution();
    }

    @Override
    public void onCalendarUpdated() {
        retrieveCalendar();
        executeTags();
        rescheduleTagExecution();
    }

    private void retrieveCalendar() {
        if (!ThingHandlerHelper.isHandlerInitialized(this)) {
            logger.debug("Ignoring call for updating states as this instance is not initialized yet.");
            return;
        }
        final Bridge iCalendarBridge = getBridge();
        if (iCalendarBridge == null) {
            logger.debug("Bridge not instantiated!");
            return;
        }
        final ICalendarHandler iCalendarHandler = (ICalendarHandler) iCalendarBridge.getHandler();
        if (iCalendarHandler == null) {
            logger.debug("ICalendarHandler not instantiated!");
            return;
        }
        this.calendar = iCalendarHandler.getRuntimeCalendar();
    }

    /**
     * Updates all states and channels. Reschedules an update if no error occurs.
     */
    private void executeTags() {
        if (!ThingHandlerHelper.isHandlerInitialized(this)) {
            logger.debug("Ignoring call for updating states as this instance is not initialized yet.");
            return;
        }
        AbstractPresentableCalendar cal = this.calendar;
        if (cal != null) {
            updateStatus(ThingStatus.ONLINE);

            Instant reference = Instant.now();

            Instant lastUpdate = this.lastUpdate;
            if (lastUpdate != null) {

                // process all Command Tags in all Calendar Events which ENDED since updateStates was last called
                // the END Event tags must be processed before the BEGIN ones
                executeEventCommands(cal.getJustEndedEvents(lastUpdate, reference), CommandTagType.END);

                // process all Command Tags in all Calendar Events which BEGAN since updateStates was last called
                // the END Event tags must be processed before the BEGIN ones
                executeEventCommands(cal.getJustBegunEvents(lastUpdate, reference), CommandTagType.BEGIN);
            }

            this.lastUpdate = reference;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Calendar has not been retrieved yet.");
        }
    }

    /**
     * Reschedules the next update of the states.
     */
    private void rescheduleTagExecution() {
        final ScheduledFuture<?> currentUpdateJobFuture = updateFuture;
        if (currentUpdateJobFuture != null) {
            if (!(currentUpdateJobFuture.isCancelled() || currentUpdateJobFuture.isDone())) {
                currentUpdateJobFuture.cancel(true);
            }
            updateFuture = null;
        }
        final AbstractPresentableCalendar currentCalendar = calendar;
        if (currentCalendar == null) {
            return;
        }
        final TagExecutorConfiguration config = configuration;
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Something is broken, the configuration is not available.");
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
        if (nextEvent != null) {
            if (nextRegularUpdate == null || nextEvent.start.isBefore(nextRegularUpdate)) {
                nextRegularUpdate = nextEvent.start;
            }
        }

        if (nextRegularUpdate != null) {
            updateFuture = scheduler.schedule(() -> {
                TagExecutorHandler.this.executeTags();
                TagExecutorHandler.this.rescheduleTagExecution();
            }, nextRegularUpdate.getEpochSecond() - now.getEpochSecond(), TimeUnit.SECONDS);
            logger.debug("Scheduled update in {} seconds", nextRegularUpdate.getEpochSecond() - now.getEpochSecond());
        } else {
            logger.debug("No");
        }
    }

    private void executeEventCommands(List<Event> events, CommandTagType execTime) {
        // no begun or ended events => exit quietly as there is nothing to do
        if (events.isEmpty()) {
            return;
        }

        final TagExecutorConfiguration syncConfiguration = configuration;
        if (syncConfiguration == null) {
            logger.debug("Configuration not instantiated!");
            return;
        }
        // loop through all events in the list
        for (Event event : events) {

            // loop through all command tags in the event
            for (CommandTag cmdTag : event.extractTags()) {

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
}
