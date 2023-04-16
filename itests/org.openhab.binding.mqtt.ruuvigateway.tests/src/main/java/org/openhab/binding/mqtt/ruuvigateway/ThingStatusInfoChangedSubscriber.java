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
package org.openhab.binding.mqtt.ruuvigateway;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test utility capturing thing status updates
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ThingStatusInfoChangedSubscriber implements EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(ThingStatusInfoChangedSubscriber.class);

    public Map<ThingUID, List<ThingStatusInfo>> statusUpdates = new HashMap<>();

    @Override
    public Set<@NonNull String> getSubscribedEventTypes() {
        return Collections.singleton(ThingStatusInfoChangedEvent.TYPE);
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        // Expecting only state updates in the tests
        assertInstanceOf(ThingStatusInfoChangedEvent.class, event);
        ThingStatusInfoChangedEvent statusEvent = (ThingStatusInfoChangedEvent) event;
        logger.trace("Captured event: {} ", event);
        List<ThingStatusInfo> updates = statusUpdates.computeIfAbsent(statusEvent.getThingUID(),
                item -> new CopyOnWriteArrayList<>());
        Objects.requireNonNull(updates); // To make compiler happy
        updates.add(statusEvent.getStatusInfo());
    }
}
