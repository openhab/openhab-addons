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
package org.openhab.binding.qolsysiq.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qolsysiq.internal.client.dto.event.AlarmEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ArmingEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ErrorEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.SecureArmInfoEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.SummaryInfoEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneActiveEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneAddEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneUpdateEvent;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface QolsysIQClientListener {
    /**
     * Callback when the connection has been disconnected
     *
     * @param reason
     */
    void disconnected(Exception reason);

    /**
     * {@link AlarmEvent} message callback
     *
     * @param event
     */
    void alarmEvent(AlarmEvent event);

    /**
     * {@link ArmingEvent} message callback
     *
     * @param event
     */
    void armingEvent(ArmingEvent event);

    /**
     * {@link ErrorEvent} message callback
     *
     * @param event
     */
    void errorEvent(ErrorEvent event);

    /**
     * {@link SummaryInfoEvent} message callback
     *
     * @param event
     */
    void summaryInfoEvent(SummaryInfoEvent event);

    /**
     * {@link SecureArmInfoEvent} message callback
     *
     * @param event
     */
    void secureArmInfoEvent(SecureArmInfoEvent event);

    /**
     * {@link ZoneActiveEvent} message callback
     *
     * @param event
     */
    void zoneActiveEvent(ZoneActiveEvent event);

    /**
     * {@link ZoneUpdateEvent} message callback
     *
     * @param event
     */
    void zoneUpdateEvent(ZoneUpdateEvent event);

    /**
     * {@link ZoneAddEvent} message callback
     *
     * @param event
     */
    void zoneAddEvent(ZoneAddEvent event);
}
