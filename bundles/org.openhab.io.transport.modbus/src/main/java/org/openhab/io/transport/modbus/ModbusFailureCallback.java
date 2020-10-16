/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Callback used to report failure in Modbus
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface ModbusFailureCallback<R> {
    /**
     * Callback handling response with error
     *
     * @param asyncModbusFailure details of the failure
     */
    void handle(AsyncModbusFailure<R> failure);
}
