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
package org.openhab.binding.mqtt.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.binding.generic.ChannelTransformation;

/**
 * Interface to keep track of the availability of device using an availability topic or messages received
 *
 * @author Jochen Klein - Initial contribution
 * @author Cody Cutrer - Support all/any/latest
 */
@NonNullByDefault
public interface AvailabilityTracker {
    /**
     * controls the conditions needed to set the entity to available
     */
    enum AvailabilityMode {
        /**
         * payload_available must be received on all configured availability topics before the entity is marked as
         * online
         */
        ALL,

        /**
         * payload_available must be received on at least one configured availability topic before the entity is marked
         * as online
         */
        ANY,

        /**
         * the last payload_available or payload_not_available received on any configured availability topic controls
         * the availability
         */
        LATEST
    }

    /**
     * Sets how multiple availability topics are treated
     */
    void setAvailabilityMode(AvailabilityMode mode);

    /**
     * Adds an availability topic to determine the availability of a device.
     * <p>
     * Availability topics are usually set by the device as LWT.
     *
     * @param availability_topic
     * @param payload_available
     * @param payload_not_available
     */
    void addAvailabilityTopic(String availability_topic, String payload_available, String payload_not_available);

    /**
     * Adds an availability topic to determine the availability of a device.
     * <p>
     * Availability topics are usually set by the device as LWT.
     *
     * @param availability_topic The MQTT topic where availability is published to.
     * @param payload_available The value for the topic to indicate the device is online.
     * @param payload_not_available The value for the topic to indicate the device is offline.
     * @param transformation A transformation to process the value before comparing to
     *            payload_available/payload_not_available.
     */
    void addAvailabilityTopic(String availability_topic, String payload_available, String payload_not_available,
            @Nullable ChannelTransformation transformation);

    void removeAvailabilityTopic(String availability_topic);

    void clearAllAvailabilityTopics();

    /**
     * resets the indicator, if messages have been received.
     * <p>
     * This is used to time out the availability of the device after some time without receiving a message.
     */
    void resetMessageReceived();
}
