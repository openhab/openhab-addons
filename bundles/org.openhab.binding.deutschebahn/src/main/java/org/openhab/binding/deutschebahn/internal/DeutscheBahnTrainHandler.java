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
package org.openhab.binding.deutschebahn.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a Train-Thing in DeutscheBahn Binding.
 *
 * Represents a Train that arrives / departs at the station selected by the DeutscheBahnTimetable-Bridge.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class DeutscheBahnTrainHandler extends BaseThingHandler {

    /**
     * Wraps the Channel-UID with the configured {@link AttributeSelection}.
     */
    private final class ChannelWithConfig {

        private final ChannelUID channelUid;
        private final AttributeSelection attributeSelection;

        /**
         * Creates a new ChannelWithConfig.
         * 
         * @param channelUid The UID of the channel
         * @param attributeSelection The attribute that provides the state that will be displayed.
         */
        public ChannelWithConfig( //
                final ChannelUID channelUid, //
                final AttributeSelection attributeSelection) {
            this.channelUid = channelUid;
            this.attributeSelection = attributeSelection;
        }

        /**
         * Updates the value for the channel from given {@link TimetableStop}.
         */
        public void updateChannelValue(final TimetableStop stop) {
            final State newState = this.determineState(stop);
            if (newState != null) {
                DeutscheBahnTrainHandler.this.updateState(this.channelUid, newState);
            } else {
                DeutscheBahnTrainHandler.this.updateState(this.channelUid, UnDefType.NULL);
            }
        }

        @Nullable
        private State determineState(final TimetableStop stop) {
            return this.attributeSelection.getState(stop);
        }

        @Override
        public String toString() {
            return this.channelUid.toString();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(DeutscheBahnTrainHandler.class);
    private final List<ChannelWithConfig> configuredChannels = new ArrayList<>();

    /**
     * Creates a new {@link DeutscheBahnTrainHandler}.
     */
    public DeutscheBahnTrainHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.updateStatus(ThingStatus.UNKNOWN);

        if (this.getBridge() == null) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Please select bridge");
            return;
        }

        this.createChannelMapping();
        this.updateStatus(ThingStatus.ONLINE);
    }

    private void createChannelMapping() {
        this.configuredChannels.clear();
        for (Channel channel : this.getThing().getChannelsOfGroup("trip")) {
            this.createTripChannelConfiguration(channel);
        }
        for (Channel channel : this.getThing().getChannelsOfGroup("arrival")) {
            this.createEventChannelConfiguration(EventType.ARRIVAL, channel);
        }
        for (Channel channel : this.getThing().getChannelsOfGroup("departure")) {
            this.createEventChannelConfiguration(EventType.DEPARTURE, channel);
        }
        this.logger.debug("Created {} configured channels for thing {}.", this.configuredChannels.size(),
                this.getThing().getUID());
    }

    /**
     * Creates a {@link ChannelWithConfig} for a channel that represents an attribute of an
     * {@link org.openhab.binding.deutschebahn.internal.timetable.dto.TripLabel}.
     */
    private void createTripChannelConfiguration(Channel channel) {
        final ChannelUID channelUid = channel.getUID();
        final String attributeName = getAttributeName(channelUid);
        final TripLabelAttribute<?, ?> attribute = TripLabelAttribute.getByChannelName(attributeName);
        if (attribute == null) {
            this.logger.warn("Could not find trip attribute {} of channel: {} .", attribute, channelUid.getId());
            return;
        }
        final ChannelWithConfig channelWithConfig = new ChannelWithConfig( //
                channelUid, //
                attribute);
        this.configuredChannels.add(channelWithConfig);
    }

    /**
     * Creates the {@link ChannelWithConfig} for a channel that represents an attribute of an
     * {@link org.openhab.binding.deutschebahn.internal.timetable.dto.Event}.}
     */
    private void createEventChannelConfiguration(EventType eventType, Channel channel) {
        final ChannelUID channelUid = channel.getUID();
        final String attributeName = getAttributeName(channelUid);
        final EventAttribute<?, ?> attribute = EventAttribute.getByChannelName(attributeName, eventType);
        if (attribute == null) {
            this.logger.warn("Could not find event attribute {} of channel: {} .", attribute, channelUid.getId());
            return;
        }
        final ChannelWithConfig channelWithConfig = new ChannelWithConfig( //
                channelUid, //
                new EventAttributeSelection(eventType, attribute));
        this.configuredChannels.add(channelWithConfig);
    }

    /**
     * Strips the attribute name from the channel-UID.
     */
    private static String getAttributeName(ChannelUID channelUid) {
        final String channelId = channelUid.getId();
        int hashIndex = channelId.indexOf("#");
        assert hashIndex > 0;
        return channelId.substring(hashIndex + 1);
    }

    /**
     * Does not handle any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Updates the value for the channels of this train from the given {@link TimetableStop}.
     */
    void updateChannels(TimetableStop stop) {
        for (ChannelWithConfig channel : this.configuredChannels) {
            channel.updateChannelValue(stop);
        }
    }
}
