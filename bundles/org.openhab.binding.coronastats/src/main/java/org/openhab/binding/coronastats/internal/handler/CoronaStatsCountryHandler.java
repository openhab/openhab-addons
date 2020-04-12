/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.coronastats.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.coronastats.internal.dto.CoronaStats;

/**
 * The {@link CoronaStatsCountryHandler} is the handler for world/country thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountryHandler {
    public void notifyOnUpdate(CoronaStats localCoronaStats) {
    }
}
