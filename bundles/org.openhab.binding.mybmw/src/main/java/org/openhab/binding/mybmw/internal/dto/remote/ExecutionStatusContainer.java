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
package org.openhab.binding.mybmw.internal.dto.remote;

/**
 * The {@link ExecutionStatusContainer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored to Java Bean
 */
public class ExecutionStatusContainer {
    private String eventId = "";
    private String creationTime = "";
    private String eventStatus = "";
    private ExecutionError errorDetails = null;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public ExecutionError getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(ExecutionError errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        return "ExecutionStatusContainer [eventId=" + eventId + ", creationTime=" + creationTime + ", eventStatus="
                + eventStatus + ", errorDetails=" + errorDetails + "]";
    }
}
