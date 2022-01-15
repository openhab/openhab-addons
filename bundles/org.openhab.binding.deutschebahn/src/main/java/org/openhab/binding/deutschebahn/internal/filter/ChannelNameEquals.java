/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.filter;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.AttributeSelection;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.EventAttributeSelection;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.TripLabelAttribute;

/**
 * Token representing an attribute filter.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public final class ChannelNameEquals extends FilterToken {

    private final String channelName;
    private final Pattern filterValue;
    private String channelGroup;

    /**
     * Creates an new {@link ChannelNameEquals}.
     */
    public ChannelNameEquals(int position, String channelGroup, String channelName, Pattern filterPattern) {
        super(position);
        this.channelGroup = channelGroup;
        this.channelName = channelName;
        this.filterValue = filterPattern;
    }

    /**
     * Returns the channel group.
     */
    public String getChannelGroup() {
        return channelGroup;
    }

    /**
     * Returns the channel name.
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Returns the filter value.
     */
    public Pattern getFilterValue() {
        return filterValue;
    }

    @Override
    public String toString() {
        return this.channelGroup + "#" + channelName + "=\"" + this.filterValue.toString() + "\"";
    }

    @Override
    public <R> R accept(FilterTokenVisitor<R> visitor) throws FilterParserException {
        return visitor.handle(this);
    }

    /**
     * Maps this into an {@link TimetableStopByStringEventAttributeFilter}.
     */
    public TimetableStopByStringEventAttributeFilter mapToPredicate() throws FilterParserException {
        return new TimetableStopByStringEventAttributeFilter(mapAttributeSelection(), filterValue);
    }

    private AttributeSelection mapAttributeSelection() throws FilterParserException {
        switch (this.channelGroup) {
            case "trip":
                final TripLabelAttribute<?, ?> tripAttribute = TripLabelAttribute.getByChannelName(this.channelName);
                if (tripAttribute == null) {
                    throw new FilterParserException("Invalid trip channel: " + channelName);
                }
                return tripAttribute;

            case "departure":
                final EventType eventTypeDeparture = EventType.DEPARTURE;
                final EventAttribute<?, ?> departureAttribute = EventAttribute.getByChannelName(this.channelName,
                        eventTypeDeparture);
                if (departureAttribute == null) {
                    throw new FilterParserException("Invalid departure channel: " + channelName);
                }
                return new EventAttributeSelection(eventTypeDeparture, departureAttribute);

            case "arrival":
                final EventType eventTypeArrival = EventType.ARRIVAL;
                final EventAttribute<?, ?> arrivalAttribute = EventAttribute.getByChannelName(this.channelName,
                        eventTypeArrival);
                if (arrivalAttribute == null) {
                    throw new FilterParserException("Invalid arrival channel: " + channelName);
                }
                return new EventAttributeSelection(eventTypeArrival, arrivalAttribute);
            default:
                throw new FilterParserException("Unknown channel group: " + channelGroup);
        }
    }
}
