/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.types.State;

/**
 * Selection of an attribute within an {@link TimetableStop} that provides a channel {@link State}.
 * 
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public interface AttributeSelection {

    /**
     * Returns the {@link State} that should be set for the channels'value for this attribute.
     */
    @Nullable
    public abstract State getState(TimetableStop stop);
}
