/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.types.State;

/**
 * Selection of an attribute within a {@link TimetableStop} that provides a channel {@link State}.
 * 
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public interface AttributeSelection {

    /**
     * Returns the value for this attribute.
     */
    @Nullable
    Object getValue(TimetableStop stop);

    /**
     * Returns the {@link State} that should be set for the channels'value for this attribute.
     */
    @Nullable
    State getState(TimetableStop stop);

    /**
     * Returns a list of values as string list.
     * Returns empty list if value is not present, singleton list if attribute is not single-valued.
     */
    List<String> getStringValues(TimetableStop t);
}
