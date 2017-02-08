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
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunEclipse;
import org.openhab.binding.astro.internal.model.SunPhaseName;

/**
 * Schedules the events for the sun for the current day.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DailyJobSun extends AbstractDailyJob {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void schedulePlanetEvents(String thingUid, AstroThingHandler handler, Planet planet) {
        Sun sun = (Sun) planet;
        scheduleRange(thingUid, handler, sun.getRise(), EVENT_CHANNEL_ID_RISE);
        scheduleRange(thingUid, handler, sun.getSet(), EVENT_CHANNEL_ID_SET);
        scheduleRange(thingUid, handler, sun.getNoon(), EVENT_CHANNEL_ID_NOON);
        scheduleRange(thingUid, handler, sun.getNight(), EVENT_CHANNEL_ID_NIGHT);
        scheduleRange(thingUid, handler, sun.getMorningNight(), EVENT_CHANNEL_ID_MORNING_NIGHT);
        scheduleRange(thingUid, handler, sun.getAstroDawn(), EVENT_CHANNEL_ID_ASTRO_DAWN);
        scheduleRange(thingUid, handler, sun.getNauticDawn(), EVENT_CHANNEL_ID_NAUTIC_DAWN);
        scheduleRange(thingUid, handler, sun.getCivilDawn(), EVENT_CHANNEL_ID_CIVIL_DAWN);
        scheduleRange(thingUid, handler, sun.getAstroDusk(), EVENT_CHANNEL_ID_ASTRO_DUSK);
        scheduleRange(thingUid, handler, sun.getNauticDusk(), EVENT_CHANNEL_ID_NAUTIC_DUSK);
        scheduleRange(thingUid, handler, sun.getCivilDusk(), EVENT_CHANNEL_ID_CIVIL_DUSK);
        scheduleRange(thingUid, handler, sun.getEveningNight(), EVENT_CHANNEL_ID_EVENING_NIGHT);
        scheduleRange(thingUid, handler, sun.getDaylight(), EVENT_CHANNEL_ID_DAYLIGHT);

        SunEclipse eclipse = sun.getEclipse();
        scheduleEvent(thingUid, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE);
        scheduleEvent(thingUid, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE);
        scheduleEvent(thingUid, handler, eclipse.getRing(), EVENT_ECLIPSE_RING, EVENT_CHANNEL_ID_ECLIPSE);

        // schedule republish jobs
        schedulePublishPlanet(thingUid, handler, "zodiac", sun.getZodiac().getEnd());
        schedulePublishPlanet(thingUid, handler, "season", sun.getSeason().getNextSeason());

        // schedule phase jobs
        scheduleSunPhase(thingUid, handler, SunPhaseName.SUN_RISE, sun.getRise().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.SUN_SET, sun.getSet().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.NOON, sun.getNoon().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.NIGHT, sun.getNight().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.DAYLIGHT, sun.getDaylight().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.ASTRO_DAWN, sun.getAstroDawn().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.NAUTIC_DAWN, sun.getNauticDawn().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.CIVIL_DAWN, sun.getCivilDawn().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.ASTRO_DUSK, sun.getAstroDusk().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.NAUTIC_DUSK, sun.getNauticDusk().getStart());
        scheduleSunPhase(thingUid, handler, SunPhaseName.CIVIL_DUSK, sun.getCivilDusk().getStart());
    }

}
