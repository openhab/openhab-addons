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

@NonNullByDefault
public final class CalDavBindingConstants {
    public static final String BINDING_ID = "caldav";
    public static final String ACCOUNT_THING_TYPE = "account";
    public static final String CALENDAR_THING_TYPE = "calendar";
    public static final String GROUP_EVENTS = "events";
    public static final String GROUP_CURRENT = "current";
    public static final String GROUP_NEXT = "next";
    public static final String GROUP_SYNC = "sync";
    public static final Set<String> THING_TYPES = Set.of(ACCOUNT_THING_TYPE, CALENDAR_THING_TYPE);

    private CalDavBindingConstants() {
    }
}
