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
package org.openhab.binding.meteoalerte.internal.deserialization;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.Period;
import org.openhab.binding.meteoalerte.internal.dto.Term;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Periods extends HashMap<Term, @Nullable Period> {
    private static final long serialVersionUID = -4448877461442293135L;
}
