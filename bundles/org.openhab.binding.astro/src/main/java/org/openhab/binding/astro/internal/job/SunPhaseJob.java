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

import static org.openhab.binding.astro.internal.AstroBindingConstants.CHANNEL_ID_SUN_PHASE_NAME;
import static org.openhab.binding.astro.internal.job.Job.checkNull;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;

/**
 * Scheduled job for Sun phase change
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class SunPhaseJob extends AbstractJob {

    private final SunPhaseName sunPhaseName;

    /**
     * Constructor
     *
     * @param thingUID thing UID
     * @param sunPhaseName {@link SunPhaseName} name
     * @throws IllegalArgumentException
     *             if any of the arguments is {@code null}
     */
    public SunPhaseJob(String thingUID, SunPhaseName sunPhaseName) {
        super(thingUID);
        checkArgument(sunPhaseName != null, "The sunPhaseName must not be null");
        this.sunPhaseName = sunPhaseName;
    }

    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(getThingUID());
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        Channel phaseNameChannel = astroHandler.getThing().getChannel(CHANNEL_ID_SUN_PHASE_NAME);
        if (checkNull(phaseNameChannel, "Phase Name Channel is null")) {
            return;
        }
        Planet planet = astroHandler.getPlanet();
        if (planet != null && planet instanceof Sun) {
            final Sun typedSun = (Sun) planet;
            typedSun.getPhase().setName(sunPhaseName);
            astroHandler.publishChannelIfLinked(phaseNameChannel.getUID());
        }
    }

    @Override
    public String toString() {
        return "Sun phase job " + getThingUID() + "/" + sunPhaseName;
    }
}
