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
package org.openhab.binding.deutschebahn.internal.filter;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Predicate to match an TimetableStop
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public interface TimetableStopPredicate extends Predicate<TimetableStop> {

}
