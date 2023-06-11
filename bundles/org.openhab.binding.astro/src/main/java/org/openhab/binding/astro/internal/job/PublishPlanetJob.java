/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;

/**
 * Scheduled job for planets
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class PublishPlanetJob extends AbstractJob {

    /**
     * Constructor
     *
     * @param thingUID thing UID
     * @throws IllegalArgumentException
     *             if the provided argument is {@code null}
     */
    public PublishPlanetJob(String thingUID) {
        super(thingUID);
    }

    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(getThingUID());
        if (astroHandler != null) {
            astroHandler.publishDailyInfo();
        } else {
            LOGGER.trace("AstroThingHandler is null");
        }
    }

    @Override
    public String toString() {
        return "Publish planet job " + getThingUID();
    }
}
