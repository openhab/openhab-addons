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
package org.openhab.binding.caldav.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class CalendarConfiguration {
    public String path = "";
    public String calendarId = "";
    public boolean enabled = true;
    public String rangeAnchor = "TODAY";
    public int rangeStartOffset = 0;
    public int rangeEndOffset = 6;
    public int maxEvents = 500;
    public boolean includeCancelled = false;
}
