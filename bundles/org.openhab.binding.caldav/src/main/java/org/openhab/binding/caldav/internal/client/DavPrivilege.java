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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Known DAV privileges reported by the server; aggregate privileges are not expanded into inferred rights.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public enum DavPrivilege {
    READ("read"),
    WRITE("write"),
    WRITE_CONTENT("write-content"),
    WRITE_PROPERTIES("write-properties"),
    BIND("bind"),
    UNBIND("unbind");

    private final String externalName;

    DavPrivilege(String externalName) {
        this.externalName = externalName;
    }

    public String externalName() {
        return externalName;
    }
}
