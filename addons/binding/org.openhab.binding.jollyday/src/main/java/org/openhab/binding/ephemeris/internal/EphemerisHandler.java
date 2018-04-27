/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ephemeris.internal;

import static org.openhab.binding.ephemeris.EphemerisBindingConstants.*;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ephemeris.internal.config.EphemerisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;

/**
 * Common abstract class for Jollyday dependant handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public abstract class EphemerisHandler extends BaseThingHandler {
    private static CronExpression expression;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ExpressionThreadPoolExecutor scheduledExecutor = ExpressionThreadPoolManager
            .getExpressionScheduledPool(BINDING_ID);

    protected final Locale systemLocale;
    protected EphemerisConfig configuration;
    private HolidayManager holidayManager;

    public EphemerisHandler(Thing thing, Locale systemLocale) {
        super(thing);
        this.systemLocale = systemLocale;
        try {
            expression = new CronExpression("30 0 0 * * ? *");
        } catch (ParseException e) {
            logger.error("Error in the cron expression");
        }
    }

    public abstract ManagerParameter getManagagerParameter();

    public String getCountry() {
        return configuration.country == null ? systemLocale.getCountry() : configuration.country;
    }

    public abstract void handleUpdate(Set<Holiday> holidays);

    @Override
    public void initialize() {
        configuration = getConfigAs(EphemerisConfig.class);
        ManagerParameter mp = getManagagerParameter();

        try {
            holidayManager = HolidayManager.getInstance(mp);
            scheduledExecutor.schedule(dailyRunnable, expression);
            dailyRunnable.run();
            updateStatus(ThingStatus.ONLINE);

        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No holidays resource file found : " + mp.getDisplayName());
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        dailyRunnable.run();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        scheduledExecutor.remove(expression);
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    protected Runnable dailyRunnable = new Runnable() {

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DATE, configuration.offset);

            ZonedDateTime a = calendar.toInstant().atZone(calendar.getTimeZone().toZoneId());
            try {
                Set<Holiday> holidays = null;
                if (configuration.region == null) {
                    holidays = holidayManager.getHolidays(a.toLocalDate(), a.toLocalDate());
                } else if (configuration.city == null) {
                    holidays = holidayManager.getHolidays(a.toLocalDate(), a.toLocalDate(), configuration.region);
                } else {
                    holidays = holidayManager.getHolidays(a.toLocalDate(), a.toLocalDate(), configuration.region,
                            configuration.city);
                }

                updateState(new ChannelUID(getThing().getUID(), CHANNEL_EVENT_DATE), new DateTimeType(calendar));
                handleUpdate(holidays);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error analyzing Holiday file : " + e.getMessage());
            }
        }
    };

}
