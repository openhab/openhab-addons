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
package org.openhab.binding.miio.internal;

/**
 * Will be thrown instead of the many possible errors in the crypto module
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoCryptoException extends Exception {

    public MiIoCryptoException() {
        super();
    }

    public MiIoCryptoException(String arg0) {
        super(arg0);
    }

    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -1280858607995252320L;
}
