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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digiplex.internal.communication.events.AreaEvent;
import org.openhab.binding.digiplex.internal.communication.events.GenericEvent;
import org.openhab.binding.digiplex.internal.communication.events.SpecialAlarmEvent;
import org.openhab.binding.digiplex.internal.communication.events.TroubleEvent;
import org.openhab.binding.digiplex.internal.communication.events.ZoneEvent;
import org.openhab.binding.digiplex.internal.communication.events.ZoneStatusEvent;

/**
 * Interface for message handlers.
 *
 * Visitor pattern is used to dispatch message processing to proper methods.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public interface DigiplexMessageHandler {

    default void handleCommunicationStatus(CommunicationStatus response) {
    }

    default void handleZoneLabelResponse(ZoneLabelResponse response) {
    }

    default void handleZoneStatusResponse(ZoneStatusResponse response) {
    }

    default void handleAreaLabelResponse(AreaLabelResponse response) {
    }

    default void handleAreaStatusResponse(AreaStatusResponse response) {
    }

    default void handleArmDisarmAreaResponse(AreaArmDisarmResponse response) {
    }

    default void handleUnknownResponse(UnknownResponse response) {
    }

    // Events
    default void handleZoneEvent(ZoneEvent event) {
    }

    default void handleZoneStatusEvent(ZoneStatusEvent event) {
    }

    default void handleSpecialAlarmEvent(SpecialAlarmEvent event) {
    }

    default void handleAreaEvent(AreaEvent event) {
    }

    default void handleGenericEvent(GenericEvent event) {
    }

    default void handleTroubleEvent(TroubleEvent troubleEvent) {
    }
}
