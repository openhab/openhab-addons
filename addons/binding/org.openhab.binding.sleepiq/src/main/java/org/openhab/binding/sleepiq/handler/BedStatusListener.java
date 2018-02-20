/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sleepiq.handler;

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
