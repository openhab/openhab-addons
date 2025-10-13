/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NhcCarChargerEvent} interface is used to pass car charger events received from the Niko Home Control
 * controller to the consuming client. It is designed to pass events to openHAB handlers that implement this interface.
 * Because of the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used
 * independent of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcCarChargerEvent extends NhcBaseEvent {

    /**
     * Handles an update event related to the charging status of a car charger.
     * This event will update the respective car charger thing channels.
     *
     * @param status true if charging is active, false otherwise.
     * @param chargingStatus the current charging status as a string, or null if unavailable.
     * @param evStatus the electric vehicle status as a string, or null if unavailable.
     * @param couplingStatus the coupling status as a string, or null if unavailable.
     * @param electricalPower the electrical power being delivered (in watts), or null if unavailable.
     */
    void chargingStatusEvent(boolean status, @Nullable String chargingStatus, @Nullable String evStatus,
            @Nullable String couplingStatus, @Nullable Integer electricalPower);

    /**
     * Handles an update event related to the car charger's charging mode.
     * This event will update the respective car charger thing channels.
     *
     * @param chargingMode the current charging mode, or {@code null} if not available
     * @param targetDistance the target distance to be achieved by charging (in kilometers)
     * @param targetTime the target time to reach the desired charge, or {@code null} if not specified
     * @param boost {@code true} if boost mode is enabled; {@code false} otherwise
     * @param reachableDistance the currently reachable distance with the SMART charging mode (in kilometers)
     * @param nextChargingTime the next scheduled charging time, or {@code null} if not set
     */
    void chargingModeEvent(@Nullable String chargingMode, float targetDistance, @Nullable String targetTime,
            boolean boost, float reachableDistance, @Nullable String nextChargingTime);

    /**
     * This method is called when a meter reading is received from the Niko Home Control controller.
     *
     * @param reading meter reading
     * @param dayReading meter reading for current day
     * @param lastReadingUTC last meter reading date and time, UTC
     */
    void meterReadingEvent(double reading, double dayReading, LocalDateTime lastReadingUTC);
}
