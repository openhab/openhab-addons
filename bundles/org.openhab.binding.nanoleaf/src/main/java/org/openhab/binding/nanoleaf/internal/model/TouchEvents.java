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
package org.openhab.binding.nanoleaf.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents effect commands for select and write
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class TouchEvents {

    private List<TouchEvent> events = new ArrayList<>();

    public List<TouchEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TouchEvent> events) {
        this.events = events;
    }
}
