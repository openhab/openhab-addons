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
package org.openhab.binding.kvv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds information parameters.
 *
 * @author Maximilian Hess - Initial contribution
 *
 */
@NonNullByDefault
public class KVVStationConfig {

    /** the id of the station */
    public @Nullable String stationId;

    /** maximum number of traines being queried */
    public int maxTrains;

    public int updateInterval;

}
