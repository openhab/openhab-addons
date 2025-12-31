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
package org.openhab.binding.astro.internal.job;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;

/**
 * Scheduled job to trigger events
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class EventJob extends AbstractJob {

    private final String channelID;
    private final String event;

    /**
     * Constructor
     *
     * @param handler the thing handler
     * @param channelID channel ID
     * @param event Event name
     * @throws IllegalArgumentException
     *             if any of the arguments is {@code null}
     */
    public EventJob(AstroThingHandler handler, String channelID, String event) {
        super(handler);
        this.channelID = channelID;
        this.event = event;
    }

    @Override
    public void run() {
        try {
            handler.triggerEvent(channelID, event);
        } catch (Exception e) {
            LOGGER.warn("The triggering of event \"{}\" for \"{}\" failed: {}", event, handler.getThing().getUID(),
                    e.getMessage());
            LOGGER.trace("", e);
        }
    }

    @Override
    public String toString() {
        return "Event job " + handler.getThing().getUID() + "/" + channelID + "/" + event;
    }
}
