/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.syphr.sleepiq.api.SleepIQ;
import org.syphr.sleepiq.api.model.BedStatus;

/**
 * The {@link BedStatusListener} is notified when a chamber is updated.
 *
 * @author Gregory Moyer - Initial contribution
 */
public interface BedStatusListener {
    /**
     * This method will be called whenever a new bed status is received by the cloud handler.
     *
     * @param cloud the cloud service that can be used to gather additional information
     * @param status the status returned from the cloud service
     */
    public void onBedStateChanged(SleepIQ cloud, BedStatus status);
}
