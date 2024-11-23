/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.filter;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.AttributeSelection;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Abstract predicate that filters timetable stops by a selected attribute of a {@link TimetableStop}.
 * 
 * If value has multiple values (for example stations on the planned-path) the predicate will return <code>true</code>,
 * if at least one value matches the given filter.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class TimetableStopByStringEventAttributeFilter implements TimetableStopPredicate {

    private final AttributeSelection attributeSelection;
    private final Pattern filter;

    /**
     * Creates a new {@link TimetableStopByStringEventAttributeFilter}.
     */
    TimetableStopByStringEventAttributeFilter(final AttributeSelection attributeSelection, final Pattern filter) {
        this.attributeSelection = attributeSelection;
        this.filter = filter;
    }

    @Override
    public boolean test(TimetableStop t) {
        final List<String> values = attributeSelection.getStringValues(t);

        for (String actualValue : values) {
            if (filter.matcher(actualValue).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link AttributeSelection}.
     */
    final AttributeSelection getAttributeSelection() {
        return attributeSelection;
    }

    /**
     * Returns the filter pattern.
     */
    final Pattern getFilter() {
        return filter;
    }
}
