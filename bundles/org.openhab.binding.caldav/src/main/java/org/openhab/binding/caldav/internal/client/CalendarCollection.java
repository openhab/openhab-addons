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
package org.openhab.binding.caldav.internal.client;

import java.net.URI;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A discovered CalDAV calendar collection.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Optional collection metadata
 */
@NonNullByDefault
public record CalendarCollection(URI uri, String displayName, String description, String color,
        Set<DavPrivilege> privileges) {
    public CalendarCollection {
        privileges = Set.copyOf(privileges);
    }

    public CalendarCollection(URI uri, String displayName) {
        this(uri, displayName, "", "", Set.of());
    }
}
