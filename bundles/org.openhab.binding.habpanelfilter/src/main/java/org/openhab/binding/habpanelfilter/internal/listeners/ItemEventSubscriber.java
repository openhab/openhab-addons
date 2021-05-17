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
package org.openhab.binding.habpanelfilter.internal.listeners;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.habpanelfilter.internal.web.FilteredEventsResource;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Cuchriajev
 *
 */
@Component
@NonNullByDefault
public class ItemEventSubscriber implements EventSubscriber {

    private final Set<String> subscribedEventTypes = Collections.singleton(EventSubscriber.ALL_EVENT_TYPES);

    private final FilteredEventsResource filteredEventsResource;

    @Activate
    public ItemEventSubscriber(final @Reference FilteredEventsResource filteredEventsResource) {
        this.filteredEventsResource = filteredEventsResource;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        filteredEventsResource.broadcastEvent(event);
    }
}
