package org.openhab.binding.icalpresence.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Summary;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instanciation.
 *
 * @author Michael Wodniok - Initial contribution
 */
class PresentableCalendar extends AbstractPresentableCalendar {
    private final @NonNull TemporalAmount lookAround;
    private final @NonNull Calendar bakingCalendar;

    PresentableCalendar(@NonNull InputStream streamed, @NonNull TemporalAmount lookAround)
            throws IOException, CalendarException {
        if (System.getProperty("net.fortuna.ical4j.timezone.cache.impl") == null) {
            System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
        }
        try {
            this.bakingCalendar = new CalendarBuilder().build(streamed);
            this.lookAround = lookAround;
        } catch (ParserException e) {
            throw new CalendarException("Exception occured while building calendar from stream", e);
        }
    }

    @Override
    public boolean isEventPresent(Instant instant) {
        return (this.getCurrentComponentWPeriod(instant) != null);
    }

    @Override
    public @Nullable Event getCurrentEvent(Instant instant) {
        VEventWPeriod currentComponentWPeriod = this.getCurrentComponentWPeriod(instant);
        if (currentComponentWPeriod == null) {
            return null;
        }

        return currentComponentWPeriod.toEvent();
    }

    @Override
    public @Nullable Event getNextEvent(Instant instant) {
        VEventWPeriod earliestNextEvent = null;
        VEventWPeriod currentVEventWPeriod = this.getCurrentComponentWPeriod(instant);

        Period recurrenceCalculation = new Period(new DateTime(instant.getEpochSecond() * 1000),
                new DateTime(instant.plus(lookAround).getEpochSecond() * 1000));
        for (Component currentComponent : bakingCalendar.getComponents(Component.VEVENT)) {
            VEvent currentVEvent = (VEvent) currentComponent;
            for (Period recurredPeriod : currentVEvent.calculateRecurrenceSet(recurrenceCalculation)) {
                if ((earliestNextEvent == null || earliestNextEvent.period.getStart().after(recurredPeriod.getStart()))
                        && (currentVEventWPeriod == null || !currentVEvent.equals(currentVEventWPeriod.vEvent)
                                || !recurredPeriod.equals(currentVEventWPeriod.period))) {
                    earliestNextEvent = new VEventWPeriod(currentVEvent, recurredPeriod);
                }
            }
        }

        if (earliestNextEvent == null) {
            return null;
        }
        return earliestNextEvent.toEvent();
    }

    private @Nullable VEventWPeriod getCurrentComponentWPeriod(Instant instant) {
        Period recurrenceCalculation = new Period(new DateTime(instant.minus(lookAround).getEpochSecond() * 1000),
                new DateTime(instant.plus(lookAround).getEpochSecond() * 1000));
        DateTime instantDate = new DateTime(instant.getEpochSecond() * 1000);
        for (Component currentComponent : bakingCalendar.getComponents(Component.VEVENT)) {
            VEvent currentVEvent = (VEvent) currentComponent;
            for (Period recurredPeriod : currentVEvent.calculateRecurrenceSet(recurrenceCalculation)) {
                if (recurredPeriod.includes(instantDate)) {
                    return new VEventWPeriod(currentVEvent, recurredPeriod);
                }
            }
        }
        return null;
    }

    private static class VEventWPeriod {
        VEvent vEvent;
        Period period;

        VEventWPeriod(VEvent vEvent, Period period) {
            this.vEvent = vEvent;
            this.period = period;
        }

        @NonNull
        Event toEvent() {
            String title;
            Summary eventSummary = vEvent.getSummary();
            if (eventSummary != null) {
                title = eventSummary.getValue();
            } else {
                title = "-";
            }
            Instant start = Instant.ofEpochMilli(this.period.getStart().getTime());
            Instant end = Instant.ofEpochMilli(this.period.getEnd().getTime());
            return new Event(title, start, end);
        }
    }
}
