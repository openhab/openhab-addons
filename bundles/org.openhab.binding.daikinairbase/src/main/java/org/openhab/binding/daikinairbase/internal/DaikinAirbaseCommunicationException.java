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
package org.openhab.binding.daikinairbase.internal;

import java.io.IOException;

/**
 * Exception for when an unexpected response is received from the Daikin Airbase controller.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 *
 */
public class DaikinAirbaseCommunicationException extends IOException {
    private static final long serialVersionUID = 529232811860854017L;

    public DaikinAirbaseCommunicationException(String message) {
        super(message);
    }

    public DaikinAirbaseCommunicationException(Throwable ex) {
        super(ex);
    }

    public DaikinAirbaseCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
