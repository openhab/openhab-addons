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
package org.openhab.binding.digitalstrom.internal.lib.event.types;

import java.util.List;

/**
 * The {@link Event} represents a digitalSTROM-Event.
 *
 * @author Alexander Betker - Initial contribution
 */
public interface Event {

    /**
     * Returns a list of the {@link EventItem}s of this Event.
     *
     * @return List of {@link EventItem}s
     */
    List<EventItem> getEventItems();
}
