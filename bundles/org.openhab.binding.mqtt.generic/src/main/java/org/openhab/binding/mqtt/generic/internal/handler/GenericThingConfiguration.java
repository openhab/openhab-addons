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
package org.openhab.binding.mqtt.generic.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link GenericMQTTThingHandler} manages Things that are responsible for MQTT components.
 * This class contains the necessary configuration for such a Thing handler.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class GenericThingConfiguration {
    /**
     * topic for the availability channel
     */
    public @Nullable String availabilityTopic;

    /**
     * payload for the availability topic when the device is available.
     */
    public String payloadAvailable = OnOffType.ON.toString();

    /**
     * payload for the availability topic when the device is *not* available.
     */
    public String payloadNotAvailable = OnOffType.OFF.toString();

    /**
     * transformation pattern for the availability payload
     */
    public List<String> transformationPattern = List.of();
}
