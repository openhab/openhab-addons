/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.panamaxfurman.internal.protocol.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event fired when the connection to the Power Conditioner is broken.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanConnectionBrokenEvent implements PanamaxFurmanConnectivityEvent {

    private Throwable cause;
    private final @Nullable String errorDetail;

    public PanamaxFurmanConnectionBrokenEvent(Exception e) {
        this.cause = e;
        this.errorDetail = e.getClass().getSimpleName() + " " + e.getMessage();
    }

    public @Nullable String getErrorDetail() {
        return errorDetail;
    }

    @Override
    public String toString() {
        return "PanamaxFurmanConnectionBrokenEvent " + errorDetail;
    }

    public Throwable getCause() {
        return cause;
    }
}
