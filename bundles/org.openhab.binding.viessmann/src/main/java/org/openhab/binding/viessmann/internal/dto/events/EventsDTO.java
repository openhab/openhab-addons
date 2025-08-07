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
package org.openhab.binding.viessmann.internal.dto.events;

import java.util.List;

/**
 * The {@link EventsDTO} provides all data of a EventsDTO
 *
 * @author Ronny Grun - Initial contribution
 */
public class EventsDTO {
    public List<EventsDataDTO> data = null;
    public Cursor cursor;
}
