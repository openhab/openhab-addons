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
package org.openhab.binding.atmofrance.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * An exception that occurred while communicating with Atmo Data API server or related processes.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AtmoFranceException extends Exception {
    private static final long serialVersionUID = 1L;
    private @Nullable ThingStatusDetail statusDetail;

    public @Nullable ThingStatusDetail getStatusDetail() {
        return statusDetail;
    }

    public AtmoFranceException(String format, Object... args) {
        super(format.formatted(args));
    }

    public AtmoFranceException(Exception e, String format, Object... args) {
        super(format.formatted(args), e);
    }

    public AtmoFranceException(ThingStatusDetail statusDetail, String message) {
        super("@text/" + message);
        this.statusDetail = statusDetail;
    }
}
