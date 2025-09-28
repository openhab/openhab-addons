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

import static org.openhab.binding.astro.internal.AstroBindingConstants.CHANNEL_ID_SUN_PHASE_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.core.thing.Channel;

/**
 * Scheduled job for Sun phase change
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class SunPhaseJob extends AbstractJob {

    private final SunPhaseName sunPhaseName;

    /**
     * Constructor
     *
     * @param handler the thing handler
     * @param sunPhaseName {@link SunPhaseName} name
     * @throws IllegalArgumentException
     *             if any of the arguments is {@code null}
     */
    public SunPhaseJob(AstroThingHandler handler, SunPhaseName sunPhaseName) {
        super(handler);
        this.sunPhaseName = sunPhaseName;
    }

    @Override
    public void run() {
        try {
            Channel phaseNameChannel = handler.getThing().getChannel(CHANNEL_ID_SUN_PHASE_NAME);
            if (phaseNameChannel != null) {
                if (handler.getPlanet() instanceof Sun theSun) {
                    theSun.getPhase().setName(sunPhaseName);
                    handler.publishChannelIfLinked(phaseNameChannel.getUID());
                }
            } else {
                logger.trace("Phase Name Channel for {} is null", handler.getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("The publishing of the sun phase for \"{}\" failed: {}", handler.getThing().getUID(),
                    e.getMessage());
            logger.trace("", e);
        }
    }

    @Override
    public String toString() {
        return "Sun phase job " + handler.getThing().getUID() + "/" + sunPhaseName;
    }
}
