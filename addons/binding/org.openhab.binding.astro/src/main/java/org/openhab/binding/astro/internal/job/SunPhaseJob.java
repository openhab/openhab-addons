/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import static org.openhab.binding.astro.AstroBindingConstants.CHANNEL_ID_SUN_PHASE_NAME;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.quartz.JobDataMap;

/**
 * Job to publish the current sun phase.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunPhaseJob extends AbstractBaseJob {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(String thingUid, JobDataMap jobDataMap) {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        Channel phaseNameChannel = astroHandler.getThing().getChannel(CHANNEL_ID_SUN_PHASE_NAME);
        if (astroHandler != null && phaseNameChannel != null) {
            SunPhaseName phaseName = (SunPhaseName) jobDataMap.get(KEY_PHASE_NAME);
            ((Sun) astroHandler.getPlanet()).getPhase().setName(phaseName);
            astroHandler.publishChannelIfLinked(phaseNameChannel.getUID());
        }
    }
}
