/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import static org.openhab.binding.astro.AstroBindingConstants.*;

import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.Planet;

/**
 * Schedules the events for the moon for the current day.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DailyJobMoon extends AbstractDailyJob {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void schedulePlanetEvents(String thingUid, AstroThingHandler handler, Planet planet) {
        Moon moon = (Moon) planet;
        scheduleEvent(thingUid, handler, moon.getRise().getStart(), EVENT_START, EVENT_CHANNEL_ID_RISE);
        scheduleEvent(thingUid, handler, moon.getSet().getEnd(), EVENT_END, EVENT_CHANNEL_ID_SET);

        MoonPhase moonPhase = moon.getPhase();
        scheduleEvent(thingUid, handler, moonPhase.getFirstQuarter(), EVENT_PHASE_FIRST_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE);
        scheduleEvent(thingUid, handler, moonPhase.getThirdQuarter(), EVENT_PHASE_THIRD_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE);
        scheduleEvent(thingUid, handler, moonPhase.getFull(), EVENT_PHASE_FULL, EVENT_CHANNEL_ID_MOON_PHASE);
        scheduleEvent(thingUid, handler, moonPhase.getNew(), EVENT_PHASE_NEW, EVENT_CHANNEL_ID_MOON_PHASE);

        Eclipse eclipse = moon.getEclipse();
        scheduleEvent(thingUid, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE);
        scheduleEvent(thingUid, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE);

        scheduleEvent(thingUid, handler, moon.getPerigee().getDate(), EVENT_PERIGEE, EVENT_CHANNEL_ID_PERIGEE);
        scheduleEvent(thingUid, handler, moon.getApogee().getDate(), EVENT_APOGEE, EVENT_CHANNEL_ID_APOGEE);
    }

}
