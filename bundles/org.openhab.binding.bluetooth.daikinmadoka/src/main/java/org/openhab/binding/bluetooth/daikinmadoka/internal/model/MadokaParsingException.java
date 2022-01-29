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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This exception is thrown when an exception happens parsing a message from the BLE controller.
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class MadokaParsingException extends Exception {

    public MadokaParsingException(String message) {
        super(message);
    }

    public MadokaParsingException(Throwable t) {
        super(t);
    }
}
