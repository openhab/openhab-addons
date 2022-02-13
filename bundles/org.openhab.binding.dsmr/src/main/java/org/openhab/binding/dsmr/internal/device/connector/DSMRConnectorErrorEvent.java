/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error events from a connector.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Reduced number of event to only errors
 */
@NonNullByDefault
public enum DSMRConnectorErrorEvent {
    DONT_EXISTS,
    IN_USE,
    INTERNAL_ERROR,
    NOT_COMPATIBLE,
    READ_ERROR;

    /**
     * @return the event details
     */
    public String getEventDetails() {
        return "@text/error.connector." + name().toLowerCase();
    }
}
