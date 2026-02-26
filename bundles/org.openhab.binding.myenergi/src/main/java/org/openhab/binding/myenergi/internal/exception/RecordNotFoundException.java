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
 * The {@link RecordNotFoundException} is thrown if a record is not found during a search.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class RecordNotFoundException extends MyenergiException {

    private static final long serialVersionUID = 4808759343110248340L;

    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(Throwable cause) {
        super(cause);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
