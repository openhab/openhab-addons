/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.net.URLConnection;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Versioning} is used in alpha and beta release only !
 *
 * @author Marco Loose - Initial Contribution
 */
@NonNullByDefault
public enum Versioning {

    INSTANCE;

    public final Instant buildTime;

    private Versioning() {
        this.buildTime = this.getLastModifiedDate();
    }

    private Instant getLastModifiedDate() {

        try {
            URLConnection conn = Versioning.class.getResource(Versioning.class.getSimpleName() + ".class")
                    .openConnection();
            var timestamp = conn.getLastModified();
            return Instant.ofEpochMilli(timestamp);
        } catch (Exception e) {
            return Instant.ofEpochMilli(0L);
        }
    }
}
