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
package org.openhab.binding.nadreceiver.internal.protocol;

/**
 * NAD operation
 *
 * @author Marc Chételat - Initial contribution
 *
 */
public enum NadReceiverOperation {
    EXECUTE("="),
    QUERY("?");

    private final String operationChar;

    NadReceiverOperation(String operationChar) {
        this.operationChar = operationChar;
    }

    @Override
    public String toString() {
        return this.operationChar;
    }
}
