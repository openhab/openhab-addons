/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.dto.events;

/**
 * The {@link EventsDataDTO} provides all data of Datum
 *
 * @author Ronny Grun - Initial contribution
 */
public class EventsDataDTO {
    public String eventType;
    public String gatewaySerial;
    public Body body;
    public String createdAt;
    public String eventTimestamp;
    public String editedBy;
    public String origin;
}
