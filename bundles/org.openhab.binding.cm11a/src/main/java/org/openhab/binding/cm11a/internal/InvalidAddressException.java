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
package org.openhab.binding.cm11a.internal;

/**
 * Exception for invalid X10 House / Unit code
 *
 * @author Bob Raker - Initial contribution
 */
public class InvalidAddressException extends Exception {

    private static final long serialVersionUID = -1049253542819790311L;

    public InvalidAddressException(String string) {
        super(string);
    }
}
