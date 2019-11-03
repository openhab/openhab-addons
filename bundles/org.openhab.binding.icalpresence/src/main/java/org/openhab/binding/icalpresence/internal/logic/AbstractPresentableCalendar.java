package org.openhab.binding.icalpresence.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A calendar-class which provides everything needed for the binding,
 * encapsulating the implementation of the real calendar.
 *
 * @author Michael Wodniok - Initial contribution
 */
public abstract class AbstractPresentableCalendar {

    /**
     * Creates an implementing Instance of AbstractPresentableCalendar.
     *
     * @param calendarStream A Stream containing the iCal-data.
     * @return The instance.
     * @throws IOException When something while reading stream fails.
     * @throws CalendarException When something while parsing fails.
     */
    public static @NonNull AbstractPresentableCalendar create(@NonNull InputStream calendarStream)
            throws IOException, CalendarException {
        return new PresentableCalendar(calendarStream, Duration.ofDays(2));
    }

    /**
     * Checks whether an event is present at given Instant.
     *
     * @param instant The Instant, that should be checked.
     * @return Whether an event is present.
     */
    public abstract boolean isEventPresent(@NonNull Instant instant);

    /**
     * Searches the event currently (at given Instant) present.
     *
     * @param instant The Instant, the event should be returned for.
     * @return The current {@link Event} containing the data of the event or
     *         null if no event is present.
     */
    public abstract @Nullable Event getCurrentEvent(@NonNull Instant instant);

    /**
     * The next event after given instant.
     *
     * @param instant The Instant after which the next event should be
     *            searched.
     * @return The next event after the given Instant or null if there is any
     *         further in the calendar.
     */
    public abstract @Nullable Event getNextEvent(@NonNull Instant instant);
}
