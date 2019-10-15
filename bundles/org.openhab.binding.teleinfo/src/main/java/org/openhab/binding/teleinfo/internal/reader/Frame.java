/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The {@link Frame} class defines common attributes for any Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class Frame implements Serializable {

    private static final long serialVersionUID = -1934715078822532494L;

    private UUID id;
    private LocalDate timestamp; // UTC timestamp

    public Frame() {
        // default constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

}
