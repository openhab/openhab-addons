/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerWorkAreaHandler} represents a WorkArea of an automower as thing.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class AutomowerWorkAreaHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WORKAREA);

    private final Logger logger = LoggerFactory.getLogger(AutomowerWorkAreaHandler.class);
    private final String thingId;

    public AutomowerWorkAreaHandler(Thing thing) {
        super(thing);
        this.thingId = this.getThing().getUID().getId();
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler == null) {
            logger.warn("No AutomowerBridgeHandler found for thingId {}", this.thingId);
            return;
        }
        // Split thingId at the last "-" to get mowerId and areaId
        int lastDash = this.thingId.lastIndexOf("-");
        if (lastDash == -1) {
            logger.warn("Invalid thingId format: {}", this.thingId);
            return;
        }
        String mowerId = this.thingId.substring(0, lastDash);
        String areaId = this.thingId.substring(lastDash + 1);
        logger.trace("Handling command {} for channel {} of mowerId {} and areaId {}", command, channelUID, mowerId,
                areaId);
        AutomowerHandler mowerHandler = automowerBridgeHandler.getAutomowerHandlerByMowerId(mowerId);
        if (mowerHandler == null) {
            logger.warn("No AutomowerHandler found for mowerId {}", mowerId);
            return;
        }
        String groupId = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        if (groupId == null) {
            logger.warn("Invalid channelUID format: {}", channelUID);
            return;
        }

        /* all pre-conditions met ... */
        if (RefreshType.REFRESH == command) {
            mowerHandler.updateAutomowerState(); // refresh current state from cache
        } else if (GROUP_CALENDARTASK.startsWith(groupId)) {
            String[] channelIDSplit = channelId.split("-", 2);
            int index = Integer.parseInt(channelIDSplit[0]) - 1;
            String param = channelIDSplit[1];
            mowerHandler.sendAutomowerCalendarTask(command, index, areaId, param);
        } else if (GROUP_WORKAREA.startsWith(groupId)) {
            if (CHANNEL_WORKAREA_ENABLED.equals(channelUID.getId())) {
                if (command instanceof OnOffType cmd) {
                    mowerHandler.sendAutomowerWorkAreaEnable(areaId, cmd == OnOffType.ON);
                } else {
                    logger.warn("Command {} not supported for channel {}", command, channelUID);
                }
            } else if (CHANNEL_WORKAREA_CUTTING_HEIGHT.equals(channelUID.getId())) {
                if (command instanceof QuantityType cmd) {
                    cmd = cmd.toUnit("%");
                    if (cmd != null) {
                        mowerHandler.sendAutomowerWorkAreaCuttingHeight(areaId, cmd.byteValue());
                    } else {
                        logger.warn("Command {} not supported for channel {}", command, channelUID);
                    }
                } else if (command instanceof DecimalType cmd) {
                    mowerHandler.sendAutomowerWorkAreaCuttingHeight(areaId, cmd.byteValue());
                } else {
                    logger.warn("Command {} not supported for channel {}", command, channelUID);
                }
            } else {
                logger.warn("Command {} not supported for channel {}", command, channelUID);
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN); // Set to UNKNOWN initially

        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            // Adding handler to map of handlers
            automowerBridgeHandler.registerAutomowerWorkAreaHandler(this.thingId, this);

            scheduler.execute(() -> completeInitAsync());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
        }
    }

    private void completeInitAsync() {
        // Initial poll to create and set the status of the channels
        poll();
        logger.trace("AutomowerWorkAreaHandler initialized for thingId {}", this.thingId);
    }

    @Nullable
    private AutomowerBridge getAutomowerBridge() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler.getAutomowerBridge();
            }
        }
        return null;
    }

    @Nullable
    private AutomowerBridgeHandler getAutomowerBridgeHandler() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            automowerBridgeHandler.unregisterAutomowerWorkAreaHandler(this.thingId);
        }
    }

    public void poll() {
        AutomowerBridge automowerBridge = getAutomowerBridge();
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null && automowerBridge != null) {
            automowerBridgeHandler.pollAutomowers(automowerBridge);
        }
    }

    private synchronized void addRemoveDynamicChannels(List<CalendarTask> calendarTasks,
            AutomowerHandler mowerHandler) {
        // make sure that static channels are present
        List<Channel> channelAdd = new ArrayList<>();

        for (String channelID : WORKAREA_STATIC_CHANNEL_IDS) {
            Channel channel = thing.getChannel(channelID);
            if (channel == null) {
                logger.warn("Static Channel '{}' is not present: remove and re-add Thing", channelID);
            } else {
                channelAdd.add(channel);
            }
        }

        int i;
        for (i = 0; i < calendarTasks.size(); i++) {
            int j = 0;
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), "Number:Time", channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), "Number:Time", channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            AutomowerHandler.createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                    CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
        }

        // remove channels that are now longer required and add new once
        updateThing(editThing().withChannels(channelAdd).build());
    }

    public void updateChannels(WorkArea workArea, List<CalendarTask> calendarTasks, AutomowerHandler mowerHandler) {
        updateStatus(ThingStatus.ONLINE);

        updateWorkAreaChannels(workArea, mowerHandler);

        // only show the Tasks of the current WorkArea
        List<CalendarTask> calendarTasksFiltered = new ArrayList<>();
        long workAreaId = workArea.getWorkAreaId();
        for (CalendarTask calendarTask : calendarTasks) {
            if (calendarTask.getWorkAreaId().equals(workAreaId)) {
                calendarTasksFiltered.add(calendarTask);
            }
        }

        addRemoveDynamicChannels(calendarTasksFiltered, mowerHandler);
        updateCalendarTaskChannels(calendarTasksFiltered);
    }

    private void updateWorkAreaChannels(WorkArea workArea, AutomowerHandler mowerHandler) {
        if (workArea.getWorkAreaId() == 0L && workArea.getName().isBlank()) {
            updateState(CHANNEL_WORKAREA_NAME, new StringType("main area"));
        } else {
            updateState(CHANNEL_WORKAREA_NAME, new StringType(workArea.getName()));
        }
        updateState(CHANNEL_WORKAREA_CUTTING_HEIGHT, new QuantityType<>(workArea.getCuttingHeight(), Units.PERCENT));
        updateState(CHANNEL_WORKAREA_ENABLED, OnOffType.from(workArea.isEnabled()));
        if (workArea.getProgress() != null) {
            updateState(CHANNEL_WORKAREA_PROGRESS, new QuantityType<>(workArea.getProgress(), Units.PERCENT));
        } else {
            updateState(CHANNEL_WORKAREA_PROGRESS, UnDefType.NULL);
        }

        // lastTimeCompleted is in seconds, convert it to milliseconds
        Long lastTimeCompleted = workArea.getLastTimeCompleted();
        // If lastTimeCompleted is 0 it means the work area has never been completed
        if (lastTimeCompleted != null && lastTimeCompleted != 0L) {
            updateState(CHANNEL_WORKAREA_LAST_TIME_COMPLETED, new DateTimeType(mowerHandler
                    .toZonedDateTime(TimeUnit.SECONDS.toMillis(lastTimeCompleted), mowerHandler.getMowerZoneId())));
        } else {
            updateState(CHANNEL_WORKAREA_LAST_TIME_COMPLETED, UnDefType.NULL);
        }
    }

    private void updateCalendarTaskChannels(List<CalendarTask> calendarTasks) {
        int i = 0;
        for (; i < calendarTasks.size(); i++) {
            int j = 0;
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    new QuantityType<>(calendarTasks.get(i).getStart(), Units.MINUTE));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    new QuantityType<>(calendarTasks.get(i).getDuration(), Units.MINUTE));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getMonday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getTuesday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getWednesday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getThursday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getFriday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getSaturday()));
            updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                    OnOffType.from(calendarTasks.get(i).getSunday()));
        }
    }

    private void updateIndexedState(String ChannelGroup, int id, String channel, org.openhab.core.types.State state) {
        String indexedChannel = AutomowerHandler.getIndexedChannel(ChannelGroup, id, channel);
        updateState(indexedChannel, state);
    }
}
