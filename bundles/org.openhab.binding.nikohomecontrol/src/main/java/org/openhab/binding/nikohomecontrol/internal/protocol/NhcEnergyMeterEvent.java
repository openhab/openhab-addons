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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NhcEnergyMeterEvent} interface is used to pass energyMeters meter events received from the Niko Home
 * Control
 * controller to the consuming client. It is designed to pass events to openHAB handlers that implement this interface.
 * Because of the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used
 * independent of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcEnergyMeterEvent {

    /**
     * This method is called when an energyMeter event is received from the Niko Home Control controller.
     *
     * @param power current power consumption/production in W (positive for consumption), null for an empty reading
     */
    void energyMeterEvent(@Nullable Integer power);

    /**
     * Called to indicate the energyMeter has been initialized.
     *
     */
    void energyMeterInitialized();

    /**
     * Called to indicate the energyMeter has been removed from the Niko Home Control controller.
     *
     */
    void energyMeterRemoved();
}
