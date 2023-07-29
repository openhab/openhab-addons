/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Connector listener to handle connector events.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface DSMRConnectorListener {

    /**
     * Callback for {@link DSMRErrorStatus} events.
     *
     * @param errorStatus {@link DSMRErrorStatus} that has occurred
     * @param message Additional error message
     */
    void handleError(DSMRErrorStatus errorStatus, String message);

    /**
     * Handle data.
     *
     * @param buffer byte buffer with the data
     * @param length length of the data in the buffer. Buffer may be larger than data in buffer, therefore always use
     *            length
     */
    void handleData(byte[] buffer, int length);
}
