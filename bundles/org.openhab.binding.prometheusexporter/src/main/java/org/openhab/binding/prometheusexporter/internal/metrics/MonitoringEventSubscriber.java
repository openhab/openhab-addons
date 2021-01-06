/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.prometheusexporter.internal.metrics;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

/**
 * The {@link MonitoringEventSubscriber} class subscribes to the event bus in order to feed corresponding metrics
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
@Component(service = { MonitoringEventSubscriber.class, EventSubscriber.class })
public class MonitoringEventSubscriber implements EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(MonitoringEventSubscriber.class);
    @Nullable
    private Counter eventCounter = null;

    @Override
    public Set<String> getSubscribedEventTypes() {
        HashSet<String> subscribedEvents = new HashSet<>();
        subscribedEvents.add(ItemCommandEvent.TYPE);
        subscribedEvents.add(ItemStateEvent.TYPE);
        return subscribedEvents;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (eventCounter == null) {
            logger.trace("Measurement not started. Skipping event processing");
            return;
        }
        String topic = event.getTopic();
        logger.debug("Received event on topic {}.", topic);
        eventCounter.labels(topic).inc();
    }

    public synchronized void startMeasurement() {
        if (eventCounter == null) {
            eventCounter = Counter.build(EventCountMetric.METRIC_NAME, "openHAB event count").labelNames("topic")
                    .create().register();
        }
    }

    public synchronized void stopMeasurement() {
        if (eventCounter != null) {
            CollectorRegistry.defaultRegistry.unregister(eventCounter);
            eventCounter = null;
        }
    }
}
