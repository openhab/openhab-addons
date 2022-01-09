/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.timetable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Utility for merging timetable stops.
 * This is required, thus first only the plan is returned from the API and afterwards the loaded timetable-stops must be
 * merged with the fetched changes.
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
final class TimetableStopMerger {

    /**
     * Merges the {@link TimetableStop} inplace to the first TimetableStop.
     */
    public static void merge(final TimetableStop first, final TimetableStop second) {
        mergeStopAttributes(first, second);
    }

    /**
     * Updates all values from the second {@link TimetableStop} into the first one.
     */
    private static void mergeStopAttributes(final TimetableStop first, final TimetableStop second) {
        mergeEventAttributes(first.getAr(), second.getAr());
        mergeEventAttributes(first.getDp(), second.getDp());
    }

    /**
     * Updates all values from the second Event into the first one.
     */
    private static void mergeEventAttributes(@Nullable final Event first, @Nullable final Event second) {
        if ((first == null) || (second == null)) {
            return;
        }

        for (final EventAttribute<?, ?> attribute : EventAttribute.ALL_ATTRIBUTES) {
            updateAttribute(attribute, first, second);
        }
    }

    /**
     * Sets the value of the given {@link EventAttribute} from the second Event in the first event, if not
     * <code>null</code>.
     */
    private static <VALUE_TYPE> void updateAttribute(final EventAttribute<VALUE_TYPE, ?> attribute, final Event first,
            final Event second) {
        final @Nullable VALUE_TYPE value = attribute.getValue(second);
        if (value != null) {
            attribute.setValue(first, value);
        }
    }
}
