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
package org.openhab.binding.astro.internal.job;

import static org.openhab.binding.astro.internal.job.Job.checkNull;

import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;

/**
 * Scheduled job to trigger events
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class EventJob extends AbstractJob {

    private final String channelID;
    private final String event;

    /**
     * Constructor
     *
     * @param thingUID thing UID
     * @param channelID channel ID
     * @param event Event name
     * @throws IllegalArgumentException
     *             if any of the arguments is {@code null}
     */
    public EventJob(String thingUID, String channelID, String event) {
        super(thingUID);
        checkArgument(channelID != null, "The channelID must not be null");
        checkArgument(event != null, "The event must not be null");
        this.channelID = channelID;
        this.event = event;
    }

    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(getThingUID());
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        astroHandler.triggerEvent(channelID, event);
    }

    @Override
    public String toString() {
        return "Event job " + getThingUID() + "/" + channelID + "/" + event;
    }
}
