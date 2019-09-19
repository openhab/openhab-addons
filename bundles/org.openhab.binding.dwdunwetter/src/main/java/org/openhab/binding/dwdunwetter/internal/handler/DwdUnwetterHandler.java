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
package org.openhab.binding.dwdunwetter.internal.handler;

import static org.openhab.binding.dwdunwetter.internal.DwdUnwetterBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.dwdunwetter.internal.config.DwdUnwetterConfiguration;
import org.openhab.binding.dwdunwetter.internal.data.DwdWarningsData;
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
     * <p>
     * The Switch Channel is switched to ON only after all other Channels are updated.
     * The Switch Channel is switched to OFF before all other Channels are updated.
     */
    private void refresh() {
        if (inRefresh
                || (getThing().getStatus() != ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.UNKNOWN)) {
            return;
        }
        final DwdWarningsData warningsData = this.data;
        inRefresh = true;
        if (warningsData == null || !warningsData.refresh()) {
            return;
        }
        if (getThing().getStatus() == ThingStatus.UNKNOWN) {
            updateStatus(ThingStatus.ONLINE);
        }

        updateState(getChannelUuid(CHANNEL_LAST_UPDATED), new DateTimeType());

        for (int i = 0; i < warningCount; i++) {
            State warning = warningsData.getWarning(i);
            if (warning == OnOffType.OFF) {
                updateState(getChannelUuid(CHANNEL_WARNING, i), warning);
            }
            updateState(getChannelUuid(CHANNEL_SEVERITY, i), warningsData.getSeverity(i));
            updateState(getChannelUuid(CHANNEL_DESCRIPTION, i), warningsData.getDescription(i));
            updateState(getChannelUuid(CHANNEL_EFFECTIVE, i), warningsData.getEffective(i));
            updateState(getChannelUuid(CHANNEL_EXPIRES, i), warningsData.getExpires(i));
            updateState(getChannelUuid(CHANNEL_ONSET, i), warningsData.getOnset(i));
            updateState(getChannelUuid(CHANNEL_EVENT, i), warningsData.getEvent(i));
            updateState(getChannelUuid(CHANNEL_HEADLINE, i), warningsData.getHeadline(i));
            updateState(getChannelUuid(CHANNEL_ALTITUDE, i), warningsData.getAltitude(i));
            updateState(getChannelUuid(CHANNEL_CEILING, i), warningsData.getCeiling(i));
            updateState(getChannelUuid(CHANNEL_INSTRUCTION, i), warningsData.getInstruction(i));
            updateState(getChannelUuid(CHANNEL_URGENCY, i), warningsData.getUrgency(i));
            if (warning == OnOffType.ON) {
                updateState(getChannelUuid(CHANNEL_WARNING, i), warning);
            }
            if (warningsData.isNew(i)) {
                triggerChannel(getChannelUuid(CHANNEL_UPDATED, i), "NEW");
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
        warningCount = config.warningCount;

        data = new DwdWarningsData(config.cellId);

        updateThing(editThing().withChannels(createChannels()).build());

        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.MINUTES);
        logger.debug("Finished initializing!");
    }

    private ChannelUID getChannelUuid(String typeId, int warningNumber) {
        return new ChannelUID(getThing().getUID(), typeId + (warningNumber + 1));
    }

    private ChannelUID getChannelUuid(String typeId) {
        return new ChannelUID(getThing().getUID(), typeId);
    }

    /**
     * Creates a trigger Channel.
     */
    private Channel createTriggerChannel(String typeId, String label, int warningNumber) {
        ChannelUID channelUID = getChannelUuid(typeId, warningNumber);
        return ChannelBuilder.create(channelUID, "String") //
                .withType(new ChannelTypeUID(BINDING_ID, typeId)) //
                .withLabel(label + " (" + (warningNumber + 1) + ")")//
                .withKind(ChannelKind.TRIGGER) //
                .build();
    }

    /**
     * Creates a normal, state based, channel associated with a warning.
     */
    private Channel createChannel(String typeId, String itemType, String label, int warningNumber) {
        ChannelUID channelUID = getChannelUuid(typeId, warningNumber);
        return ChannelBuilder.create(channelUID, itemType) //
                .withType(new ChannelTypeUID(BINDING_ID, typeId)) //
                .withLabel(label + " (" + (warningNumber + 1) + ")")//
                .build();
    }

    /**
     * Creates a normal, state based, channel not associated with a warning.
     */
    private Channel createChannel(String typeId, String itemType, String label) {
        ChannelUID channelUID = getChannelUuid(typeId);
        return ChannelBuilder.create(channelUID, itemType) //
                .withType(new ChannelTypeUID(BINDING_ID, typeId)) //
                .withLabel(label)//
                .build();
    }

    /**
     * Creates the ChannelsT for each warning.
     *
     * @return The List of Channels
     */
    private List<Channel> createChannels() {
        List<Channel> channels = new ArrayList<>(warningCount * 11 + 1);
        channels.add(createChannel(CHANNEL_LAST_UPDATED, "DateTime", "Last Updated"));
        for (int i = 0; i < warningCount; i++) {
            channels.add(createChannel(CHANNEL_WARNING, "Switch", "Warning", i));
            channels.add(createTriggerChannel(CHANNEL_UPDATED, "Updated", i));
            channels.add(createChannel(CHANNEL_SEVERITY, "String", "Severity", i));
            channels.add(createChannel(CHANNEL_DESCRIPTION, "String", "Description", i));
            channels.add(createChannel(CHANNEL_EFFECTIVE, "DateTime", "Issued", i));
            channels.add(createChannel(CHANNEL_ONSET, "DateTime", "Valid from", i));
            channels.add(createChannel(CHANNEL_EXPIRES, "DateTime", "Valid to", i));
            channels.add(createChannel(CHANNEL_EVENT, "String", "Type", i));
            channels.add(createChannel(CHANNEL_HEADLINE, "String", "Headline", i));
            channels.add(createChannel(CHANNEL_ALTITUDE, "Number:Length", "Height (from)", i));
            channels.add(createChannel(CHANNEL_CEILING, "Number:Length", "Height (to)", i));
            channels.add(createChannel(CHANNEL_INSTRUCTION, "String", "Instruction", i));
            channels.add(createChannel(CHANNEL_URGENCY, "String", "Urgency", i));
        }
        return channels;
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }
}
