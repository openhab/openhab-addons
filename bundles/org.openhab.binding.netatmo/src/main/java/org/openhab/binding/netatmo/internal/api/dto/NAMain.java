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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.api.ListBodyResponse;

/**
 * The {@link NAMain} defines a weather or nhc device.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAMain extends Device {
    public class StationDataResponse extends ApiResponse<ListBodyResponse<NAMain>> {
    }

    private boolean readOnly;

    /**
     * true when the user was invited to (or has favorited) a station, false when the user owns it
     *
     * @return readOnly
     **/
    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean hasFreshData(int dataFreshnessLimit) {
        // check by comparing data freshness
        ZonedDateTime localLastSeen = lastSeen;
        if (localLastSeen != null && !getType().isLogical()) {
            return Duration.between(localLastSeen.toInstant(), Instant.now()).getSeconds() < dataFreshnessLimit;
        }
        return true;
    }
}
