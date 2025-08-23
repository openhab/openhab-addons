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
package org.openhab.binding.sedif.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Will be thrown for cloud errors
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SedifException extends Exception {
    private static final long serialVersionUID = 3703839284673384018L;

    public SedifException() {
        super();
    }

    public SedifException(String message) {
        super(message);
    }

    public SedifException(Exception e, String message) {
        super(message, e);
    }

    public SedifException(String message, Object... params) {
        this(message.formatted(params));
    }

    public SedifException(Exception e, String message, Object... params) {
        this(e, message.formatted(params));
    }
}
