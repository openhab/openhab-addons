/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.connector;

/**
 * Error events from a connector.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Reduced number of event to only errors
 */
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
