/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MyenergiException} class is the parent of all myenergi binding exceptions.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyenergiException extends Exception {

    private static final long serialVersionUID = -7893729815604230535L;

    public MyenergiException() {
        super();
    }

    public MyenergiException(String message) {
        super(message);
    }

    public MyenergiException(Throwable cause) {
        super(cause);
    }

    public MyenergiException(String message, Throwable cause) {
        super(message, cause);
    }
}
