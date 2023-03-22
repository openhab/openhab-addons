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
package org.openhab.binding.dwdunwetter.internal.handler;

import static org.openhab.binding.dwdunwetter.internal.DwdUnwetterBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dwdunwetter.internal.config.DwdUnwetterConfiguration;
import org.openhab.binding.dwdunwetter.internal.dto.DwdWarningsData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DwdUnwetterHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Martin Koehler - Initial contribution
 */
@NonNullByDefault
public class DwdUnwetterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DwdUnwetterHandler.class);

    private @Nullable ScheduledFuture<?> refreshJob;
    private int warningCount;
    private @Nullable DwdWarningsData data;

    private boolean inRefresh;

    public DwdUnwetterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.submit(this::refresh);
        }
    }

    /**
     * Refreshes the Warning Data.
     *
     * The Switch Channel is switched to ON only after all other Channels are updated.
     * The Switch Channel is switched to OFF before all other Channels are updated.
     */
    private void refresh() {
        if (inRefresh) {
            logger.trace("Already refreshing. Ignoring refresh request.");
            return;
        }

        if (!ThingHandlerHelper.isHandlerInitialized(getThing())) {
            logger.debug("Unable to refresh. Thing status is '{}'", getThing().getStatus());
            return;
        }

        final DwdWarningsData warningsData = data;
        if (warningsData == null) {
            logger.debug("Unable to refresh. No data to use.");
            return;
        }

        inRefresh = true;

        boolean refreshSucceeded = warningsData.refresh();
        if (!refreshSucceeded) {
            logger.debug("Failed to retrieve new data from the server.");
            inRefresh = false;
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        updateState(getChannelUuid(CHANNEL_LAST_UPDATED), new DateTimeType());

        for (int i = 0; i < warningCount; i++) {
            State warning = warningsData.getWarning(i);
            int warningNumber = i + 1;
            if (warning == OnOffType.OFF) {
                updateState(getChannelUuid(CHANNEL_WARNING, warningNumber), warning);
            }
            updateState(getChannelUuid(CHANNEL_SEVERITY, warningNumber), warningsData.getSeverity(i));
            updateState(getChannelUuid(CHANNEL_DESCRIPTION, warningNumber), warningsData.getDescription(i));
            updateState(getChannelUuid(CHANNEL_EFFECTIVE, warningNumber), warningsData.getEffective(i));
            updateState(getChannelUuid(CHANNEL_EXPIRES, warningNumber), warningsData.getExpires(i));
            updateState(getChannelUuid(CHANNEL_ONSET, warningNumber), warningsData.getOnset(i));
            updateState(getChannelUuid(CHANNEL_EVENT, warningNumber), warningsData.getEvent(i));
            updateState(getChannelUuid(CHANNEL_HEADLINE, warningNumber), warningsData.getHeadline(i));
            updateState(getChannelUuid(CHANNEL_ALTITUDE, warningNumber), warningsData.getAltitude(i));
            updateState(getChannelUuid(CHANNEL_CEILING, warningNumber), warningsData.getCeiling(i));
            updateState(getChannelUuid(CHANNEL_INSTRUCTION, warningNumber), warningsData.getInstruction(i));
            updateState(getChannelUuid(CHANNEL_URGENCY, warningNumber), warningsData.getUrgency(i));
            if (warning == OnOffType.ON) {
                updateState(getChannelUuid(CHANNEL_WARNING, warningNumber), warning);
            }
            if (warningsData.isNew(i)) {
                triggerChannel(getChannelUuid(CHANNEL_UPDATED, warningNumber), "NEW");
            }
        }

        warningsData.updateCache();
        inRefresh = false;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        DwdUnwetterConfiguration config = getConfigAs(DwdUnwetterConfiguration.class);
        int newWarningCount = config.warningCount;

        if (warningCount != newWarningCount) {
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (warningCount > newWarningCount) {
                for (int i = newWarningCount + 1; i <= warningCount; ++i) {
                    toBeRemovedChannels.addAll(removeChannels(i));
                }
            } else {
                for (int i = warningCount + 1; i <= newWarningCount; ++i) {
                    toBeAddedChannels.addAll(createChannels(i));
                }
            }
            warningCount = newWarningCount;

            ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
            for (Channel channel : toBeAddedChannels) {
                builder.withChannel(channel);
            }
            updateThing(builder.build());
        }

        data = new DwdWarningsData(config.cellId);

        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.MINUTES);

        logger.debug("Finished initializing!");
    }

    private ChannelUID getChannelUuid(String typeId, int warningNumber) {
        return new ChannelUID(getThing().getUID(), typeId + warningNumber);
    }

    private ChannelUID getChannelUuid(String typeId) {
        return new ChannelUID(getThing().getUID(), typeId);
    }

    /**
     * Creates a normal, state based, channel associated with a warning.
     */
    private void createChannelIfNotExist(ThingHandlerCallback cb, List<Channel> channels, String typeId, String label,
            int warningNumber) {
        ChannelUID channelUID = getChannelUuid(typeId, warningNumber);
        Channel existingChannel = getThing().getChannel(channelUID);
        if (existingChannel != null) {
            logger.trace("Thing '{}' already has an existing channel '{}'. Omit adding new channel '{}'.",
                    getThing().getUID(), existingChannel.getUID(), channelUID);
        } else {
            channels.add(cb.createChannelBuilder(channelUID, new ChannelTypeUID(BINDING_ID, typeId))
                    .withLabel(label + " " + getChannelLabelSuffix(warningNumber)).build());
        }
    }

    private String getChannelLabelSuffix(int warningNumber) {
        return "(" + warningNumber + ")";
    }

    /**
     * Creates the Channels for each warning.
     *
     * @return The List of Channels to be added
     */
    private List<Channel> createChannels(int warningNumber) {
        logger.debug("Building channels for thing '{}'.", getThing().getUID());
        List<Channel> channels = new ArrayList<>();
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            createChannelIfNotExist(callback, channels, CHANNEL_UPDATED, "Updated", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_WARNING, "Warning", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_SEVERITY, "Severity", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_DESCRIPTION, "Description", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_EFFECTIVE, "Issued", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_ONSET, "Valid From", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_EXPIRES, "Valid To", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_EVENT, "Type", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_HEADLINE, "Headline", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_ALTITUDE, "Height (from)", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_CEILING, "Height (to)", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_INSTRUCTION, "Instruction", warningNumber);
            createChannelIfNotExist(callback, channels, CHANNEL_URGENCY, "Urgency", warningNumber);
        }
        return channels;
    }

    /**
     * Filters the Channels for each warning
     *
     * @return The List of Channels to be removed
     */
    @SuppressWarnings("null")
    private List<Channel> removeChannels(int warningNumber) {
        return getThing().getChannels().stream()
                .filter(channel -> channel.getLabel() != null
                        && channel.getLabel().endsWith(getChannelLabelSuffix(warningNumber)))
                .collect(Collectors.toList());
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
