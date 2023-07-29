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
package org.openhab.binding.mqtt.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface to keep track of the availability of device using an availability topic or messages received
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public interface AvailabilityTracker {

    /**
     * Adds an availability topic to determine the availability of a device.
     * <p>
     * Availability topics are usually set by the device as LWT.
     *
     * @param availability_topic
     * @param payload_available
     * @param payload_not_available
     */
    public void addAvailabilityTopic(String availability_topic, String payload_available, String payload_not_available);

    /**
     * Adds an availability topic to determine the availability of a device.
     * <p>
     * Availability topics are usually set by the device as LWT.
     *
     * @param availability_topic The MQTT topic where availability is published to.
     * @param payload_available The value for the topic to indicate the device is online.
     * @param payload_not_available The value for the topic to indicate the device is offline.
     * @param transformation_pattern A transformation pattern to process the value before comparing to
     *            payload_available/payload_not_available.
     * @param transformationServiceProvider The service provider to obtain the transformation service (required only if
     *            transformation_pattern is not null).
     */
    public void addAvailabilityTopic(String availability_topic, String payload_available, String payload_not_available,
            @Nullable String transformation_pattern,
            @Nullable TransformationServiceProvider transformationServiceProvider);

    public void removeAvailabilityTopic(String availability_topic);

    public void clearAllAvailabilityTopics();

    /**
     * resets the indicator, if messages have been received.
     * <p>
     * This is used to time out the availability of the device after some time without receiving a message.
     */
    public void resetMessageReceived();
}
