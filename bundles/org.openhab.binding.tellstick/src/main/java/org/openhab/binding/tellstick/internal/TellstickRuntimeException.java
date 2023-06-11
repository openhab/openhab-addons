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
package org.openhab.binding.tellstick.internal;

/**
 * Runtime exception in tellstick binding.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1644730263645760297L;

    public TellstickRuntimeException(String msg) {
        super(msg);
    }
}
