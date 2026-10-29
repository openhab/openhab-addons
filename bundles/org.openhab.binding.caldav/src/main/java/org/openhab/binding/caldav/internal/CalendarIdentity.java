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

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.core.thing.ThingUID;

/**
 * Identifies a calendar reference within a configured bridge, independently of its connection settings.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public final class CalendarIdentity {
    private CalendarIdentity() {
    }

    /**
     * Creates the bridge-scoped representation of a same-origin calendar collection.
     *
     * @param bridge the owning account bridge
     * @param accountUrl the account URL used to validate and resolve the collection reference
     * @param collection the calendar collection URI
     * @return the bridge UID followed by the canonical origin-relative collection reference
     * @throws IllegalArgumentException if the collection is unsafe or belongs to another origin
     */
    public static String create(ThingUID bridge, URI accountUrl, URI collection) {
        return bridge.getAsString() + "|" + CalDavUris.originRelative(accountUrl, collection);
    }
}
