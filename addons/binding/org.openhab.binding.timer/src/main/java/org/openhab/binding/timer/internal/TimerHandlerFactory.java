/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.timer.internal;

import static org.openhab.binding.timer.TimerBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.timer.handler.DailyTimerHandler;
import org.openhab.binding.timer.handler.MonthlyTimerHandler;
import org.openhab.binding.timer.handler.OnceDateTimeTimerHandler;
import org.openhab.binding.timer.handler.OnceDelayTimerHandler;
import org.openhab.binding.timer.handler.PeriodicTimerHandler;

/**
 * The {@link TimerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Neil Renaud - Initial contribution
 */
public class TimerHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_DAILY_TIMER)) {
            return new DailyTimerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PERIODIC_TIMER)) {
            return new PeriodicTimerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MONTHLY_TIMER)) {
            return new MonthlyTimerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ONE_TIME_BY_DATE_TIMER)) {
            return new OnceDateTimeTimerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ONE_TIME_BY_DELAY_TIMER)) {
            return new OnceDelayTimerHandler(thing);
        }

        return null;
    }
}
