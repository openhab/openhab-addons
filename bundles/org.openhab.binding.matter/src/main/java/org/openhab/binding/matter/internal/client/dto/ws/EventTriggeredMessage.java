/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client.dto.ws;

/**
 * EventTriggeredMessage is a message that is sent when an matter event is triggered.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EventTriggeredMessage {
    public Path path;
    public TriggerEvent[] events;

    public EventTriggeredMessage() {
    }

    public EventTriggeredMessage(Path path, TriggerEvent[] events) {
        this.path = path;
        this.events = events;
    }
}
