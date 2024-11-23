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
package org.openhab.binding.ephemeris.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Exception raised by Ephemeris Handlers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class EphemerisException extends Exception {
    private static final long serialVersionUID = -8813754360966576513L;
    private final ThingStatusDetail statusDetail;

    public EphemerisException(String message, ThingStatusDetail statusDetail) {
        super(message);
        this.statusDetail = statusDetail;
    }

    public ThingStatusDetail getStatusDetail() {
        return statusDetail;
    }
}
