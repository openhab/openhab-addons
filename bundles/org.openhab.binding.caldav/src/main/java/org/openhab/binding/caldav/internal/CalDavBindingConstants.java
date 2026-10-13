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
package org.openhab.binding.caldav.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Identifiers shared by CalDAV Thing metadata, channels and calendar representation matching. */
@NonNullByDefault
public final class CalDavBindingConstants {
    /** Namespace of the CalDAV binding and its Thing and channel types. */
    public static final String BINDING_ID = "caldav";
    /** Thing type identifier for the account bridge. */
    public static final String ACCOUNT_THING_TYPE = "account";
    /** Thing type identifier for a calendar collection. */
    public static final String CALENDAR_THING_TYPE = "calendar";
    /** Canonical calendar reference within the associated account bridge used for Inbox representation matching. */
    public static final String PROPERTY_CALENDAR_IDENTITY = "calendarIdentity";
    /** Channel group for the bounded event list and output range. */
    public static final String GROUP_EVENTS = "events";
    /** Channel group describing the currently active event. */
    public static final String GROUP_CURRENT = "current";
    /** Channel group describing the next upcoming event. */
    public static final String GROUP_NEXT = "next";
    /** Channel group for synchronization time, status and error details. */
    public static final String GROUP_SYNC = "sync";
    /** Thing type identifiers implemented by this binding. */
    public static final Set<String> THING_TYPES = Set.of(ACCOUNT_THING_TYPE, CALENDAR_THING_TYPE);

    private CalDavBindingConstants() {
    }
}
