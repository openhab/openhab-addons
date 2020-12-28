/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.omatic.internal.event;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.events.ItemStateEvent;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OMaticEventSubscriber}
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@Component(service = { EventSubscriber.class,
        OMaticEventSubscriber.class }, configurationPid = "binding.omatic.eventsubscriber")
@NonNullByDefault
public class OMaticEventSubscriber implements EventSubscriber {

    public static final String PROPERTY_ITEM_EVENT = "PROPERTY_ITEM_EVENT";

    private final Logger logger = LoggerFactory.getLogger(OMaticEventSubscriber.class);

    private final Set<String> subscribedEventTypes = new HashSet<String>();

    private final Set<String> omaticMonitoringItems = new HashSet<>();

    private final PropertyChangeSupport propertyChangeSupport;

    public OMaticEventSubscriber() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        subscribedEventTypes.add(ItemStateEvent.TYPE);
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    public void addItemItemName(String name) {
        omaticMonitoringItems.add("openhab/items/" + name + "/state");
    }

    @Override
    public void receive(Event event) {
        if (!omaticMonitoringItems.contains(event.getTopic())) {
            return;
        }
        propertyChangeSupport.firePropertyChange(PROPERTY_ITEM_EVENT, null, event);
        logger.debug("Got Event event: topic {} payload: {}", event.getTopic(), event.getPayload());
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        logger.debug("Adding listener for EventSubscriber");
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }
}
