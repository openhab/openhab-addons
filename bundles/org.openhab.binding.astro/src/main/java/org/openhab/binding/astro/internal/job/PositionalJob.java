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
 * Scheduled job for planet positions
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class PositionalJob extends AbstractJob {

    /**
     * Constructor
     *
     * @param handler the thing handler
     * @throws IllegalArgumentException
     *             if the provided argument is {@code null}
     */
    public PositionalJob(AstroThingHandler handler) {
        super(handler);
    }

    @Override
    public void run() {
        try {
            handler.publishPositionalInfo();
        } catch (Exception e) {
            logger.warn("The publishing of positional info for \"{}\" failed: {}", handler.getThing().getUID(),
                    e.getMessage());
            logger.trace("", e);
        }
    }

    @Override
    public String toString() {
        return "Positional job " + handler.getThing().getUID();
    }
}
