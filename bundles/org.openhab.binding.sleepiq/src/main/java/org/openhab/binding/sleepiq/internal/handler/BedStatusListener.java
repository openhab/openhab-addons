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
package org.openhab.binding.sleepiq.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
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
     * @param cloud the cloud service that can be used to gather additional information
     * @param status the status returned from the cloud service
     */
    public void onBedStateChanged(BedStatus status);

    public void onSleeperChanged(@Nullable Sleeper sleeper);
}
