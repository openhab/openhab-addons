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
package org.openhab.binding.sleepiq.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.Sleeper;

/**
 * The {@link BedStatusListener} is notified when a chamber is updated.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public interface BedStatusListener {
    /**
     * This method will be called whenever a new bed status is received by the cloud handler.
     *
     * @param status the bed status returned from the cloud service
     */
    void onBedStateChanged(BedStatus status);

    /**
     * This method will be called whenever a new foundation status is received by the cloud handler.
     *
     * @param status the foundation status returned from the cloud service
     */
    void onFoundationStateChanged(String bedId, FoundationStatusResponse status);

    /**
     * Determine if bed has a foundation installed.
     *
     * @return true if bed has a foundation; otherwise falase
     */
    boolean isFoundationInstalled();

    void onSleeperChanged(@Nullable Sleeper sleeper);
}
